package com.splitmate.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.domain.model.*
import com.splitmate.domain.repository.ExpenseRepository
import com.splitmate.domain.repository.GroupRepository
import com.splitmate.domain.repository.SettlementRepository
import com.splitmate.domain.repository.UserRepository
import com.splitmate.domain.usecase.balance.CalculateBalanceUseCase
import com.splitmate.domain.usecase.balance.SimplifyDebtsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A unified activity item shown in the recent-activity feed. */
data class ActivityFeedItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val currency: String,
    val date: Long,
    val groupName: String,
    val type: FeedItemType
)

enum class FeedItemType { EXPENSE, SETTLEMENT }

data class DashboardUiState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val totalOwed: Double = 0.0,
    val totalOwedToYou: Double = 0.0,
    val netBalance: Double = 0.0,
    val groups: List<Group> = emptyList(),
    val groupBalances: Map<String, GroupBalance> = emptyMap(),
    val recentExpenses: List<Expense> = emptyList(),
    val recentActivities: List<ActivityFeedItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val calculateBalanceUseCase: CalculateBalanceUseCase,
    private val simplifyDebtsUseCase: SimplifyDebtsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                // Core reactive pipeline: user + groups + all expenses
                combine(
                    userRepository.getCurrentUser(),
                    groupRepository.getAllGroups(),
                    expenseRepository.getAllExpenses()
                ) { currentUser, groups, allExpenses ->
                    Triple(currentUser, groups, allExpenses)
                }.collect { (currentUser, groups, allExpenses) ->
                    if (currentUser == null) {
                        _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                        return@collect
                    }

                    var totalOwed = 0.0
                    var totalOwedToYou = 0.0
                    val groupBalanceMap = mutableMapOf<String, GroupBalance>()
                    val allSettlements = mutableListOf<Settlement>()

                    // Fetch settlements per group (one-shot per collect cycle)
                    for (group in groups) {
                        val settlements = settlementRepository
                            .getSettlementsForGroup(group.id)
                            .firstOrNull() ?: emptyList()
                        allSettlements += settlements

                        val groupExpenses = allExpenses.filter { it.groupId == group.id }
                        val balance = calculateBalanceUseCase(
                            expenses = groupExpenses,
                            settlements = settlements,
                            members = group.members,
                            groupId = group.id,
                            groupName = group.name
                        )
                        val simplified = simplifyDebtsUseCase(balance.balances, group.members)
                        groupBalanceMap[group.id] = balance.copy(simplifiedDebts = simplified)

                        val userBal = balance.balances.find { it.userId == currentUser.id }
                        if (userBal != null) {
                            when {
                                userBal.amount > 0 -> totalOwedToYou += userBal.amount
                                userBal.amount < 0 -> totalOwed += kotlin.math.abs(userBal.amount)
                            }
                        }
                    }

                    // Build activity feed: merge recent expenses + settlements, sort by date
                    val groupMap = groups.associateBy { it.id }
                    val expenseItems = allExpenses
                        .sortedByDescending { it.date }
                        .take(20)
                        .map { expense ->
                            val group = groupMap[expense.groupId]
                            val payerName = group?.members
                                ?.find { it.id == expense.paidById }?.name ?: "Someone"
                            val isYou = expense.paidById == currentUser.id
                            ActivityFeedItem(
                                id       = expense.id,
                                title    = expense.title,
                                subtitle = "Paid by ${if (isYou) "you" else payerName}",
                                amount   = expense.amount,
                                currency = expense.currency,
                                date     = expense.date,
                                groupName = group?.name ?: "",
                                type     = FeedItemType.EXPENSE
                            )
                        }
                    val settlementItems = allSettlements
                        .sortedByDescending { it.date }
                        .take(10)
                        .map { s ->
                            val fromName = if (s.payerId == currentUser.id) "You" else s.payerName
                            val toName   = if (s.payeeId == currentUser.id) "you" else s.payeeName
                            val group    = groupMap[s.groupId]
                            ActivityFeedItem(
                                id       = s.id,
                                title    = "$fromName paid $toName",
                                subtitle = "Settlement • ${group?.name ?: ""}",
                                amount   = s.amount,
                                currency = s.currency,
                                date     = s.date,
                                groupName = group?.name ?: "",
                                type     = FeedItemType.SETTLEMENT
                            )
                        }
                    val activities = (expenseItems + settlementItems)
                        .sortedByDescending { it.date }
                        .take(15)

                    _uiState.update {
                        it.copy(
                            isLoading       = false,
                            isRefreshing    = false,
                            currentUser     = currentUser,
                            totalOwed       = Math.round(totalOwed * 100.0) / 100.0,
                            totalOwedToYou  = Math.round(totalOwedToYou * 100.0) / 100.0,
                            netBalance      = Math.round((totalOwedToYou - totalOwed) * 100.0) / 100.0,
                            groups          = groups,
                            groupBalances   = groupBalanceMap,
                            recentExpenses  = allExpenses.sortedByDescending { e -> e.date }.take(10),
                            recentActivities = activities,
                            error           = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
