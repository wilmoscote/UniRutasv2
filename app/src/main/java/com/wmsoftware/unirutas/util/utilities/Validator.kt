package com.wmsoftware.unirutas.util.utilities

object Validator {

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[\\w.-]+@uniguajira\\.edu\\.co$"
        return email.matches(emailPattern.toRegex())
    }
}