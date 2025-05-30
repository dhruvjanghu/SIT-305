package com.example.lostandfoundapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.lostandfoundapp.ui.theme.LostAndFoundAppTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setting up our whole UI inside this
        setContent {
            // using the default theme
            LostAndFoundAppTheme {
                // we need this nav controller to move between screens
                val navController = rememberNavController()

                // this is where we define what screens we have and their routes
                NavHost(
                    navController = navController,
                    startDestination = "home" // this is the first screen that loads
                ) {
                    // our home screen with two buttons
                    composable("home") { LostAndFoundHomeScreen(navController) }

                    // this screen has the form to add lost/found items
                    composable("add_item") { AddItemScreen(navController) }
                    composable("list") { LostAndFoundListScreen(navController) }
                    composable("detail/{itemId}") { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")?.toInt() ?: -1
                        LostAndFoundDetailScreen(navController, itemId)

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
                onClick = { navController.navigate("add_item") }, // takes us to form screen
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp) // space above and below button
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
        }
    }


    @Composable
    fun AddItemScreen(navController: NavHostController) {
        // get context so we can access database
        val context = LocalContext.current

        // get the database and DAO so we can insert new item
        val db = LostItemDatabase.getDatabase(context)
        val dao = db.lostItemDao()

        // needed to run database operations in the background
        val coroutineScope = rememberCoroutineScope()

        // these are the values that the user will enter in the form
        var postType by remember { mutableStateOf("Lost") } // default selected is Lost
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }

        // full screen vertical layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // space from screen edge
        ) {
            // title at the top
            Text("Create a new advert", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp)) // space below the title

            // radio buttons for Lost / Found type
            Row {
                Text("Post type: ") // label before radio buttons

                // LOST option
                RadioButton(
                    selected = postType == "Lost",
                    onClick = { postType = "Lost" }
                )
                Text("Lost")

                Spacer(modifier = Modifier.width(8.dp)) // space between radios

                // FOUND option
                RadioButton(
                    selected = postType == "Found",
                    onClick = { postType = "Found" }
                )
                Text("Found")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // input fields the user will fill
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

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp)) // space before buttons

            // when user taps SAVE, save data to the database
            Button(
                onClick = {
                    coroutineScope.launch {
                        // building a LostItem object with the user input
                        val newItem = LostItem(
                            postType = postType,
                            name = name,
                            phone = phone,
                            description = description,
                            date = date,
                            location = location
                        )

                        // save this object in the database
                        dao.insertItem(newItem)

                        // after saving, go back to the home screen
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAVE")
            }

            Spacer(modifier = Modifier.height(12.dp)) // space between SAVE and BACK

            // BACK button just takes us back to the home screen
            Button(
                onClick = {
                    navController.popBackStack()
                },
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

        // screen layout
        Column(
            modifier = Modifier
                .fillMaxSize()          // take full screen
                .padding(16.dp)         // padding around the content
        ) {
            // title at the top
            Text("All Lost & Found Items", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp)) // space below title

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
                onClick = {
                    navController.popBackStack() // go back one screen
                },
                modifier = Modifier.fillMaxWidth()
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
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("BACK")
                }
            }
        } ?: run {
            // fallback if item is not found
            Text("Item not found", modifier = Modifier.padding(16.dp))
        }
    }
}


