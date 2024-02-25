package com.wmsoftware.unirutas.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.data.repository.AuthenticationRepository
import com.wmsoftware.unirutas.domain.model.User
import com.wmsoftware.unirutas.network.service.LocationService
import com.wmsoftware.unirutas.util.utilities.Const.TAG
import com.wmsoftware.unirutas.util.utilities.Const.pregradosFacultadMap
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

    private val _emailSent = MutableLiveData<Boolean>()
    val emailSent: LiveData<Boolean> get() = _emailSent

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun login(email:String, password:String) {
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
        viewModelScope.launch {
            _loading.postValue(true)
            userRepository.registerUser(email, password, userData).collect { result ->
                Log.i(TAG, "Register status: ${result.toString()}")
                _registrationStatus.value = result
                if (result.isSuccess){
                    userRepository.sendEmailVerification().collect { verifyResult ->
                        Log.i(TAG, "Verification email status: ${verifyResult.toString()}")
                    }
                    subscribeToTopic(userData.carreer.toString())
                }
            }
            _loading.postValue(true)
        }
    }

    fun sendPasswordResetEmail(email: String){
        viewModelScope.launch {
            _loading.postValue(true)
            userRepository.sendPasswordResetEmail(email).collect { result ->
                _emailSent.postValue(result.isSuccess)
            }
            _loading.postValue(false)
        }
    }

    private fun subscribeToTopic(topic: String?){
        topic?.let {  carreer ->
        Firebase.messaging.subscribeToTopic(formatForFirebaseTopic(carreer))
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic: ${formatForFirebaseTopic(carreer)}"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed in topic: ${formatForFirebaseTopic(carreer)}"
                }
                Log.d(TAG, msg)
            }

            pregradosFacultadMap[carreer]?.let {
                Firebase.messaging.subscribeToTopic(formatForFirebaseTopic(it))
                    .addOnCompleteListener { task ->
                        var msg = "Subscribed to topic facultad: ${formatForFirebaseTopic(it)}"
                        if (!task.isSuccessful) {
                            msg = "Subscribe failed in topic facultad: ${formatForFirebaseTopic(it)}"
                        }
                        Log.d(TAG, msg)
                    }
            }
        }
    }

    private fun formatForFirebaseTopic(name: String): String {
        // Reemplaza espacios y caracteres no permitidos con "_"
        val sanitized = name.replace("[^a-zA-Z0-9-_.~%]".toRegex(), "_")

        // Corta el string si supera los 900 caracteres permitidos
        return if (sanitized.length > 900) sanitized.substring(0, 900) else sanitized
    }
}
