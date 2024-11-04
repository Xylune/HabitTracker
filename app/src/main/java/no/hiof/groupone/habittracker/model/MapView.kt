package no.hiof.groupone.habittracker.model

import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MyMapView(context: Context) : MapView(context) {

    private val myLocationOverlay: MyLocationNewOverlay
    private var locationMarker: Marker

    init {
        val sharedPreferences = context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, sharedPreferences)

        setMultiTouchControls(true)

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
            enableMyLocation()
            //enableFollowLocation()
        }

        locationMarker = Marker(this).apply { }
        overlays.add(locationMarker)

        controller.setZoom(15.0)

        myLocationOverlay.runOnFirstFix {
            val location = myLocationOverlay.myLocation
            if (location != null) {
                post {
                    setMarkerAtLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun setMarkerAtLocation(latitude: Double, longitude: Double) {
        locationMarker.position = GeoPoint(latitude, longitude)
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        controller.setCenter(locationMarker.position)
    }

    override fun onDetach() {
        super.onDetach()
        myLocationOverlay.disableMyLocation()
    }
}