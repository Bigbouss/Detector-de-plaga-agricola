package com.capstone.cropcare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // chips, badges
    small = RoundedCornerShape(12.dp),       // ítems chicos, diálogos chicos
    medium = RoundedCornerShape(20.dp),      // botones, textfields, cards medianas
    large = RoundedCornerShape(28.dp),       // sheets, cards grandes
    extraLarge = RoundedCornerShape(40.dp),  // diálogos, contenedores grandes
)
