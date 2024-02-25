package com.wmsoftware.unirutas.domain.model

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
    val lastLocation: LocationInfo? = LocationInfo(),
    val lastLocationUpdateAt: String? = "",
    val fcm: String? = ""
)

@Serializable
data class LocationInfo(
    val latitude: String? = "",
    val longitude: String? = ""
)
