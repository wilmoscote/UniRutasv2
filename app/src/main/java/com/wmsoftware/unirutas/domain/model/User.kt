package com.wmsoftware.unirutas.domain.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerializedName("_id")
    val id: String?,
    val username: String?,
    val password: String?,
    val name: String?,
    val lastName: String?,
    val email: String?,
    val createdBy: String?,
    val createdAt: String?,
    @SerializedName("__v")
    val v: Int?,
    val accountStatus: Int?,
    val sendsEmails: Boolean?,
    val showActivity: Boolean?,
    val showStats: Boolean?,
    val coverPicture: String?,
    val profilePicture: String?,
    val socialNetworks: List<String>?,
    val status: String?,
    val rolId: String?
)
