package com.splitmate.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.domain.model.Currency
import com.splitmate.domain.model.User
import com.splitmate.domain.repository.UserRepository
import com.splitmate.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val currency: String = "INR",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            name = user.name,
                            email = user.email,
                            phone = user.phone,
                            currency = user.defaultCurrency
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun onCurrencyChanged(currency: String) {
        _uiState.update { it.copy(currency = currency) }
    }

    fun toggleEditing() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val user = state.user?.copy(
                name = state.name,
                email = state.email,
                phone = state.phone,
                defaultCurrency = state.currency
            ) ?: return@launch

            try {
                userRepository.updateUser(user)
                preferencesManager.setDefaultCurrency(state.currency)
                _uiState.update { it.copy(isEditing = false, user = user, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
