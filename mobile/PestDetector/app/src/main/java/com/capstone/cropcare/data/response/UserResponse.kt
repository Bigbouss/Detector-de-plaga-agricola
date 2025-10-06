package com.capstone.cropcare.data.response
import com.capstone.cropcare.domain.model.UserEntity
import com.capstone.cropcare.domain.model.UserType
import com.capstone.cropcare.domain.model.UserType.*

data class UserResponse (
    val userId: String,
    val name: String,
    val nickname: String,
    val emailUser: String,
    val userType: Int,

    )

fun UserResponse.toDomain(): UserEntity{

    val userMode: UserType = when(userType){
        WORKER_USER.userType -> WORKER_USER
        ADMIN_USER.userType -> ADMIN_USER
        else -> ADMIN_USER
    }

    return UserEntity(
        userId = userId,
        name = name,
        nickname = nickname,
        emailUser = emailUser,
        userMode = userMode
    )
}