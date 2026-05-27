package com.example.voting.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.voting.presentation.auth.AuthScreen
import com.example.voting.presentation.auth.AuthViewModel
import com.example.voting.presentation.polls.CreatePollScreen
import com.example.voting.presentation.polls.EditPollScreen
import com.example.voting.presentation.polls.PollListScreen
import com.example.voting.presentation.polls.PollListViewModel
import com.example.voting.presentation.results.ResultsScreen
import com.example.voting.presentation.voting.VotingScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object PollList : Screen("poll_list")
    object CreatePoll : Screen("create_poll")
    object EditPoll : Screen("edit_poll/{pollId}") {
        fun pass(pollId: Int) = "edit_poll/$pollId"
    }
    object Voting : Screen("voting/{pollId}") {
        fun pass(pollId: Int) = "voting/$pollId"
    }
    object Results : Screen("results/{pollId}") {
        fun pass(pollId: Int) = "results/$pollId"
    }
}

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    pollListViewModel: PollListViewModel,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) Screen.PollList.route else Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onSuccess = {
                    pollListViewModel.refreshUserEmail()
                    pollListViewModel.loadPolls()
                    navController.navigate(Screen.PollList.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PollList.route) {
            PollListScreen(
                viewModel = pollListViewModel,
                onCreateClick = {
                    navController.navigate(Screen.CreatePoll.route)
                },
                onVoteClick = { pollId ->
                    navController.navigate(Screen.Voting.pass(pollId))
                },
                onResultsClick = { pollId ->
                    navController.navigate(Screen.Results.pass(pollId))
                },
                onEditClick = { pollId ->
                    navController.navigate(Screen.EditPoll.pass(pollId))
                },
                onToggleTheme = onToggleTheme,
                onLogout = {
                    pollListViewModel.clearPolls()
                    authViewModel.logout {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.CreatePoll.route) {
            CreatePollScreen(
                viewModel = pollListViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditPoll.route,
            arguments = listOf(navArgument("pollId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pollId = backStackEntry.arguments?.getInt("pollId") ?: return@composable
            EditPollScreen(
                pollId = pollId,
                viewModel = pollListViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Voting.route,
            arguments = listOf(navArgument("pollId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pollId = backStackEntry.arguments?.getInt("pollId") ?: return@composable
            VotingScreen(
                pollId = pollId,
                viewModel = pollListViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onVoteSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(navArgument("pollId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pollId = backStackEntry.arguments?.getInt("pollId") ?: return@composable
            ResultsScreen(
                pollId = pollId,
                viewModel = pollListViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}