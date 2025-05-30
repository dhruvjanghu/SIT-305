package com.example.taskmanagerapp

// these are needed icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.example.taskmanagerapp.ui.theme.TaskManagerAppTheme

class MainActivity : ComponentActivity() {

    // setting up the ViewModel using Room + repository
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            TaskRepository(
                TaskDatabase.getDatabase(this).taskDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskManagerAppTheme {
                // this keeps track of which screen/tab we're on
                val navController = rememberNavController()

                // this will store the current screen route
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                Scaffold(
                    bottomBar = {
                        // show bottom nav only on main two screens
                        if (currentRoute == Screen.TaskList.route || currentRoute?.startsWith("add_edit_task") == true) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    // main nav graph, handles routing between screens
                    NavHost(
                        navController = navController,
                        startDestination = Screen.TaskList.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // screen to show all tasks
                        composable(Screen.TaskList.route) {
                            TaskListScreen(navController, viewModel)
                        }

                        // add/edit task screen
                        composable(
                            route = Screen.AddEditTask.route,
                            arguments = listOf(
                                navArgument("taskId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
                            AddEditTaskScreen(navController, viewModel, taskId)
                        }
                    }
                }
            }
        }
    }
}

// this is the actual bottom navigation bar with two items
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val items = listOf(
            BottomNavItem(
                route = Screen.TaskList.route,
                label = "Tasks",
                icon = { Icon(Icons.Default.List, contentDescription = "Tasks") }
            ),
            BottomNavItem(
                route = Screen.AddEditTask.createRoute(-1),
                label = "Add",
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") }
            )
        )

        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // avoid making multiple copies of the same screen
                        popUpTo(Screen.TaskList.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = item.icon,
                label = { Text(item.label) }
            )
        }
    }
}

