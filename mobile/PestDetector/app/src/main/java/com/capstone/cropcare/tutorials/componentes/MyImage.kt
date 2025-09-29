package com.capstone.cropcare.tutorials.componentes

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.capstone.cropcare.R

@Composable
fun MyImage(){
    Image(
        painter = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = null, //accesibilidad, se puede describir que es lo que hay con un "asdas"
        modifier = Modifier
            .size(200.dp)
            .clip(RoundedCornerShape(topEnd = 20.dp))//redondear esquinas reportando imagen
            .border(width = 5.dp, color = Color.Red ,shape = CircleShape),
        contentScale = ContentScale.Fit // escala de imagen.
        )
}


@Composable
fun MyNetworkImage(){
    AsyncImage(model = "https://cdn.mos.cms.futurecdn.net/hbqiS6U4Jtvc2pVzpFj4ck.jpg", contentDescription = "estoy probando, no explotes",
        Modifier.size(500.dp),
        onError = {
            Log.i("Image", "Ocurrio error ${it.result.throwable.message}")
        }
        )
}


@Composable
fun MyIcon(){
    Icon(
        painter = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = null,
        modifier = Modifier.size(300.dp),
        tint = Color.Blue
    )
}