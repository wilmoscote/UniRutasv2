package com.wmsoftware.unirutas.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.data.repository.AuthenticationRepository
import com.wmsoftware.unirutas.domain.model.User
import com.wmsoftware.unirutas.network.service.LocationService
import com.wmsoftware.unirutas.util.utilities.Const.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val userRepository: AuthenticationRepository
) : ViewModel() {

    private val _loginStatus = MutableStateFlow<Result<User>?>(null)
    val loginStatus: StateFlow<Result<User>?> = _loginStatus.asStateFlow()

    private val _registrationStatus = MutableStateFlow<Result<User>?>(null)
    val registrationStatus: StateFlow<Result<User>?> = _registrationStatus.asStateFlow()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun login(email:String, password:String) {
        Log.i(TAG, "Login: Email: $email Pass: $password")

        viewModelScope.launch {
            _loading.postValue(true)
            userRepository.loginUser(email, password).collect { result ->
                Log.d(TAG, "Login result: ${result.toString()}")
                _loginStatus.value = result
                if(result.isSuccess){
                    userPreferences.saveUser(result.getOrNull())
                }
            }
            _loading.postValue(false)
        }
    }

    fun register(email:String,password:String, userData: User) {
        Log.i(TAG, "Register: Email: $email Pass: $password Data: $userData")

        viewModelScope.launch {
            _loading.postValue(true)
            userRepository.registerUser(email, password, userData).collect { result ->
                Log.i(TAG, "Register status: ${result.toString()}")
                _registrationStatus.value = result
                if (result.isSuccess){
                    userRepository.sendEmailVerification().collect { verifyResult ->
                        Log.i(TAG, "Verification email status: ${verifyResult.toString()}")
                    }
                }
            }
            _loading.postValue(true)
        }
    }
}
