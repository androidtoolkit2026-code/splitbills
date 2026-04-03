package com.splitmate.presentation.screens.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.splitmate.presentation.components.*
import com.splitmate.presentation.theme.NegativeRed
import com.splitmate.presentation.theme.PositiveGreen
import com.splitmate.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenseDetail: (String) -> Unit,
    onNavigateToSettleUp: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Group") },
            text = { Text("This will delete the group and all its expenses. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGroup()
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.name ?: "Group") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = onNavigateToSettleUp,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Handshake, contentDescription = "Settle Up")
                }
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddExpense,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Expense") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingScreen(Modifier.padding(padding))
            return@Scaffold
        }

        val group = uiState.group ?: return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Group Info Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GroupTypeIcon(group.type, Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (group.description.isNotBlank()) {
                                    Text(
                                        text = group.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Total Expenses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                                Text(
                                    FormatUtils.formatCurrency(group.totalExpenses),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Members",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                                Text(
                                    "${group.members.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Tabs
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Expenses") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Balances") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Members") }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Expenses Tab
                    if (uiState.expenses.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.Receipt,
                                title = "No expenses yet",
                                subtitle = "Add your first expense"
                            )
                        }
                    } else {
                        items(uiState.expenses, key = { it.id }) { expense ->
                            val payerName = group.members.find { it.id == expense.paidById }?.name ?: "Unknown"
                            ExpenseListItem(
                                title = expense.title,
                                amount = expense.amount,
                                paidByName = payerName,
                                date = expense.date,
                                currency = expense.currency,
                                onClick = { onNavigateToExpenseDetail(expense.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                1 -> {
                    // Balances Tab
                    val balance = uiState.groupBalance
                    val currentUser = uiState.currentUser
                    val myBalance = balance?.balances?.find { it.userId == currentUser?.id }

                    // Personal summary card
                    if (myBalance != null) {
                        item {
                            val isOwing = myBalance.amount < -0.01
                            val isOwed = myBalance.amount > 0.01
                            val summaryColor = when {
                                isOwing -> NegativeRed
                                isOwed -> PositiveGreen
                                else -> MaterialTheme.colorScheme.primary
                            }
                            val summaryBg = when {
                                isOwing -> NegativeRed.copy(alpha = 0.1f)
                                isOwed -> PositiveGreen.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val summaryText = when {
                                isOwing -> "You owe ${FormatUtils.formatCurrency(Math.abs(myBalance.amount))} total"
                                isOwed -> "You are owed ${FormatUtils.formatCurrency(myBalance.amount)} total"
                                else -> "You're all settled up"
                            }
                            val summaryIcon = when {
                                isOwing -> Icons.Default.ArrowUpward
                                isOwed -> Icons.Default.ArrowDownward
                                else -> Icons.Default.CheckCircle
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = summaryBg)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        summaryIcon,
                                        contentDescription = null,
                                        tint = summaryColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = summaryText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = summaryColor
                                    )
                                }
                            }
                        }
                    }

                    if (balance != null) {
                        item {
                            SectionHeader(title = "MEMBER BALANCES")
                        }
                        items(balance.balances.filter { it.amount != 0.0 }) { bal ->
                            val isMe = bal.userId == currentUser?.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarCircle(name = bal.userName)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isMe) "You" else bal.userName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = when {
                                                isMe -> "your balance"
                                                bal.amount < 0 -> "owes money"
                                                else -> "is owed money"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    BalanceText(
                                        amount = bal.amount,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }

                        if (balance.simplifiedDebts.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "SIMPLIFIED DEBTS",
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                            items(balance.simplifiedDebts) { debt ->
                                val isFromMe = debt.fromUserId == currentUser?.id
                                val isToMe = debt.toUserId == currentUser?.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isFromMe -> NegativeRed.copy(alpha = 0.08f)
                                            isToMe -> PositiveGreen.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarCircle(name = debt.fromUserName, size = 36)
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isFromMe) "You" else debt.fromUserName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isFromMe) FontWeight.Bold else FontWeight.Medium
                                            )
                                            Text(
                                                text = "pays ${if (isToMe) "you" else debt.toUserName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = FormatUtils.formatCurrency(debt.amount),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isFromMe -> NegativeRed
                                                isToMe -> PositiveGreen
                                                else -> NegativeRed
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (balance?.balances?.all { it.amount == 0.0 } == true) {
                        item {
                            EmptyState(
                                icon = Icons.Default.CheckCircle,
                                title = "All settled up!",
                                subtitle = "No outstanding balances"
                            )
                        }
                    }
                }

                2 -> {
                    // Members Tab — header row with Add Member button
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${group.members.size} member${if (group.members.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FilledTonalButton(onClick = { viewModel.showAddMemberSheet() }) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Add Member")
                            }
                        }
                    }

                    // Member error banner
                    if (uiState.memberError != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
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
                                        text = uiState.memberError!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = viewModel::clearMemberError) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(group.members, key = { it.id }) { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarCircle(name = member.name)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (member.id == uiState.currentUser?.id) {
                                        Text(
                                            text = "You",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else if (member.email.isNotBlank()) {
                                        Text(
                                            text = member.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                // Can remove any member except yourself
                                if (member.id != uiState.currentUser?.id) {
                                    IconButton(onClick = { viewModel.removeMember(member.id) }) {
                                        Icon(
                                            Icons.Default.PersonRemove,
                                            contentDescription = "Remove ${member.name}",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Member bottom sheet
        if (uiState.showAddMemberSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val availableToAdd = uiState.allUsers.filter { user ->
                group.members.none { it.id == user.id }
            }
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissAddMemberSheet() },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Add Member",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    Divider()
                    if (availableToAdd.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "All users are already in this group",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        availableToAdd.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addMember(user)
                                        viewModel.dismissAddMemberSheet()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarCircle(name = user.name)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (user.email.isNotBlank()) {
                                        Text(
                                            text = user.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add ${user.name}",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}
