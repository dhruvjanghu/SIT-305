package com.example.lostandfoundapp

import android.Manifest
import android.app.Activity.RESULT_OK               // for RESULT_OK
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.lostandfoundapp.ui.theme.LostAndFoundAppTheme
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.accompanist.permissions.*          // accompanist permissions
import java.util.Locale
import kotlinx.coroutines.launch
import android.app.Activity


import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource



import com.google.accompanist.permissions.*

import kotlinx.coroutines.launch


// we opt in here so Accompanist’s permission APIs compile cleanly
@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize Google Places (only once)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext,
                "AIzaSyAX1Vi9Y3yHrg4fO_i82-lMaNPhCuWisaw"
            )
        }

        // setting up our whole UI inside this
        setContent {
            LostAndFoundAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home")   { LostAndFoundHomeScreen(navController) }
                    composable("add_item") { AddItemScreen(navController) }
                    composable("list")   { LostAndFoundListScreen(navController) }
                    composable("detail/{itemId}") { backStackEntry ->
                        val itemId = backStackEntry
                            .arguments
                            ?.getString("itemId")
                            ?.toInt() ?: -1
                        LostAndFoundDetailScreen(navController, itemId)
                    }
                    composable("map")    {
                        MapScreen(LocalContext.current, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun LostAndFoundHomeScreen(navController: NavHostController) {
    // simple vertical layout with spacing
    Column(
        modifier = Modifier
            .fillMaxSize()          // screen takes full space
            .padding(24.dp),        // space around the edges
        verticalArrangement = Arrangement.Center // buttons will be vertically centered
    ) {
        // first button to add a new advert
        Button(
            onClick = { navController.navigate("add_item") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("CREATE A NEW ADVERT")
        }

        // second button to view all items
        Button(
            onClick = { navController.navigate("list") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("SHOW ALL LOST & FOUND ITEMS")
        }

        // third button to show items on map
        Button(
            onClick = { navController.navigate("map") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("SHOW ON MAP")
        }
    }
}

@Suppress("DEPRECATION") // geocoder.getFromLocation is still OK for now
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddItemScreen(navController: NavHostController) {
    val context = LocalContext.current
    var activity: Activity? by remember { mutableStateOf(null) }

    // safely get activity reference
    LaunchedEffect(Unit) {
        activity = context as? Activity
    }

    val db = LostItemDatabase.getDatabase(context)
    val dao = db.lostItemDao()
    val coroutineScope = rememberCoroutineScope()

    var postType by remember { mutableStateOf("Lost") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val permissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // launcher for autocomplete result
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            location = place.address ?: ""
        }
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Create a new advert", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text("Post type: ")
            RadioButton(
                selected = postType == "Lost",
                onClick = { postType = "Lost" }
            )
            Text("Lost")
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(
                selected = postType == "Found",
                onClick = { postType = "Found" }
            )
            Text("Found")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Autocomplete Location Search Field
        OutlinedTextField(
            value = location,
            onValueChange = {},
            label = { Text("Search location") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        activity?.let {
                            val fields = listOf(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS
                            )
                            val intent = Autocomplete
                                .IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                                .build(it)
                            launcher.launch(intent)
                        }
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Get Current Location
        Button(
            onClick = {
                if (permissionState.status.isGranted) {
                    val cts = CancellationTokenSource()
                    fusedLocationClient
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            loc?.let {
                                val geo = Geocoder(context, Locale.getDefault())
                                val list = geo.getFromLocation(it.latitude, it.longitude, 1)
                                if (!list.isNullOrEmpty()) {
                                    location = list[0].getAddressLine(0)
                                } else {
                                    Toast.makeText(context, "Couldn't get address", Toast.LENGTH_SHORT).show()
                                }
                            } ?: run {
                                Toast.makeText(context, "Couldn't get current location", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GET CURRENT LOCATION")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val newItem = LostItem(
                        postType = postType,
                        name = name,
                        phone = phone,
                        description = description,
                        date = date,
                        location = location
                    )
                    dao.insertItem(newItem)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SAVE")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BACK")
        }
    }
}



@Composable
fun LostAndFoundListScreen(navController: NavHostController) {
    // get the app context (needed for database)
    val context = LocalContext.current

    // get database and DAO to access the data
    val db = LostItemDatabase.getDatabase(context)
    val dao = db.lostItemDao()

    // this holds the list of items we get from the database
    var items by remember { mutableStateOf<List<LostItem>>(emptyList()) }

    // when the screen loads, fetch all saved items from the DB
    LaunchedEffect(Unit) {
        items = dao.getAllItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // title at the top
        Text("All Lost & Found Items", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // go through each item in the list and show it inside a card
        items.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()                      // card takes full width
                    .padding(vertical = 8.dp)            // space between cards
                    .clickable {
                        // when user taps on a card, go to detail screen with itemId
                        navController.navigate("detail/${item.id}")
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // show post type and name
                    Text("${item.postType} - ${item.name}", style = MaterialTheme.typography.titleMedium)
                    // show the date it was posted
                    Text("Date: ${item.date}")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // space before back button

        // back button at the bottom to return to home screen
        Button(
            onClick   = { navController.popBackStack() },
            modifier  = Modifier.fillMaxWidth()
        ) {
            Text("BACK")
        }
    }
}

@Composable
fun LostAndFoundDetailScreen(navController: NavHostController, itemId: Int) {
    val context = LocalContext.current
    val db = LostItemDatabase.getDatabase(context)
    val dao = db.lostItemDao()
    val coroutineScope = rememberCoroutineScope()

    var item by remember { mutableStateOf<LostItem?>(null) }

    // fetch the item from database
    LaunchedEffect(Unit) {
        val allItems = dao.getAllItems()
        item = allItems.find { it.id == itemId }
    }

    item?.let { foundItem ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // show all item details
            Text(
                "${foundItem.postType} - ${foundItem.name}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Phone: ${foundItem.phone}")
            Text("Description: ${foundItem.description}")
            Text("Date: ${foundItem.date}")
            Text("Location: ${foundItem.location}")

            Spacer(modifier = Modifier.height(24.dp))

            // remove button
            Button(
                onClick = {
                    coroutineScope.launch {
                        dao.deleteItem(foundItem.id)
                        navController.popBackStack("list", inclusive = false)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("REMOVE")
            }

            Spacer(modifier = Modifier.height(12.dp)) // space between buttons

            // back button
            Button(
                onClick   = { navController.popBackStack() },
                modifier  = Modifier.fillMaxWidth()
            ) {
                Text("BACK")
            }
        }
    } ?: run {
        // fallback if item is not found
        Text("Item not found", modifier = Modifier.padding(16.dp))
    }
}
