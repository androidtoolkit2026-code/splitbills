package com.splitmate.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.splitmate.presentation.components.*
import com.splitmate.presentation.theme.NegativeRed
import com.splitmate.presentation.theme.PositiveGreen
import com.splitmate.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToGroup: (String) -> Unit,
    onNavigateToAddExpense: (String) -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SplitMate") },
                actions = {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = onNavigateToCreateGroup,
                icon           = { Icon(Icons.Default.Add, contentDescription = null) },
                text           = { Text("New Group") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {

            // ── Hero header ──────────────────────────────────────────────
            item {
                HeroHeader(
                    userName   = uiState.currentUser?.name,
                    netBalance = uiState.netBalance
                )
            }

            // ── Owe / Owed 2-column cards ────────────────────────────────
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BalanceSummaryCard(
                        label    = "You Owe",
                        amount   = uiState.totalOwed,
                        icon     = Icons.Default.ArrowUpward,
                        color    = NegativeRed,
                        modifier = Modifier.weight(1f)
                    )
                    BalanceSummaryCard(
                        label    = "You\u2019re Owed",
                        amount   = uiState.totalOwedToYou,
                        icon     = Icons.Default.ArrowDownward,
                        color    = PositiveGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Error banner ─────────────────────────────────────────────
            if (uiState.error != null) {
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
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text     = uiState.error!!,
                                color    = MaterialTheme.colorScheme.onErrorContainer,
                                style    = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = viewModel::clearError) {
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

            // ── Groups section ───────────────────────────────────────────
            if (uiState.groups.isNotEmpty()) {
                item {
                    SectionHeader(
                        title    = "YOUR GROUPS",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Horizontal quick-access chips
                item {
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier              = Modifier.padding(bottom = 4.dp)
                    ) {
                        items(uiState.groups.take(6)) { group ->
                            val userBal = uiState.groupBalances[group.id]
                                ?.balances?.find { it.userId == uiState.currentUser?.id }
                            GroupChipCard(
                                groupName   = group.name,
                                groupType   = group.type,
                                memberCount = group.members.size,
                                userBalance = userBal?.amount,
                                onClick     = { onNavigateToGroup(group.id) }
                            )
                        }
                    }
                }

                // Vertical full cards for all groups
                items(uiState.groups, key = { it.id }) { group ->
                    val userBal = uiState.groupBalances[group.id]
                        ?.balances?.find { it.userId == uiState.currentUser?.id }
                    GroupSummaryCard(
                        group        = group,
                        userBalance  = userBal?.amount ?: 0.0,
                        onClick      = { onNavigateToGroup(group.id) },
                        onAddExpense = { onNavigateToAddExpense(group.id) }
                    )
                }
            }

            // ── Recent Activity ──────────────────────────────────────────
            if (uiState.recentActivities.isNotEmpty()) {
                item {
                    SectionHeader(
                        title    = "RECENT ACTIVITY",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(uiState.recentActivities, key = { it.id }) { item ->
                    ActivityFeedCard(item = item)
                }
            }

            // ── Empty state ──────────────────────────────────────────────
            if (uiState.groups.isEmpty()) {
                item {
                    EmptyState(
                        icon     = Icons.Default.GroupAdd,
                        title    = "No groups yet",
                        subtitle = "Tap \u2019New Group\u2019 to start splitting expenses",
                        modifier = Modifier.padding(top = 48.dp),
                        action   = {
                            Button(onClick = onNavigateToCreateGroup) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Create First Group")
                            }
                        }
                    )
                }
            }
        }
    }
}

// ── Sub-composables ─────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(
    userName: String?,
    netBalance: Double
) {
    val isPositive = netBalance >= 0
    val netColor   = if (isPositive) PositiveGreen else NegativeRed
    val netLabel   = when {
        netBalance >  0.01 -> "You are up overall"
        netBalance < -0.01 -> "You are down overall"
        else               -> "All settled up!"
    }

    Surface(
        modifier       = Modifier.fillMaxWidth(),
        color          = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text       = "Hi, ${userName ?: "there"} \uD83D\uDC4B",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Here\u2019s your expense summary",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))
            // Net balance pill
            Surface(
                shape    = MaterialTheme.shapes.extraLarge,
                color    = netColor.copy(alpha = 0.15f),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint               = netColor,
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        text       = "Net: ${if (netBalance < 0) "-" else "+"}${FormatUtils.formatCurrency(Math.abs(netBalance))}",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = netColor
                    )
                    Text(
                        text  = "\u2022 $netLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = netColor.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceSummaryCard(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text       = FormatUtils.formatCurrency(amount),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupChipCard(
    groupName: String,
    groupType: com.splitmate.domain.model.GroupType,
    memberCount: Int,
    userBalance: Double?,
    onClick: () -> Unit
) {
    val hasBalance = userBalance != null && kotlin.math.abs(userBalance) > 0.01
    val balColor   = if ((userBalance ?: 0.0) >= 0) PositiveGreen else NegativeRed

    Card(
        onClick   = onClick,
        modifier  = Modifier.width(130.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier              = Modifier.padding(12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(4.dp)
        ) {
            GroupTypeIcon(groupType, Modifier.size(26.dp))
            Text(
                text       = groupName,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text  = "$memberCount members",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (hasBalance) {
                Text(
                    text       = "${if ((userBalance ?: 0.0) >= 0) "+" else ""}${FormatUtils.formatCurrency(Math.abs(userBalance ?: 0.0))}",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = balColor
                )
            } else {
                Text(
                    text  = "Settled",
                    style = MaterialTheme.typography.labelSmall,
                    color = PositiveGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupSummaryCard(
    group: com.splitmate.domain.model.Group,
    userBalance: Double,
    onClick: () -> Unit,
    onAddExpense: () -> Unit
) {
    val hasBalance = kotlin.math.abs(userBalance) > 0.01
    val balColor   = when {
        userBalance >  0.01 -> PositiveGreen
        userBalance < -0.01 -> NegativeRed
        else                -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in a tinted container
            Surface(
                shape    = MaterialTheme.shapes.medium,
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    GroupTypeIcon(group.type, Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = group.name,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text  = "${group.members.size} member${if (group.members.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (group.totalExpenses > 0) {
                        Text("\u2022", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                        Text(
                            text  = FormatUtils.formatCurrency(group.totalExpenses),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (hasBalance) {
                    Text(
                        text       = "${if (userBalance >= 0) "+" else ""}${FormatUtils.formatCurrency(Math.abs(userBalance))}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = balColor
                    )
                    Text(
                        text  = if (userBalance > 0) "you get" else "you owe",
                        style = MaterialTheme.typography.labelSmall,
                        color = balColor.copy(alpha = 0.75f)
                    )
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = PositiveGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text     = "Settled",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = PositiveGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityFeedCard(item: ActivityFeedItem) {
    val isSett  = item.type == FeedItemType.SETTLEMENT
    val icon    = if (isSett) Icons.Default.Handshake else Icons.Default.Receipt
    val iconBg  = if (isSett) PositiveGreen.copy(alpha = 0.12f)
                  else MaterialTheme.colorScheme.primaryContainer
    val iconTint = if (isSett) PositiveGreen else MaterialTheme.colorScheme.primary

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = MaterialTheme.shapes.medium, color = iconBg) {
                Box(
                    modifier         = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = iconTint,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text  = FormatUtils.formatRelativeDate(item.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (item.groupName.isNotBlank()) {
                        Text(
                            text  = "\u2022",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text     = item.groupName,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text       = FormatUtils.formatCurrency(item.amount, item.currency),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = if (isSett) PositiveGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
