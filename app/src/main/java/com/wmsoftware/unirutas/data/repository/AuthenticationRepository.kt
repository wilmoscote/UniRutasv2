package com.wmsoftware.unirutas.data.repository

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wmsoftware.unirutas.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun loginUser(email: String, password: String): Flow<Result<User>> = flow {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            val userId = auth.currentUser?.uid ?: throw Exception("Error al iniciar sesión")
            if(auth.currentUser?.isEmailVerified == true){
                val documentSnapshot = db.collection("users").document(userId).get().await()
                val user = documentSnapshot.toObject(User::class.java) ?: throw Exception("Usuario no encontrado")
                if (user.status == 0) throw Exception("Por motivos de seguridad su cuenta ha sido inhabilitada")
                emit(Result.success(user))
            } else throw Exception("Revise su correo electrónico y active su Cuenta")
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun registerUser(email: String, password: String, userData: User): Flow<Result<User>> = flow {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val userId = auth.currentUser?.uid ?: throw Exception("Error al obtener el ID del usuario")
            val userWithId = userData.copy(id = userId)
            db.collection("users").document(userId).set(userWithId).await()
            emit(Result.success(userWithId))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun sendEmailVerification(): Flow<Result<Boolean>> = flow {
        try {
            auth.currentUser?.sendEmailVerification()?.await()
            emit(Result.success(true))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}