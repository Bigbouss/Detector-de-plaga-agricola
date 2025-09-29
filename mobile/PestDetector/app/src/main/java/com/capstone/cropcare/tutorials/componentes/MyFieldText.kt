package com.capstone.cropcare.tutorials.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun MyParentTextField(modifier: Modifier){
    //variables en parent
    var normalTextField: String by remember { mutableStateOf("") }
    var advanceTextField: String by remember { mutableStateOf("") }
    var outlined: String by remember { mutableStateOf("") }
    var passwordTextField: String by remember { mutableStateOf("") }


    Column(modifier = modifier) {
        MyNormalTextField(normalTextField = normalTextField) {normalTextField = it}
        MyAdvanceTextField(advanceTextField = advanceTextField) {advanceTextField = it}
        MyPasswordTextField(passwordTextField = passwordTextField) {passwordTextField = it}
        MyOutlinedTextField(outlined = outlined) {outlined = it}
    }
}

@Composable
fun MyNormalTextField(normalTextField: String, onNormalChange: (String)-> Unit){
    TextField(normalTextField, {onNormalChange(it)}, label = {
        Text("Escribe algo:")
    })
}

@Composable
fun MyAdvanceTextField(advanceTextField: String, onAdvanceChange: (String) -> Unit){
    TextField(advanceTextField, {
        onAdvanceChange(it.replace("a", "")) //reemplaza a por vacio.
    })
}

@Composable
fun MyPasswordTextField(passwordTextField: String, onPasswordChange: (String)-> Unit){
    var passwordHidden by remember { mutableStateOf(true) } //boolean para ocultar contraseña

    TextField(
        value = passwordTextField,
        onValueChange = {onPasswordChange (it)},
        singleLine = true,
        label = {Text("Ingrese contraseña: ") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
        visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            Text(text = if(passwordHidden) "Mostrar" else "Ocultar", Modifier.clickable{passwordHidden = !passwordHidden})
        }


    )

}

@Composable
fun MyOutlinedTextField(outlined: String, onoutlinedChange: (String) -> Unit){
    OutlinedTextField(outlined, {
        onoutlinedChange(it.replace("a", "")) //reemplaza a por vacio.
    }, label = {Text("Ingrese Texto:")})
}

//BasicTextField -> lo mismo pero sin estilo, ideal para personalización de 0

