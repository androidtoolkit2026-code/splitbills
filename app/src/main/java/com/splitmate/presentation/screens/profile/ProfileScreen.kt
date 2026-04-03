package com.splitmate.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.splitmate.domain.model.Currency
import com.splitmate.presentation.components.AvatarCircle
import com.splitmate.presentation.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar & Name
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarCircle(
                    name = uiState.name,
                    size = 80,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = uiState.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.email.isNotBlank()) {
                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Edit Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (uiState.isEditing) {
                    OutlinedButton(onClick = viewModel::toggleEditing) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = viewModel::saveProfile) {
                        Text("Save")
                    }
                } else {
                    OutlinedButton(onClick = viewModel::toggleEditing) {
                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit Profile")
                    }
                }
            }
        }

        // Fields
        item {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Name") },
                enabled = uiState.isEditing,
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                enabled = uiState.isEditing,
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChanged,
                label = { Text("Phone") },
                enabled = uiState.isEditing,
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Currency Picker
        item {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (uiState.isEditing) expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = Currency.fromCode(uiState.currency).let { "${it.symbol} ${it.name}" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Default Currency") },
                    leadingIcon = { Icon(Icons.Default.CurrencyExchange, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = uiState.isEditing
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Currency.SUPPORTED.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text("${currency.symbol} ${currency.name} (${currency.code})") },
                            onClick = {
                                viewModel.onCurrencyChanged(currency.code)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Error
        if (uiState.error != null) {
            item {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
