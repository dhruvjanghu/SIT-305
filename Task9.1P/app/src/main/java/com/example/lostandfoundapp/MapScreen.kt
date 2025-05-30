package com.example.lostandfoundapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.MapsInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.location.Geocoder
import java.util.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController


@Composable
fun MapScreen(context: Context, navController: NavHostController) {
    var mapView by remember { mutableStateOf<MapView?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                MapView(it).apply {
                    onCreate(null)
                    getMapAsync(MapReadyCallback(context, this))
                    mapView = this
                }
            },
            modifier = Modifier
                .weight(1f) // this makes the map fill the remaining space
        )

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("BACK")
        }
    }
}


@SuppressLint("MissingPermission")
class MapReadyCallback(private val context: Context, private val mapView: MapView) : OnMapReadyCallback {
    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(context)

        // zoom to Deakin Burwood for now
        val defaultLocation = LatLng(-37.8474, 145.1140)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // fetch items from Room and place pins
        val db = LostItemDatabase.getDatabase(context)
        val dao = db.lostItemDao()

        GlobalScope.launch(Dispatchers.IO) {
            val items = dao.getAllItems()

            launch(Dispatchers.Main) {
                items.forEach { item ->
                    try {
                        // try to geocode the location into coordinates
                        val latLng = getLatLngFromAddress(context, item.location)
                        if (latLng != null) {
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("${item.postType}: ${item.name}")
                                    .snippet(item.description)
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            if (item.postType == "Lost") BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                                        )
                                    )
                            )
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}





fun getLatLngFromAddress(context: Context, location: String): LatLng? {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocationName(location, 1)

    return if (!addresses.isNullOrEmpty()) {
        val loc = addresses[0]
        LatLng(loc.latitude, loc.longitude)
    } else null
}
