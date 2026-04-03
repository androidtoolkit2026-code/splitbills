package com.splitmate.presentation.screens.group

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val isLoading: Boolean = true,
    val group: Group? = null,
    val expenses: List<Expense> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val groupBalance: GroupBalance? = null,
    val currentUser: User? = null,
    val error: String? = null,
    val allUsers: List<User> = emptyList(),
    val showAddMemberSheet: Boolean = false,
    val memberError: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val userRepository: UserRepository,
    private val calculateBalanceUseCase: CalculateBalanceUseCase,
    private val simplifyDebtsUseCase: SimplifyDebtsUseCase
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadGroupDetail()
        loadAllUsers()
    }

    fun loadGroupDetail() {
        viewModelScope.launch {
            try {
                combine(
                    groupRepository.getGroupById(groupId),
                    expenseRepository.getExpensesForGroup(groupId),
                    settlementRepository.getSettlementsForGroup(groupId),
                    userRepository.getCurrentUser()
                ) { group, expenses, settlements, currentUser ->
                    GroupDetailData(group, expenses, settlements, currentUser)
                }.collect { data ->
                    val group = data.group
                    if (group != null) {
                        val balance = calculateBalanceUseCase(
                            expenses = data.expenses,
                            settlements = data.settlements,
                            members = group.members,
                            groupId = group.id,
                            groupName = group.name
                        )
                        val simplified = simplifyDebtsUseCase(balance.balances, group.members)
                        val fullBalance = balance.copy(simplifiedDebts = simplified)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                group = group,
                                expenses = data.expenses,
                                settlements = data.settlements,
                                groupBalance = fullBalance,
                                currentUser = data.currentUser,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
        }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
        }
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                _uiState.update { it.copy(allUsers = users) }
            }
        }
    }

    fun showAddMemberSheet() {
        _uiState.update { it.copy(showAddMemberSheet = true, memberError = null) }
    }

    fun dismissAddMemberSheet() {
        _uiState.update { it.copy(showAddMemberSheet = false, memberError = null) }
    }

    fun addMember(user: User) {
        val group = _uiState.value.group ?: return
        if (group.members.any { it.id == user.id }) {
            _uiState.update { it.copy(memberError = "${user.name} is already in this group") }
            return
        }
        viewModelScope.launch {
            try {
                groupRepository.addMemberToGroup(groupId, user.id)
                // UI updates reactively via the combined flow
            } catch (e: Exception) {
                _uiState.update { it.copy(memberError = e.message ?: "Failed to add member") }
            }
        }
    }

    fun removeMember(userId: String) {
        val currentUserId = _uiState.value.currentUser?.id
        if (userId == currentUserId) {
            _uiState.update { it.copy(memberError = "You cannot remove yourself from the group") }
            return
        }
        viewModelScope.launch {
            try {
                groupRepository.removeMemberFromGroup(groupId, userId)
                // UI updates reactively via the combined flow
            } catch (e: Exception) {
                _uiState.update { it.copy(memberError = e.message ?: "Failed to remove member") }
            }
        }
    }

    fun clearMemberError() {
        _uiState.update { it.copy(memberError = null) }
    }

    private data class GroupDetailData(
        val group: Group?,
        val expenses: List<Expense>,
        val settlements: List<Settlement>,
        val currentUser: User?
    )
}
