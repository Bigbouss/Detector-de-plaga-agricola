//package com.capstone.cropcare.domain.mappers
//
//import com.capstone.cropcare.data.remote.dto.WorkerResponse
//import com.capstone.cropcare.domain.model.UserModel
//import com.capstone.cropcare.domain.model.UserRole
//
//fun WorkerResponse.toUserModel(organizationId: String, organizationName: String): UserModel {
//    val fullName = "${user.firstName} ${user.lastName}".trim().ifEmpty { user.username }
//
//    return UserModel(
//        uid = user.id.toString(),
//        email = user.email,
//        name = fullName,
//        role = UserRole.WORKER,
//        organizationId = organizationId,
//        organizationName = organizationName,
//        mustChangePassword = false,
//        phoneNumber = user.phoneNumber
//    )
//}