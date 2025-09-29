package com.capstone.cropcare.tutorials.advance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MyDerivedState(modifier: Modifier = Modifier) {
    //Usar derivedStateOf para mejorar rendimiento, evitando la de reecomposición al ingresar cada caracter. Hasta que se cumpla la condicion
    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }
    val isFormValid by remember(email, password) {
        derivedStateOf {
            email.contains("@") && password.length > 4
        }
    }


    Column {
        TextField(value = email, onValueChange = {email = it})
        Spacer(modifier = Modifier.height(4.dp))
        TextField(value = password, onValueChange = {password = it})
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {}, enabled = isFormValid) {
            Text(text = "Login")
        }
    }

}