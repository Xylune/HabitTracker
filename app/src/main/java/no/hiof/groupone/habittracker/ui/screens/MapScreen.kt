package no.hiof.groupone.habittracker.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import no.hiof.groupone.habittracker.model.MyMapView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, sharedPreferences)
        onDispose {
            Configuration.getInstance().osmdroidBasePath = null
            Configuration.getInstance().osmdroidTileCache = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { MyMapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
            }},
            modifier = modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
        ) {}
    }
}