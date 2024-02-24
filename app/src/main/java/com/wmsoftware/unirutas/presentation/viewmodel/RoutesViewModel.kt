package com.wmsoftware.unirutas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.network.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val locationService: LocationService
) : ViewModel() {

    // SharedFlow para manejar las actualizaciones de ubicación
    val locationFlow = locationService.getLocationFlow().shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    val mapTypeConfig: StateFlow<Int?> = userPreferences.getMapTypeConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val mapTrafficConfig: StateFlow<Boolean?> = userPreferences.getMapTraficConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val mapMyLocationConfig: StateFlow<Boolean?> = userPreferences.getMapMyLocationConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
}