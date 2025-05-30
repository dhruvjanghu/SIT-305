package com.example.taskmanagerapp

// needed for date picker dialog
import android.app.DatePickerDialog

// all the compose stuff for UI
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// for handling and formatting date
import java.text.SimpleDateFormat
import java.util.*

// for live data and observing it
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue

@Composable
fun AddEditTaskScreen(
    navController: NavHostController,
    viewModel: TaskViewModel,
    taskId: Int
) {
    // get all tasks from the ViewModel (keeps observing)
    val allTasks by viewModel.allTasks.observeAsState(emptyList())

    // try to find the task we're editing (if taskId is not -1)
    val existingTask = remember(taskId, allTasks) {
        allTasks.find { task -> task.id == taskId }
    }

    // the fields we’ll show in the form (if editing, fill with old data)
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var dueDate by remember { mutableStateOf(existingTask?.dueDate ?: Date()) }

    // we need this to show date picker dialog
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // main layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // header: Add or Edit
        Text(
            text = if (taskId == -1) "Add Task" else "Edit Task",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // title input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // description input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // date picker button
        Button(onClick = {
            val calendar = Calendar.getInstance().apply { time = dueDate }
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val newDate = Calendar.getInstance()
                    newDate.set(year, month, day)
                    dueDate = newDate.time
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text("Select Due Date: ${dateFormatter.format(dueDate)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // save button (either adds or updates depending on taskId)
        Button(
            onClick = {
                // make sure title and description are not empty
                if (title.isNotBlank() && description.isNotBlank()) {
                    val newTask = Task(
                        id = existingTask?.id ?: 0,
                        title = title,
                        description = description,
                        dueDate = dueDate
                    )

                    if (taskId == -1) {
                        viewModel.insert(newTask) // add new task
                    } else {
                        viewModel.update(newTask) // update old task
                    }

                    // go back to task list screen
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (taskId == -1) "Add Task" else "Update Task")
        }

        // only show delete button if we're editing
        if (taskId != -1 && existingTask != null) {
            Spacer(modifier = Modifier.height(16.dp))

            // delete button
            Button(
                onClick = {
                    viewModel.delete(existingTask) // delete the task
                    navController.popBackStack() // go back to list
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Task")
            }
        }
    }
}
