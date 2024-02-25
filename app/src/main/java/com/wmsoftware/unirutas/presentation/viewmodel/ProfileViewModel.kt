package com.wmsoftware.unirutas.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.data.repository.ProfileRepository
import com.wmsoftware.unirutas.domain.model.User
import com.wmsoftware.unirutas.network.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val user: StateFlow<User?> = userPreferences.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun logout(){
        viewModelScope.launch {
            profileRepository.logout()
            userPreferences.clearDataStore()
        }
    }
}