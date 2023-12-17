package com.example.adopmekotlin.activity.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.adopmekotlin.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val btnSelectPlace = findViewById<Button>(R.id.btnSelectPlace)
        btnSelectPlace.setOnClickListener {
            openMainActivity()
        }

        // Inicializar el fragmento del mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.FrMaps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar opciones del mapa
        mMap.uiSettings.isZoomControlsEnabled = true // Permitir controles de zoom

        // Configurar un listener para el evento de clic en el mapa
        mMap.setOnMapClickListener { latLng ->
            // Puedes manejar el evento de clic aquí y realizar acciones, como agregar marcadores, etc.
            // Ejemplo: Agregar un marcador en la ubicación clicada
            mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
        }

        // Mover la cámara a una ubicación específica (por ejemplo, Lima, Perú)
        val limaLatLng = LatLng(-12.0464, -77.0428)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(limaLatLng, 12f))
    }
}