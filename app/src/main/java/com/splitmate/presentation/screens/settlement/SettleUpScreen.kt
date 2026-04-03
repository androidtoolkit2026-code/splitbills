package com.splitmate.presentation.screens.settlement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.splitmate.domain.model.PaymentMethod
import com.splitmate.domain.model.Settlement
import com.splitmate.presentation.components.*
import com.splitmate.presentation.theme.NegativeRed
import com.splitmate.presentation.theme.PositiveGreen
import com.splitmate.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    viewModel: SettleUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedMessage) {
        if (uiState.savedMessage != null) {
            snackbarHostState.showSnackbar(uiState.savedMessage!!)
            viewModel.clearSavedMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settle Up") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading && uiState.members.isEmpty()) {
            LoadingScreen(Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Personal balance summary ─────────────────────────────────────
            val myBalance = uiState.myBalance
            if (myBalance != 0.0) {
                item {
                    val isOwing = myBalance < -0.01
                    val color   = if (isOwing) NegativeRed else PositiveGreen
                    val bg      = color.copy(alpha = 0.1f)
                    val icon    = if (isOwing) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                    val text    = if (isOwing)
                        "You owe ${FormatUtils.formatCurrency(Math.abs(myBalance))} in this group"
                    else
                        "You are owed ${FormatUtils.formatCurrency(myBalance)} in this group"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = bg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = color,
                                modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = color
                            )
                        }
                    }
                }
            }

            // ── Suggested settlements ────────────────────────────────────────
            if (uiState.simplifiedDebts.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "SUGGESTED SETTLEMENTS",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(uiState.simplifiedDebts) { debt ->
                    val isSelected = uiState.selectedDebt == debt
                    val isFromMe   = debt.fromUserId == uiState.currentUser?.id
                    val isToMe     = debt.toUserId   == uiState.currentUser?.id

                    Card(
                        onClick = { viewModel.onDebtSelected(debt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                isFromMe   -> NegativeRed.copy(alpha = 0.07f)
                                isToMe     -> PositiveGreen.copy(alpha = 0.07f)
                                else       -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarCircle(
                                name = if (isFromMe) "You" else debt.fromUserName,
                                size = 40
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isFromMe) "You" else debt.fromUserName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isFromMe) FontWeight.Bold else FontWeight.Medium
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "pays ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = if (isToMe) "you" else debt.toUserName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isToMe) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                            Text(
                                text = FormatUtils.formatCurrency(debt.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NegativeRed
                            )
                            if (isSelected) {
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Record Payment form ──────────────────────────────────────────
            item {
                SectionHeader(
                    title = "RECORD PAYMENT",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Paying: member picker
            item {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)) {
                    Text(
                        text = "Paying",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.members) { member ->
                            val isMe       = member.id == uiState.currentUser?.id
                            val isSelected = member.id == uiState.fromUserId
                            FilterChip(
                                selected  = isSelected,
                                onClick   = { viewModel.onFromUserSelected(member.id) },
                                label     = { Text(if (isMe) "You" else member.name) },
                                leadingIcon = if (isSelected) ({
                                    Icon(Icons.Default.Check, null,
                                        modifier = Modifier.size(16.dp))
                                }) else null
                            )
                        }
                    }
                }
            }

            // Receiving: member picker
            item {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Receiving",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.members) { member ->
                            val isMe       = member.id == uiState.currentUser?.id
                            val isSelected = member.id == uiState.toUserId
                            FilterChip(
                                selected  = isSelected,
                                onClick   = { viewModel.onToUserSelected(member.id) },
                                label     = { Text(if (isMe) "You" else member.name) },
                                leadingIcon = if (isSelected) ({
                                    Icon(Icons.Default.Check, null,
                                        modifier = Modifier.size(16.dp))
                                }) else null
                            )
                        }
                    }
                }
            }

            // Amount
            item {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChanged,
                    label = { Text("Amount") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // Payment method
            item {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val methods = listOf(
                            PaymentMethod.CASH to "Cash",
                            PaymentMethod.UPI   to "UPI",
                            PaymentMethod.BANK  to "Bank Transfer"
                        )
                        items(methods) { (method, label) ->
                            FilterChip(
                                selected = uiState.paymentMethod == method,
                                onClick  = { viewModel.onPaymentMethodChanged(method) },
                                label    = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Notes
            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    label = { Text("Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // Error banner
            if (uiState.error != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = viewModel::clearError) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // Record Settlement button
            item {
                val canSubmit = uiState.fromUserId.isNotBlank()
                    && uiState.toUserId.isNotBlank()
                    && uiState.amount.isNotBlank()
                    && !uiState.isLoading

                Button(
                    onClick = viewModel::settleUp,
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Handshake, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Record Settlement")
                    }
                }
            }

            // ── Settlement history ───────────────────────────────────────────
            if (uiState.settlements.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "SETTLEMENT HISTORY",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(uiState.settlements, key = { it.id }) { settlement ->
                    SettlementHistoryCard(
                        settlement    = settlement,
                        currentUserId = uiState.currentUser?.id ?: "",
                        onDelete      = { viewModel.deleteSettlement(settlement.id) }
                    )
                }
            }

            // Empty state when no debts and no history
            if (uiState.simplifiedDebts.isEmpty() && uiState.settlements.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyState(
                        icon     = Icons.Default.CheckCircle,
                        title    = "All settled up!",
                        subtitle = "No outstanding debts in this group"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettlementHistoryCard(
    settlement: Settlement,
    currentUserId: String,
    onDelete: () -> Unit
) {
    val isFromMe = settlement.payerId == currentUserId
    val isToMe   = settlement.payeeId == currentUserId
    val accentColor = when {
        isFromMe -> NegativeRed
        isToMe   -> PositiveGreen
        else     -> MaterialTheme.colorScheme.onSurface
    }
    val methodLabel = when (settlement.paymentMethod) {
        PaymentMethod.CASH -> "Cash"
        PaymentMethod.UPI  -> "UPI"
        PaymentMethod.BANK -> "Bank Transfer"
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Settlement") },
            text  = { Text("Remove this settlement record? Balances will be recalculated.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarCircle(
                name = if (isFromMe) "You" else settlement.payerName,
                size = 40
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isFromMe) "You" else settlement.payerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isFromMe) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = " → ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (isToMe) "you" else settlement.payeeName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isToMe) FontWeight.Bold else FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = FormatUtils.formatDate(settlement.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = methodLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (settlement.notes.isNotBlank()) {
                    Text(
                        text = settlement.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatCurrency(settlement.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete settlement",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
