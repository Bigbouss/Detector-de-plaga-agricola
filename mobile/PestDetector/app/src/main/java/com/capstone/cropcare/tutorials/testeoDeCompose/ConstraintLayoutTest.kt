package com.capstone.cropcare.tutorials.testeoDeCompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun MyFirstConstraintLayout(modifier: Modifier){
    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        //similar a id en xml
        val (boxCyan, boxBlack, boxDarkGray, boxMagenta, boxGreen, boxYellow, boxGray, boxRed, boxBlue) = createRefs()

        Box(modifier = Modifier.size(75.dp).background(Color.Yellow).constrainAs(boxYellow){
            top.linkTo(parent.top)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)

        }){}

        Box(modifier = Modifier.size(75.dp).background(Color.Magenta).constrainAs(boxMagenta){
            bottom.linkTo(boxYellow.top)
            end.linkTo(boxYellow.start)
        }){}

        Box(modifier = Modifier.size(175.dp).background(Color.Blue).constrainAs(boxBlue){
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(boxYellow.bottom)
        }){}


        Box(modifier = Modifier.size(75.dp).background(Color.Green).constrainAs(boxGreen){
            bottom.linkTo(boxYellow.top)
            start.linkTo(boxYellow.end)
        }){}

        Box(modifier = Modifier.size(75.dp).background(Color.Gray).constrainAs(boxGray){
            top.linkTo(boxYellow.bottom)
            end.linkTo(boxYellow.start)
        }){}

        Box(modifier = Modifier.size(75.dp).background(Color.Red).constrainAs(boxRed){
            top.linkTo(boxYellow.bottom)
            start.linkTo(boxYellow.end)
        }){}

        Box(modifier = Modifier.size(75.dp).background(Color.Black).constrainAs(boxBlack){
            start.linkTo(boxCyan.end)
            end.linkTo(boxDarkGray.start)
            bottom.linkTo(boxBlue.top)
            top.linkTo(parent.top)
        }){}



        Box(modifier = Modifier.size(175.dp).background(Color.Cyan).constrainAs(boxCyan){
            bottom.linkTo(boxMagenta.top)
            end.linkTo(boxMagenta.end)
        }){}

        Box(modifier = Modifier.size(175.dp).background(Color.DarkGray).constrainAs(boxDarkGray){
            bottom.linkTo(boxGreen.top)
            start.linkTo(boxGreen.start)
        }){}


    }

}

//grandes 175
//chicas 75
