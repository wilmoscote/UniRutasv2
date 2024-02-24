package com.wmsoftware.unirutas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
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
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val user: StateFlow<User?> = userPreferences.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /*private fun listenToUserChanges(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    // Log.w(TAG, "Aplicando cambios ", error)
                    CoroutineScope(Dispatchers.IO).launch {
                        userPreferences.saveUser(user)
                    }
                    if (user.status == 0) {

                    }
                }
            }
        }
    }*/

    fun logout(){
        viewModelScope.launch {
            userPreferences.clearDataStore()
        }
    }
}