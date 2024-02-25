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
}