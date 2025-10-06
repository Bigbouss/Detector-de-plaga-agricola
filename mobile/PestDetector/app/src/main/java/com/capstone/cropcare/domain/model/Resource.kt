package com.capstone.cropcare.domain.model

sealed class Resource<T>(val data: T? = null, val message: String? = null){
    class loading<T>(data: T? = null): Resource<T>(data)
    class Succces<T>(data: T? ): Resource<T>(data)
    class Error<T>(message: String, data: T? = null): Resource<T>(data, message)
}
