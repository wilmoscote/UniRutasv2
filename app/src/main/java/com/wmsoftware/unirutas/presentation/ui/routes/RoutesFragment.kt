package com.wmsoftware.unirutas.presentation.ui.routes

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.wmsoftware.unirutas.R
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.databinding.FragmentRoutesBinding
import com.wmsoftware.unirutas.network.service.LocationService
import com.wmsoftware.unirutas.presentation.viewmodel.RoutesViewModel
import com.wmsoftware.unirutas.util.utilities.Const.TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class RoutesFragment : Fragment(), OnMapReadyCallback, OnMapsSdkInitializedCallback {
    private lateinit var binding: FragmentRoutesBinding

    @Inject
    lateinit var locationService: LocationService

    @Inject
    lateinit var userPreferences: UserPreferences

    private lateinit var map: GoogleMap
    private var isBottomSheetShown = false
    private var firstLookLocation = true

    private var userLocation: Location? = null
    private lateinit var userLocationMarker: Marker

    private val viewModel: RoutesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoutesBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        collectUserLocation()
        initListeners()
    }

    private fun initView() {
        MapsInitializer.initialize(
            this@RoutesFragment.requireContext(),
            MapsInitializer.Renderer.LATEST,
            this
        )
    }

    private fun initListeners() {
        binding.btnMapConfig.setOnClickListener {
            if (!isBottomSheetShown) {
                val bottomSheet = MapConfigBottomSheet().apply {
                    onDismissListener = {
                        isBottomSheetShown = false
                    }
                }
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
                isBottomSheetShown = true
            }
        }

        binding.buttonZoomIn.setOnClickListener {
            if (::map.isInitialized) {
                val currentZoom = map.cameraPosition.zoom
                if (currentZoom < map.maxZoomLevel) {
                    map.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1))
                }
            }
        }

        binding.buttonZoomOut.setOnClickListener {
            if (::map.isInitialized) {
                val currentZoom = map.cameraPosition.zoom
                if (currentZoom > map.minZoomLevel) {
                    map.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1))
                }
            }
        }

        binding.buttonMyLocation.setOnClickListener {
            if (::map.isInitialized) {
                val location = userLocation
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            }
        }
    }

    private fun collectUserLocation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationFlow.collect { location ->
                    location?.let { updateMapWithUserLocation(it) }
                }
            }
        }
    }

    private fun updateMapWithUserLocation(location: Location) {
        userLocation = location
        val userLatLng = LatLng(location.latitude, location.longitude)
        if (!::userLocationMarker.isInitialized) {
            val markerOptions = MarkerOptions()
                .position(userLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_map_pin))
            userLocationMarker = map.addMarker(markerOptions)!!
        } else {
            userLocationMarker.position = userLatLng
        }
        if (firstLookLocation){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            firstLookLocation = false
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.apply {
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            isMyLocationEnabled = false

        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(11.545443868177097, -72.90697823022481), 8f))

        observeMapConfigs()
    }

    private fun observeMapConfigs() {
        viewModel.mapTypeConfig.asLiveData().observe(viewLifecycleOwner) { mapType ->
            mapType?.let { map.mapType = it }
        }

        viewModel.mapTrafficConfig.asLiveData().observe(viewLifecycleOwner) { isEnabled ->
            isEnabled?.let { map.isTrafficEnabled = it }
        }

        viewModel.mapMyLocationConfig.asLiveData().observe(viewLifecycleOwner) { isEnabled ->
            isEnabled?.let {
                // Asegúrate de manejar los permisos antes de cambiar la visibilidad del marcador
                if (::userLocationMarker.isInitialized) {
                    userLocationMarker.isVisible = it
                }
            }
        }
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }
}