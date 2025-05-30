package com.example.taskmanagerapp

// this sealed class lets us define all our app's screens (routes)
sealed class Screen(val route: String) {
    object TaskList : Screen("task_list")
    object AddEditTask : Screen("add_edit_task/{taskId}") {
        // helper to build the route with taskId (if editing)
        fun createRoute(taskId: Int?) =
            if (taskId != null) "add_edit_task/$taskId" else "add_edit_task/-1"
    }
}
