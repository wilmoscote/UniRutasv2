package com.wmsoftware.unirutas.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val name: String? = "",
    val lastName: String? = "",
    val email: String? = "",
    val createdAt: String? = null,
    val status: Int? = 1,
    val coverPicture: String? = "",
    val profilePicture: String? = "",
    val carreer: String? = "Universidad de La Guajira",
    val lastLocation: LocationInfo? = LocationInfo()
)

@Serializable
data class LocationInfo(
    val latitude: String? = null,
    val longitude: String? = null
)
