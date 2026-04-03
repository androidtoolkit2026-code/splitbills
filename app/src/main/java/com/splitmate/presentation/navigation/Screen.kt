package com.splitmate.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Groups : Screen("groups")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
    object CreateGroup : Screen("create_group")
    object EditGroup : Screen("edit_group/{groupId}") {
        fun createRoute(groupId: String) = "edit_group/$groupId"
    }
    object AddExpense : Screen("add_expense/{groupId}") {
        fun createRoute(groupId: String) = "add_expense/$groupId"
    }
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: String) = "edit_expense/$expenseId"
    }
    object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_detail/$expenseId"
    }
    object SettleUp : Screen("settle_up/{groupId}") {
        fun createRoute(groupId: String) = "settle_up/$groupId"
    }
    object Activity : Screen("activity")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object BackupExport : Screen("backup_export")
}
