package com.splitmate.presentation.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.domain.model.Expense
import com.splitmate.domain.model.Settlement
import com.splitmate.domain.repository.ExpenseRepository
import com.splitmate.domain.repository.GroupRepository
import com.splitmate.domain.repository.SettlementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val currency: String,
    val date: Long,
    val isSettlement: Boolean = false
)

data class ActivityUiState(
    val isLoading: Boolean = true,
    val activities: List<ActivityItem> = emptyList(),
    val filterGroupId: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        loadActivity()
    }

    private fun loadActivity() {
        viewModelScope.launch {
            combine(
                expenseRepository.getAllExpenses(),
                settlementRepository.getAllSettlements(),
                groupRepository.getAllGroups()
            ) { expenses, settlements, groups ->
                val groupMap = groups.associateBy { it.id }
                val memberMap = groups.flatMap { it.members }.associateBy { it.id }

                val expenseItems = expenses.map { expense ->
                    val payer = memberMap[expense.paidById]
                    val group = groupMap[expense.groupId]
                    ActivityItem(
                        id = expense.id,
                        title = expense.title,
                        subtitle = "${payer?.name ?: "Someone"} paid in ${group?.name ?: "a group"}",
                        amount = expense.amount,
                        currency = expense.currency,
                        date = expense.date,
                        isSettlement = false
                    )
                }

                val settlementItems = settlements.map { settlement ->
                    val payer = memberMap[settlement.payerId]
                    val payee = memberMap[settlement.payeeId]
                    ActivityItem(
                        id = settlement.id,
                        title = "Settlement",
                        subtitle = "${payer?.name ?: "Someone"} paid ${payee?.name ?: "someone"}",
                        amount = settlement.amount,
                        currency = settlement.currency,
                        date = settlement.date,
                        isSettlement = true
                    )
                }

                (expenseItems + settlementItems).sortedByDescending { it.date }
            }.collect { activities ->
                _uiState.update { it.copy(isLoading = false, activities = activities) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
