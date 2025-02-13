package co.edu.unal.retogps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import java.net.URL
import java.util.concurrent.Executors
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import org.osmdroid.views.overlay.Polygon
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val SETTINGS_REQUEST_CODE = 123

    private val executor = Executors.newSingleThreadExecutor()
    private var lastLocation: GeoPoint? = null  // Para actualizar después de cambiar preferencias

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getPreferences(Context.MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.minZoomLevel = 3.0  // Nivel mínimo de zoom para evitar que se aleje demasiado
        map.maxZoomLevel = 19.0 // Nivel máximo para evitar acercamientos extremos

        val worldBoundingBox = org.osmdroid.util.BoundingBox(85.0, 180.0, -85.0, -180.0)
        map.setScrollableAreaLimitDouble(worldBoundingBox)

        requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        setupLocationOverlay()
    }

    override fun onResume() {
        super.onResume()
        val locationOverlay = map.overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()
        locationOverlay?.myLocation?.let {
            updateMapWithNewRadius(it)
        }
    }


    private fun drawSearchRadius(center: GeoPoint, radiusMeters: Int) {
        val mapCircle = Polygon().apply {
            points = Polygon.pointsAsCircle(center, radiusMeters.toDouble())
            fillColor = 0x3000FF00  // Verde semitransparente
            strokeColor = 0xFF00FF00.toInt() // Verde con opacidad total
            strokeWidth = 2f
        }
        map.overlays.add(mapCircle)
        map.invalidate()
    }

    private fun getSearchRadius(): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.getString("radius", "1000")?.toIntOrNull() ?: 1000
    }

    private fun setupLocationOverlay() {
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()

        map.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            runOnUiThread {
                val myLocation: GeoPoint? = locationOverlay.myLocation
                if (myLocation != null) {
                    lastLocation = myLocation // 💡 Almacenar ubicación actual
                    map.controller.setCenter(myLocation)
                    map.controller.setZoom(15.0)
                    updateMapWithNewRadius(myLocation) // 🔥 Llamar a la función para actualizar el radio y los marcadores
                } else {
                    Log.e("OSM", "No se pudo obtener la ubicación en el primer intento")
                }
            }
        }
    }

    private fun updateMapWithNewRadius(location: GeoPoint) {
        map.overlays.clear() // Limpiamos los overlays previos
        val radius = getSearchRadius()
        Log.d("OSM", "Nuevo radio: $radius metros")

        addMarker(location, "Mi Ubicación")
        fetchNearbyPlaces(location.latitude, location.longitude) // 🔥 Llamamos a la API
        drawSearchRadius(location, radius)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(map).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
            icon = resources.getDrawable(R.drawable.location)
            setOnMarkerClickListener { m, _ ->
                m.showInfoWindow()
                true
            }
        }
        map.overlays.add(marker)
        map.invalidate()
    }

    private fun fetchNearbyPlaces(latitude: Double, longitude: Double) {
        val radius = getSearchRadius()
        val url = "https://overpass-api.de/api/interpreter?data=[out:json];node(around:$radius,$latitude,$longitude)[\"amenity\"];out;"

        executor.execute {
            try {
                val response = URL(url).readText()
                val json = JSONObject(response)
                val elements = json.getJSONArray("elements")

                runOnUiThread {
                    // 🔥 Eliminamos solo los polígonos previos, no los marcadores de la ubicación
                    map.overlays.removeIf { it is Polygon }

                    for (i in 0 until elements.length()) {
                        val element = elements.getJSONObject(i)
                        val lat = element.getDouble("lat")
                        val lon = element.getDouble("lon")
                        val tags = element.optJSONObject("tags")

                        val name = tags?.optString("name", "").orEmpty() // Extraer nombre

                        if (name.isNotEmpty()) { // 🔥 Filtrar solo los que tienen nombre
                            addMarker(GeoPoint(lat, lon), name)
                        }
                    }
                    map.invalidate()
                }
            } catch (e: Exception) {
                Log.e("OSM", "Error al obtener puntos de interés", e)
            }
        }
    }


    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        if (permissions.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_REQUEST_CODE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE) {
            lastLocation?.let {
                Log.d("OSM", "Regresando de configuración, actualizando radio...")
                updateMapWithNewRadius(it) // 🔥 Llamar a la función para actualizar mapa
            }
        }
    }
}
