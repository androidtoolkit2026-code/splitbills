package com.splitmate.presentation.screens.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.domain.model.*
import com.splitmate.domain.repository.ExpenseRepository
import com.splitmate.domain.repository.GroupRepository
import com.splitmate.domain.repository.UserRepository
import com.splitmate.domain.usecase.expense.AddExpenseUseCase
import com.splitmate.domain.usecase.expense.CalculateSplitsUseCase
import com.splitmate.domain.usecase.expense.ValidateExpenseSplitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val paidById: String = "",
    val splitType: SplitType = SplitType.EQUAL,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val showDatePicker: Boolean = false,
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val selectedMembers: List<String> = emptyList(),
    val exactAmounts: Map<String, String> = emptyMap(),
    val percentages: Map<String, String> = emptyMap(),
    val shares: Map<String, String> = emptyMap(),
    val splitValidation: ValidateExpenseSplitsUseCase.ValidationResult? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val calculateSplitsUseCase: CalculateSplitsUseCase,
    private val validateExpenseSplitsUseCase: ValidateExpenseSplitsUseCase,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        loadGroup()
        viewModelScope.launch {
            _uiState
                .map { s -> Triple(s.amount, s.splitType, Triple(s.selectedMembers, s.exactAmounts, s.percentages)) }
                .distinctUntilChanged()
                .collect { (amountStr, splitType, rest) ->
                    val (selectedMembers, exactAmounts, percentages) = rest
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && selectedMembers.isNotEmpty()) {
                        val validation = validateExpenseSplitsUseCase(
                            totalAmount = amount,
                            selectedMembers = selectedMembers,
                            splitType = splitType,
                            exactAmounts = exactAmounts.mapValues { it.value.toDoubleOrNull() ?: 0.0 },
                            percentages = percentages.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
                        )
                        _uiState.update { it.copy(splitValidation = validation) }
                    } else {
                        _uiState.update { it.copy(splitValidation = null) }
                    }
                }
        }
    }

    private fun loadGroup() {
        viewModelScope.launch {
            combine(
                groupRepository.getGroupById(groupId),
                userRepository.getCurrentUser()
            ) { group, currentUser -> Pair(group, currentUser) }
            .collect { (group, currentUser) ->
                if (group != null) {
                    val memberIds = group.members.map { it.id }
                    _uiState.update {
                        it.copy(
                            group = group,
                            members = group.members,
                            paidById = currentUser?.id ?: group.members.firstOrNull()?.id ?: "",
                            selectedMembers = memberIds,
                            exactAmounts = memberIds.associateWith { "" },
                            percentages = memberIds.associateWith { "" },
                            shares = memberIds.associateWith { "1" }
                        )
                    }
                }
            }
        }
    }

    fun onTitleChanged(title: String) = _uiState.update { it.copy(title = title, error = null) }

    fun onAmountChanged(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d{0,8}\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(amount = amount, error = null) }
        }
    }

    fun onPaidByChanged(userId: String) = _uiState.update { it.copy(paidById = userId) }

    fun onSplitTypeChanged(splitType: SplitType) = _uiState.update { it.copy(splitType = splitType, error = null) }

    fun onNotesChanged(notes: String) = _uiState.update { it.copy(notes = notes) }

    fun onDateChanged(dateMs: Long) = _uiState.update { it.copy(date = dateMs, showDatePicker = false) }

    fun showDatePicker() = _uiState.update { it.copy(showDatePicker = true) }

    fun dismissDatePicker() = _uiState.update { it.copy(showDatePicker = false) }

    fun onMemberToggled(userId: String) {
        _uiState.update { state ->
            val current = state.selectedMembers.toMutableList()
            if (current.contains(userId)) {
                if (current.size > 1) current.remove(userId)
            } else {
                current.add(userId)
            }
            state.copy(selectedMembers = current, error = null)
        }
    }

    fun onExactAmountChanged(userId: String, amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d{0,8}\\.?\\d{0,2}$"))) {
            _uiState.update { state -> state.copy(exactAmounts = state.exactAmounts + (userId to amount), error = null) }
        }
    }

    fun onPercentageChanged(userId: String, pct: String) {
        if (pct.isEmpty() || pct.matches(Regex("^\\d{0,3}\\.?\\d{0,2}$"))) {
            _uiState.update { state -> state.copy(percentages = state.percentages + (userId to pct), error = null) }
        }
    }

    fun onSharesChanged(userId: String, share: String) {
        if (share.isEmpty() || share.matches(Regex("^\\d{0,4}$"))) {
            _uiState.update { state -> state.copy(shares = state.shares + (userId to share), error = null) }
        }
    }

    fun saveExpense() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val amount = state.amount.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Please enter a valid amount")
                if (state.title.isBlank()) throw IllegalArgumentException("Title cannot be empty")
                if (state.paidById.isBlank()) throw IllegalArgumentException("Please select who paid")
                if (state.selectedMembers.isEmpty()) throw IllegalArgumentException("Select at least one member to split with")

                val expense = Expense(
                    groupId = groupId,
                    title = state.title.trim(),
                    amount = amount,
                    currency = state.group?.currency ?: "INR",
                    paidById = state.paidById,
                    splitType = state.splitType,
                    notes = state.notes.trim(),
                    date = state.date
                )

                val splits = calculateSplitsUseCase(
                    amount = amount,
                    memberIds = state.selectedMembers,
                    splitType = state.splitType,
                    exactAmounts = state.exactAmounts.mapValues { it.value.toDoubleOrNull() ?: 0.0 },
                    percentages = state.percentages.mapValues { it.value.toDoubleOrNull() ?: 0.0 },
                    shares = state.shares.mapValues { it.value.toIntOrNull() ?: 1 },
                    expenseId = expense.id
                )

                val result = addExpenseUseCase(expense, splits)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

data class ExpenseDetailUiState(
    val isLoading: Boolean = true,
    val expense: Expense? = null,
    val paidByName: String = "",
    val groupName: String = "",
    val error: String? = null
)

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val expenseId: String = savedStateHandle.get<String>("expenseId") ?: ""

    private val _uiState = MutableStateFlow(ExpenseDetailUiState())
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

    init {
        loadExpense()
    }

    private fun loadExpense() {
        viewModelScope.launch {
            expenseRepository.getExpenseById(expenseId).collect { expense ->
                if (expense != null) {
                    val payer = userRepository.getUserById(expense.paidById)
                    val group = groupRepository.getGroupById(expense.groupId).firstOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            expense = expense,
                            paidByName = payer?.name ?: "Unknown",
                            groupName = group?.name ?: "Unknown"
                        )
                    }
                }
            }
        }
    }

    fun deleteExpense() {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
        }
    }
}
