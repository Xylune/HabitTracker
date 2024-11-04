package no.hiof.groupone.habittracker.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import no.hiof.groupone.habittracker.model.MyMapView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

@Composable
fun MapScreen(modifier: Modifier = Modifier, topNavBarHeight: Int) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, sharedPreferences)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { MyMapView(context) },
            modifier = modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                //.padding(top = (topNavBarHeight + 32).dp)
                .padding(end = 16.dp)
        ) {
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(context: Context): MapView {
    val mapView = remember { MapView(context) }
    DisposableEffect(mapView) {
        onDispose { mapView.onDetach() }
    }
    return mapView
}
