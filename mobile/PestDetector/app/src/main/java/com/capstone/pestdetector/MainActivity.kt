package com.capstone.pestdetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.capstone.pestdetector.tutorials.componentes.MyButtons
//imports de tutorials
import com.capstone.pestdetector.tutorials.componentes.MyParentTextField
import com.capstone.pestdetector.tutorials.testeodeStates.MyState
//
import com.capstone.pestdetector.ui.theme.PestDetectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PestDetectorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    //ComponentColumn(Modifier.padding(innerPadding))

                    MyButtons(Modifier.padding(innerPadding))


                }
            }
        }
    }
}



