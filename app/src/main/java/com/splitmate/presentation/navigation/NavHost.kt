package com.splitmate.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.splitmate.presentation.screens.activity.ActivityScreen
import com.splitmate.presentation.screens.backup.BackupExportScreen
import com.splitmate.presentation.screens.dashboard.DashboardScreen
import com.splitmate.presentation.screens.expense.AddExpenseScreen
import com.splitmate.presentation.screens.expense.ExpenseDetailScreen
import com.splitmate.presentation.screens.group.CreateGroupScreen
import com.splitmate.presentation.screens.group.GroupDetailScreen
import com.splitmate.presentation.screens.group.GroupListScreen
import com.splitmate.presentation.screens.onboarding.OnboardingScreen
import com.splitmate.presentation.screens.profile.ProfileScreen
import com.splitmate.presentation.screens.settings.SettingsScreen
import com.splitmate.presentation.screens.settlement.SettleUpScreen

@Composable
fun SplitMateNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToGroup = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onNavigateToAddExpense = { groupId ->
                    navController.navigate(Screen.AddExpense.createRoute(groupId))
                },
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route)
                }
            )
        }

        composable(Screen.Groups.route) {
            GroupListScreen(
                onNavigateToGroup = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route)
                }
            )
        }

        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.createRoute(groupId))
                },
                onNavigateToExpenseDetail = { expenseId ->
                    navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                },
                onNavigateToSettleUp = {
                    navController.navigate(Screen.SettleUp.createRoute(groupId))
                }
            )
        }

        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                onNavigateBack = { navController.popBackStack() },
                onGroupCreated = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId)) {
                        popUpTo(Screen.CreateGroup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            AddExpenseScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@composable
            ExpenseDetailScreen(
                expenseId = expenseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SettleUp.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            SettleUpScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Activity.route) {
            ActivityScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToBackup = {
                    navController.navigate(Screen.BackupExport.route)
                }
            )
        }

        composable(Screen.BackupExport.route) {
            BackupExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
