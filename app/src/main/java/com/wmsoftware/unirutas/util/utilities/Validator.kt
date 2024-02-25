package com.wmsoftware.unirutas.util.utilities

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Validator {

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[\\w.-]+@uniguajira\\.edu\\.co$"
        return email.matches(emailPattern.toRegex())
    }

    fun getCurrentDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return current.format(formatter)
    }

    enum class FirebaseErrorValidator(val errorKey: String, val spanishMessage: String) {
        USER_NOT_FOUND(
            "There is no user record corresponding to this identifier",
            "No se ha encontrado un usuario registrado con este correo institucional."
        ),
        INVALID_PASSWORD(
            "The password is invalid or the user does not have a password",
            "Correo electrónico o contraseña incorrecta"
        ),
        WRONG_CREDENTIAL(
            "The supplied auth credential is incorrect",
            "Correo electrónico o contraseña incorrecta"
        );

        companion object {
            fun getSpanishMessageForError(error: String): String {
                return entries.firstOrNull { error.contains(it.errorKey) }?.spanishMessage ?: error
            }
        }
    }
}