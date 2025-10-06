package com.capstone.cropcare.domain.usecase

import com.capstone.cropcare.domain.model.UserEntity
import com.capstone.cropcare.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUserCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke(user: String, password: String){

        if (user.contains("@hotmail.com")){
            return
        }
        val response: UserEntity = authRepository.doLogin(user, password)

    }
}