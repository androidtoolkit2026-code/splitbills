package com.splitmate.presentation.screens.group

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.domain.model.*
import com.splitmate.domain.repository.GroupRepository
import com.splitmate.domain.repository.UserRepository
import com.splitmate.domain.usecase.group.CreateGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class GroupListUiState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

data class ContactPerson(
    val id: String,
    val name: String,
    val phone: String
)

data class CreateGroupUiState(
    val name: String = "",
    val description: String = "",
    val type: GroupType = GroupType.OTHER,
    val selectedMembers: List<User> = emptyList(),
    val availableUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val savedGroupId: String? = null,
    val error: String? = null,
    val showContactSheet: Boolean = false,
    val contactSearchQuery: String = "",
    val contactResults: List<ContactPerson> = emptyList(),
    val isLoadingContacts: Boolean = false
)

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { groups ->
                _uiState.update {
                    it.copy(isLoading = false, groups = groups)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
        }
    }
}

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private var allContacts: List<ContactPerson> = emptyList()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                _uiState.update { it.copy(availableUsers = users) }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onTypeChanged(type: GroupType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onMemberToggled(user: User) {
        _uiState.update { state ->
            val current = state.selectedMembers.toMutableList()
            if (current.any { it.id == user.id }) {
                current.removeAll { it.id == user.id }
            } else {
                current.add(user)
            }
            state.copy(selectedMembers = current)
        }
    }

    fun showContactPicker() {
        _uiState.update { it.copy(showContactSheet = true) }
    }

    fun dismissContactPicker() {
        _uiState.update { it.copy(showContactSheet = false, contactSearchQuery = "", contactResults = allContacts) }
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingContacts = true) }
            val contacts = withContext(Dispatchers.IO) {
                val result = mutableListOf<ContactPerson>()
                val seenIds = mutableSetOf<String>()
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        val id = it.getString(idIdx) ?: continue
                        val name = it.getString(nameIdx) ?: continue
                        val phone = it.getString(phoneIdx) ?: continue
                        if (seenIds.add(id)) {
                            result.add(ContactPerson(id, name, phone.trim()))
                        }
                    }
                }
                result
            }
            allContacts = contacts
            _uiState.update { it.copy(isLoadingContacts = false, contactResults = contacts) }
        }
    }

    fun onContactSearchChanged(query: String) {
        val filtered = if (query.isBlank()) allContacts
        else allContacts.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query) }
        _uiState.update { it.copy(contactSearchQuery = query, contactResults = filtered) }
    }

    fun addContactAsMember(contact: ContactPerson) {
        viewModelScope.launch {
            val normalizedPhone = contact.phone.replace(Regex("[^0-9+]"), "")
            val existingUser = _uiState.value.availableUsers.firstOrNull { user ->
                user.phone.replace(Regex("[^0-9+]"), "") == normalizedPhone
            }
            val user = if (existingUser != null) {
                existingUser
            } else {
                val newUser = User(name = contact.name, phone = contact.phone)
                userRepository.createUser(newUser)
                newUser
            }
            onMemberToggled(user)
        }
    }

    fun createGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val state = _uiState.value
            // Include current user in members
            val currentUser = userRepository.getCurrentUser().firstOrNull()
            val allMembers = state.selectedMembers.toMutableList()
            if (currentUser != null && allMembers.none { it.id == currentUser.id }) {
                allMembers.add(0, currentUser)
            }

            val group = Group(
                name = state.name,
                description = state.description,
                type = state.type,
                createdBy = currentUser?.id ?: "",
                members = allMembers
            )

            val result = createGroupUseCase(group)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSaved = true, savedGroupId = group.id) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = result.exceptionOrNull()?.message)
                }
            }
        }
    }
}
