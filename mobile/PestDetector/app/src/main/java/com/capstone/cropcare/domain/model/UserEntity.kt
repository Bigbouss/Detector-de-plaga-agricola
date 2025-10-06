package com.capstone.cropcare.domain.model

data class UserEntity (
    val userId: String,
    val name: String,
    val nickname: String,
    val emailUser: String,
    val userMode: UserType


)

sealed class UserType(val userType: Int){

    data object WORKER_USER: UserType(0)
    data object ADMIN_USER: UserType(1)
}