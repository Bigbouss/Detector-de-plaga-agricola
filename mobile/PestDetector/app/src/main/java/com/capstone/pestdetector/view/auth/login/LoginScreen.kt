package com.capstone.pestdetector.view.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.size.Size
import com.capstone.pestdetector.R

@Composable
fun LoginScreen() {
    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }

    Scaffold { padding ->

        Box( modifier = Modifier
            .fillMaxSize()
           // .background(Color.Red)
            ){
            Image(
                painter = painterResource(id = R.drawable.bg_login), // o rememberAsyncImagePainter para imágenes remotas
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
            )

            Column(modifier = Modifier.padding(padding)){
                Spacer(modifier = Modifier.weight(1.2f))
                //SECCION PARA HEADER O NOMBRE DE APP
                Box(
                    modifier = Modifier
                     //   .background(Color.Cyan)
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center

                ) {
                    Text(
                        text = "MyAPP",
                        fontSize = 35.sp
                    )
                } //TEMPORAL, REEMPLAZAR POR IMG -- POR DECIDIR

                Spacer(modifier = Modifier.weight(0.5f))
                //SECCION PARA FORMULARIO LOGIN
                Box(
                    modifier = Modifier
                     //   .background(Color.DarkGray)
                        .height(460.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 25.dp)) {
                        Text("LOGIN", fontSize = 30.sp, color = Color.White, modifier = Modifier.padding(bottom = 15.dp))
                        // TextField
                        EmailTextField(email = email) { email = it }
                        Spacer(Modifier.height(8.dp))
                        PasswordTextField(password = password) { password = it }
                        Spacer(Modifier.height(8.dp))
                        LoginButton()
                        Spacer(modifier = Modifier.height(20.dp))
                        OrDivider()
                        Spacer(modifier = Modifier.height(20.dp))
                        GoogleLoginButton()
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "Don't Have An Account?",
                                color = Color.White,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Thin,
                                fontSize = 14.sp)
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "Sign Up",
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                color = Color.White,
                                modifier = Modifier.clickable {}) // Aqui redireccionar a REGISTER
                            Spacer(Modifier.weight(1f))
                        }


                    }

                }

                Spacer(modifier = Modifier.weight(0.85f))

            }
        }

    }

}


//Composables TextFields =====================================================================================
//TEXTFIELD EMAIL
@Composable
fun EmailTextField(email: String, onEmailChange: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = { onEmailChange(it) },
        modifier = Modifier.fillMaxWidth(),//.border(width =1.dp ,color= Color.Red, shape = RoundedCornerShape(40)),
        label = { Text("email") },
        shape = RoundedCornerShape(40),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.7f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
           // focusedIndicatorColor = Color.Red,
            unfocusedIndicatorColor = Color.LightGray

            )
    )
}

//TEXTFIELD PASSWORD
@Composable
fun PasswordTextField(password: String, onPasswordChange: (String) -> Unit) {
    var showPassword: Boolean by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = password,
        onValueChange = { onPasswordChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("password") },
        singleLine = true,
        shape = RoundedCornerShape(40),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false
        ),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            if (showPassword) {
                Icon(
                    painter = painterResource(R.drawable.ic_show_password),
                    contentDescription = "Show Password",
                    modifier = Modifier.clickable { showPassword = !showPassword }
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_hidden_password),
                    contentDescription = "Hide Password",
                    modifier = Modifier.clickable { showPassword = !showPassword })
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.7f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
            // focusedIndicatorColor = Color.Red,
            unfocusedIndicatorColor = Color.LightGray

        )
    )
}

//COMPOSABLE BUTTON ======================================================================================
@Composable
fun LoginButton() {
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)

    ) { Text("Sign In") }
}

@Composable
fun GoogleLoginButton() {
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)

    ) { Text("Continue whith Google") }
}

//COMPOSABLE DIVIDER ======================================================================================

@Composable
fun OrDivider(modifier: Modifier = Modifier, text: String = "or") {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White,
            thickness = 1.dp
        )

        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White,
            thickness = 1.dp
        )
    }
}
