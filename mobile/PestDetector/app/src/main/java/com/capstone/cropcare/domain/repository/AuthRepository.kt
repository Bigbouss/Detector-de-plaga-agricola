package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.UserEntity

interface AuthRepository {
    fun doLogin(user: String, password: String): UserEntity
}