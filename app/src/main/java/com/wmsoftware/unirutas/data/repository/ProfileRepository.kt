package com.wmsoftware.unirutas.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.domain.model.LocationInfo
import com.wmsoftware.unirutas.domain.model.User
import com.wmsoftware.unirutas.util.utilities.Const.TAG
import com.wmsoftware.unirutas.util.utilities.Validator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private var userListenerRegistration: ListenerRegistration? = null

    fun listenToUserChanges(): Flow<User?> = callbackFlow {
        val userId = auth.currentUser?.uid.orEmpty()
        if (userId.isBlank()) {
            close(Exception("El ID de usuario no puede estar vacío"))
        } else {
            val userRef = db.collection("users").document(userId)
            userListenerRegistration = userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    trySend(user).isSuccess
                } else {
                    trySend(null).isSuccess
                }
            }
            awaitClose { userListenerRegistration?.remove() }
        }
    }

    private fun stopListeningToUserChanges() {
        userListenerRegistration?.remove()
        userListenerRegistration = null
    }

    suspend fun updateLastLocation(location: LocationInfo) {
        Log.i(TAG, "Updating last location; ${location.toString()}")
        try {
            val userRef = db.collection("users").document(auth.currentUser?.uid.toString())
            userRef.update("lastLocation", location).await()
            userRef.update("lastLocationUpdateAt", Validator.getCurrentDateTime()).await()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    suspend fun updateFcmToken(token: String) {
        try {
            val userRef = db.collection("users").document(auth.currentUser?.uid.toString())
            userRef.update("fcm", token).await()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    fun logout() {
        // Detener el listener antes de cerrar sesión
        stopListeningToUserChanges()

        // Ahora cerramos sesión
        auth.signOut()
    }
}
