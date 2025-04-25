package com.example.myapplication.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.myapplication.R
import com.example.myapplication.data.ruta.RutaDatabase
import com.example.myapplication.data.ruta.RutaEntity
import com.example.myapplication.service.GPSForegroundService
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*

class GPSFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap

    private lateinit var locationTextView: TextView
    private lateinit var btnCrearRuta: Button
    private lateinit var btnRutasGuardadas: Button

    private lateinit var db: RutaDatabase

    private var isCreatingRoute = false
    private var selectedPoints = mutableListOf<LatLng>()
    private var rutaPolyline: Polyline? = null
    private var currentLocation: LatLng? = null

    private val client = OkHttpClient()
    private val directionsApiKey = "AIzaSyDNaaCqxDRa4gLzihzOxO2KeSHbKEBK0Ic"

    private var destinoFinal: LatLng? = null
    private var haNotificadoLlegada = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_gps, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationTextView = view.findViewById(R.id.locationTextView)
        btnCrearRuta = view.findViewById(R.id.btnCrearRuta)
        btnRutasGuardadas = view.findViewById(R.id.btnRutasGuardadas)

        db = Room.databaseBuilder(
            requireContext(),
            RutaDatabase::class.java,
            "rutas_db"
        ).build()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        btnCrearRuta.setOnClickListener {
            isCreatingRoute = true
            selectedPoints.clear()
            rutaPolyline?.remove()
            mMap.clear()
        }

        btnRutasGuardadas.setOnClickListener {
            mostrarRutaEjemplo()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detenerServicioUbicacion()
    }

    private fun iniciarServicioUbicacion() {
        val serviceIntent = Intent(requireContext(), GPSForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(requireContext(), serviceIntent)
        } else {
            requireContext().startService(serviceIntent)
        }
    }

    private fun detenerServicioUbicacion() {
        val serviceIntent = Intent(requireContext(), GPSForegroundService::class.java)
        requireContext().stopService(serviceIntent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        if (hasLocationPermission()) {
            try {
                mMap.isMyLocationEnabled = true
                getLastLocation()
                iniciarActualizacionesUbicacion()
            } catch (e: SecurityException) {
                locationTextView.text = "Permiso denegado para ubicación"
                e.printStackTrace()
            }
        } else {
            requestLocationPermission()
        }

        mMap.setOnMapClickListener { latLng ->
            if (!isCreatingRoute) return@setOnMapClickListener
            if (selectedPoints.size >= 1) return@setOnMapClickListener

            selectedPoints.add(latLng)
            mMap.addMarker(MarkerOptions().position(latLng).title("Destino"))

            currentLocation?.let { origen ->
                trazarRutaConDirections(origen, latLng)
                isCreatingRoute = false
            }
        }
    }

    private fun getLastLocation() {
        if (!hasLocationPermission()) {
            locationTextView.text = "Permiso de ubicación no concedido"
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    currentLocation = userLocation
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))
                    mostrarDireccion(it)
                } ?: run {
                    locationTextView.text = "Ubicación no disponible"
                }
            }.addOnFailureListener {
                locationTextView.text = "Error al obtener ubicación"
            }
        } catch (e: SecurityException) {
            locationTextView.text = "Error de seguridad al obtener ubicación"
            e.printStackTrace()
        }
    }

    private fun iniciarActualizacionesUbicacion() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                val nuevaUbicacion = LatLng(location.latitude, location.longitude)
                currentLocation = nuevaUbicacion
                verificarLlegadaADestino(nuevaUbicacion)
            }
        }, null)
    }

    private fun verificarLlegadaADestino(ubicacionActual: LatLng) {
        val destino = destinoFinal ?: return
        if (haNotificadoLlegada) return

        val resultados = FloatArray(1)
        Location.distanceBetween(
            ubicacionActual.latitude, ubicacionActual.longitude,
            destino.latitude, destino.longitude,
            resultados
        )
        val distancia = resultados[0]

        if (distancia <= 100) {
            haNotificadoLlegada = true
            Toast.makeText(requireContext(), "¡Has llegado a tu destino!", Toast.LENGTH_LONG).show()
            detenerServicioUbicacion() // Opcional: detener servicio al llegar
        }
    }

    private fun mostrarDireccion(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val direccion = addresses[0].getAddressLine(0)
                locationTextView.text = direccion
            } else {
                locationTextView.text = "Dirección no disponible"
            }
        } catch (e: Exception) {
            locationTextView.text = "Error obteniendo dirección"
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    private fun trazarRutaConDirections(origen: LatLng, destino: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=driving" +
                "&language=es" +
                "&key=$directionsApiKey"
        lifecycleScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    val json = response.body?.string() ?: return@use
                    val rutaInfo = parsearRutaDesdeJson(json)
                    rutaInfo?.let { info ->
                        requireActivity().runOnUiThread {
                            rutaPolyline?.remove()
                            rutaPolyline = mMap.addPolyline(
                                PolylineOptions()
                                    .addAll(info.puntos)
                                    .color(Color.BLUE)
                                    .width(8f)
                            )
                            locationTextView.text = "Distancia: ${info.distanciaTexto} | Tiempo: ${info.duracionTexto}"
                            guardarRuta(info.puntos)

                            val mensaje = info.instrucciones.joinToString("\n\n")
                            AlertDialog.Builder(requireContext())
                                .setTitle("Instrucciones de la ruta")
                                .setMessage(mensaje)
                                .setPositiveButton("OK", null)
                                .show()

                            destinoFinal = destino
                            haNotificadoLlegada = false

                            // 🚀 Iniciar el servicio al generar la ruta
                            iniciarServicioUbicacion()
                        }
                    }
                }
            }
        }
    }

    private fun parsearRutaDesdeJson(json: String): RutaInfo? {
        return try {
            val jsonObj = JSONObject(json)
            val route = jsonObj.getJSONArray("routes").getJSONObject(0)
            val overviewPolyline = route.getJSONObject("overview_polyline").getString("points")
            val puntos = decodePolyline(overviewPolyline)

            val leg = route.getJSONArray("legs").getJSONObject(0)
            val distanciaTexto = leg.getJSONObject("distance").getString("text")
            val duracionTexto = leg.getJSONObject("duration").getString("text")

            val stepsJsonArray = leg.getJSONArray("steps")
            val instrucciones = mutableListOf<String>()
            for (i in 0 until stepsJsonArray.length()) {
                val step = stepsJsonArray.getJSONObject(i)
                val instruccionHtml = step.getString("html_instructions")
                val instruccionTexto = android.text.Html.fromHtml(instruccionHtml).toString()
                instrucciones.add(instruccionTexto)
            }

            RutaInfo(puntos, distanciaTexto, duracionTexto, instrucciones)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val p = LatLng(lat / 1E5, lng / 1E5)
            poly.add(p)
        }

        return poly
    }

    private fun guardarRuta(ruta: List<LatLng>) {
        if (ruta.isEmpty()) return

        val latitudes = ruta.joinToString(",") { it.latitude.toString() }
        val longitudes = ruta.joinToString(",") { it.longitude.toString() }

        val nuevaRuta = RutaEntity(
            nombre = "Ruta ${System.currentTimeMillis()}",
            fecha = Date().time,
            latitudes = latitudes,
            longitudes = longitudes
        )

        lifecycleScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                db.rutaDao().insertRuta(nuevaRuta)
            }
        }
    }

    private fun mostrarRutaEjemplo() {
        val rutaEjemplo = listOf(
            LatLng(-33.45, -70.66),
            LatLng(-33.46, -70.65),
            LatLng(-33.47, -70.64)
        )
        rutaPolyline?.remove()
        rutaPolyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(rutaEjemplo)
                .color(Color.BLUE)
                .width(8f)
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rutaEjemplo.first(), 15f))
    }

    data class RutaInfo(
        val puntos: List<LatLng>,
        val distanciaTexto: String,
        val duracionTexto: String,
        val instrucciones: List<String>
    )
}