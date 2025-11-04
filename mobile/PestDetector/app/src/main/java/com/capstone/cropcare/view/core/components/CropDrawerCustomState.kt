package com.capstone.cropcare.view.core.components

enum class CropDrawerCustomState {
    Opened,
    Closed
}

fun CropDrawerCustomState.isOpened(): Boolean{
    return this.name == "Opened"
}

fun CropDrawerCustomState.opposite(): CropDrawerCustomState{
    return if (this == CropDrawerCustomState.Opened) CropDrawerCustomState.Closed
    else CropDrawerCustomState.Opened
}