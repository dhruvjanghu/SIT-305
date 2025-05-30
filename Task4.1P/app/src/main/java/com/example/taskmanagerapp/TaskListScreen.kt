package com.example.taskmanagerapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavHostController
import androidx.compose.foundation.clickable

@Composable
fun TaskListScreen(
    navController: NavHostController,
    viewModel: TaskViewModel
) {
    // keep watching the task list from the viewmodel
    val taskList by viewModel.allTasks.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // just a heading
        Text("Task List:", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // show the task list using LazyColumn
        LazyColumn {
            items(taskList) { task ->
                TaskItem(
                    task = task,
                    onClick = {
                        // if you tap on a task, open it in Add/Edit screen
                        navController.navigate("add_edit_task/${task.id}")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // this button takes you to the full Add Task screen
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("add_edit_task/-1") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Task")
        }
    }
}

// reusable task item block
@Composable
fun TaskItem(task: Task, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(task.dueDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Title: ${task.title}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Description: ${task.description}")
            Text(text = "Due: $formattedDate")
        }
    }
}
