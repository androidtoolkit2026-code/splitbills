package com.splitmate.presentation.screens.settlement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.domain.model.*
import com.splitmate.domain.repository.ExpenseRepository
import com.splitmate.domain.repository.GroupRepository
import com.splitmate.domain.repository.SettlementRepository
import com.splitmate.domain.repository.UserRepository
import com.splitmate.domain.usecase.balance.CalculateBalanceUseCase
import com.splitmate.domain.usecase.balance.SimplifyDebtsUseCase
import com.splitmate.domain.usecase.settlement.SettleUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettleUpUiState(
    val isLoading: Boolean = true,
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val simplifiedDebts: List<DebtTransaction> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val currentUser: User? = null,
    val myBalance: Double = 0.0,
    // Selection
    val selectedDebt: DebtTransaction? = null,
    val fromUserId: String = "",
    val toUserId: String = "",
    // Form
    val amount: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String = "",
    // Feedback
    val savedMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val userRepository: UserRepository,
    private val calculateBalanceUseCase: CalculateBalanceUseCase,
    private val simplifyDebtsUseCase: SimplifyDebtsUseCase,
    private val settleUpUseCase: SettleUpUseCase
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(SettleUpUiState())
    val uiState: StateFlow<SettleUpUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                combine(
                    groupRepository.getGroupById(groupId),
                    expenseRepository.getExpensesForGroup(groupId),
                    settlementRepository.getSettlementsForGroup(groupId),
                    userRepository.getCurrentUser()
                ) { group, expenses, settlements, currentUser ->
                    SettleData(group, expenses, settlements, currentUser)
                }.collect { data ->
                    val group = data.group ?: return@collect

                    val balance = calculateBalanceUseCase(
                        expenses = data.expenses,
                        settlements = data.settlements,
                        members = group.members,
                        groupId = group.id,
                        groupName = group.name
                    )
                    val simplified = simplifyDebtsUseCase(balance.balances, group.members)
                    val myBalance = balance.balances
                        .find { it.userId == data.currentUser?.id }?.amount ?: 0.0

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            group = group,
                            members = group.members,
                            simplifiedDebts = simplified,
                            settlements = data.settlements,
                            currentUser = data.currentUser,
                            myBalance = myBalance,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /** Tap a suggested debt card to auto-fill the form; tap again to deselect. */
    fun onDebtSelected(debt: DebtTransaction) {
        val isDeselect = _uiState.value.selectedDebt == debt
        _uiState.update {
            it.copy(
                selectedDebt = if (isDeselect) null else debt,
                fromUserId   = if (isDeselect) "" else debt.fromUserId,
                toUserId     = if (isDeselect) "" else debt.toUserId,
                amount       = if (isDeselect) "" else String.format("%.2f", debt.amount)
            )
        }
    }

    /** Select the payer manually; clears any debt pre-selection. */
    fun onFromUserSelected(userId: String) {
        _uiState.update {
            it.copy(
                fromUserId   = if (it.fromUserId == userId) "" else userId,
                selectedDebt = null
            )
        }
    }

    /** Select the payee manually; clears any debt pre-selection. */
    fun onToUserSelected(userId: String) {
        _uiState.update {
            it.copy(
                toUserId     = if (it.toUserId == userId) "" else userId,
                selectedDebt = null
            )
        }
    }

    fun onAmountChanged(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(amount = amount) }
        }
    }

    fun onPaymentMethodChanged(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun settleUp() {
        viewModelScope.launch {
            val state = _uiState.value
            val fromUserId = state.fromUserId
            val toUserId   = state.toUserId

            if (fromUserId.isBlank() || toUserId.isBlank()) {
                _uiState.update { it.copy(error = "Please select who pays and who receives") }
                return@launch
            }
            if (fromUserId == toUserId) {
                _uiState.update { it.copy(error = "Payer and receiver must be different") }
                return@launch
            }
            val amountVal = state.amount.toDoubleOrNull()
            if (amountVal == null || amountVal <= 0) {
                _uiState.update { it.copy(error = "Please enter a valid amount") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            val fromUser = state.members.find { it.id == fromUserId }
            val toUser   = state.members.find { it.id == toUserId }

            val settlement = Settlement(
                groupId       = groupId,
                payerId       = fromUserId,
                payeeId       = toUserId,
                amount        = amountVal,
                currency      = state.group?.currency ?: "INR",
                paymentMethod = state.paymentMethod,
                notes         = state.notes,
                payerName     = fromUser?.name ?: "",
                payeeName     = toUser?.name ?: ""
            )

            val result = settleUpUseCase(settlement)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading     = false,
                        selectedDebt  = null,
                        fromUserId    = "",
                        toUserId      = "",
                        amount        = "",
                        notes         = "",
                        paymentMethod = PaymentMethod.CASH,
                        savedMessage  = "Settlement recorded!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to record settlement"
                    )
                }
            }
        }
    }

    /** Deletes a settlement; reactive flow automatically recalculates balances. */
    fun deleteSettlement(id: String) {
        viewModelScope.launch {
            settlementRepository.deleteSettlement(id)
        }
    }

    fun clearSavedMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private data class SettleData(
        val group: Group?,
        val expenses: List<Expense>,
        val settlements: List<Settlement>,
        val currentUser: User?
    )
}
