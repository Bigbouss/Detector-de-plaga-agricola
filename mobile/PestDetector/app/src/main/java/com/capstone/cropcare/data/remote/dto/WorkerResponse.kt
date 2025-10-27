//package com.capstone.cropcare.data.remote.dto
//
//import com.google.gson.annotations.SerializedName
//
//data class WorkerResponse(
//    val id: Int,
//    val user: UserDto,
//    val profile: ProfileDto
//)
//
//data class UserDto(
//    val id: Int,
//    val username: String,
//    val email: String,
//    @SerializedName("first_name")
//    val firstName: String,
//    @SerializedName("last_name")
//    val lastName: String,
//    @SerializedName("phone_number")
//    val phoneNumber: String? = null
//)
//
//data class ProfileDto(
//    val role: String,
//    @SerializedName("is_active")
//    val isActive: Boolean,
//    @SerializedName("can_manage_plots")
//    val canManagePlots: Boolean,
//    @SerializedName("joined_at")
//    val joinedAt: String
//)