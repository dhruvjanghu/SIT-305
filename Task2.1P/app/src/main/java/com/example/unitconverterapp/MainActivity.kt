package com.example.unitconverterapp

// all the imports needed for building UI and handling state
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unitconverterapp.ui.theme.UnitConverterAppTheme

// main activity — this is where the app starts
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge() // gets rid of weird system padding at top/bottom

        // setting up the whole screen content using Jetpack Compose
        setContent {
            UnitConverterAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UnitConverterScreen() // calling the main UI
                }
            }
        }
    }
}

@Composable
fun UnitConverterScreen() {
    // storing user input and selected units
    var input by remember { mutableStateOf("") }
    var sourceUnit by remember { mutableStateOf("Meter") }
    var destinationUnit by remember { mutableStateOf("Kilometer") }
    var result by remember { mutableStateOf("") }

    // just a list of units to show in dropdowns
    val units = listOf("Meter", "Kilometer", "Centimeter")

    // arranging everything vertically with padding
    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()) {

        // input box for the number user wants to convert
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter value") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp)) // space between elements

        // dropdown for "From" unit
        Text("From:")
        DropdownMenuBox(units, sourceUnit) { selected ->
            sourceUnit = selected
        }

        Spacer(modifier = Modifier.height(8.dp))

        // dropdown for "To" unit
        Text("To:")
        DropdownMenuBox(units, destinationUnit) { selected ->
            destinationUnit = selected
        }

        Spacer(modifier = Modifier.height(16.dp))

        // the convert button — calls the logic function and shows result
        Button(
            onClick = {
                result = convertUnits(input, sourceUnit, destinationUnit)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Convert")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // shows the result text
        Text("Result: $result")
    }
}

@Composable
fun DropdownMenuBox(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) } // tracks if menu is open

    Box {
        // text field that opens the dropdown on click
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text("Unit") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        // actual dropdown menu that pops up
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option) // update selected value
                        expanded = false   // close dropdown
                    }
                )
            }
        }
    }
}

// does the actual conversion math
fun convertUnits(value: String, fromUnit: String, toUnit: String): String {
    // trying to convert the string to number — return error if it's not a valid number
    val number = value.toDoubleOrNull() ?: return "Invalid input"

    // all the units converted to meters so math is easy
    val toMeter = mapOf(
        "Meter" to 1.0,
        "Kilometer" to 1000.0,
        "Centimeter" to 0.01
    )

    // get how many meters in the selected units
    val fromRate = toMeter[fromUnit]
    val toRate = toMeter[toUnit]

    // check if any unit is wrong or missing
    if (fromRate == null || toRate == null) return "Invalid unit"

    // basic conversion formula
    val result = number * fromRate / toRate
    return "%.2f".format(result) // format to 2 decimal places
}
