package com.wmsoftware.unirutas.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.data.repository.ProfileRepository
import com.wmsoftware.unirutas.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _userDesactivated = MutableLiveData<Boolean>()
    val userDesactivated: LiveData<Boolean> get() = _userDesactivated

    fun getUserChanges() {
        viewModelScope.launch {
            profileRepository.listenToUserChanges().collect { user ->
                user?.let {
                    userPreferences.saveUser(it)
                    if (user.status == 0) {
                        _userDesactivated.postValue(true)
                    }
                }
            }
        }
    }

    fun checkCurrentUser(){
        viewModelScope.launch {
            userPreferences.getUser().firstOrNull {  user ->
                user?.let { data ->
                    if (data.status == 0) {
                        _userDesactivated.postValue(true)
                    }
                }
                return@firstOrNull true
            }
        }
    }

    fun getUserLasLocation(){
        viewModelScope.launch {
            userPreferences.getUserLastLocation().firstOrNull {  location ->
                location?.let {
                    profileRepository.updateLastLocation(location)
                }
                return@firstOrNull true
            }
        }
    }

    fun updateFcmToken(token: String){
        viewModelScope.launch {
            userPreferences.saveFcmToken(token)
            profileRepository.updateFcmToken(token)
        }
    }

    fun logout(){
        viewModelScope.launch {
            profileRepository.logout()
            userPreferences.clearDataStore()
        }
    }
}