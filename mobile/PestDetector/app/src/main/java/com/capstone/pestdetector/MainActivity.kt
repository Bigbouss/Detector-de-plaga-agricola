package com.capstone.pestdetector
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

//
import com.capstone.pestdetector.ui.theme.PestDetectorTheme
import com.capstone.pestdetector.view.core.navigation.NavigationWrapper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PestDetectorTheme {
                NavigationWrapper()




            }
        }
    }
}



