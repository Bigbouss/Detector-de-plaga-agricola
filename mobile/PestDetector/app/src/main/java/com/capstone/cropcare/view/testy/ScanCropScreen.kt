package com.capstone.cropcare.view.testy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.capstone.cropcare.view.core.components.CropTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCropScreen(
    zoneId: String,
    viewModel: ScanCropViewModel = hiltViewModel(),
    onCropSelected: (String) -> Unit
) {
    val crops by viewModel.crops.collectAsState()

    LaunchedEffect(zoneId) {
        viewModel.loadCrops(zoneId)
    }

    //Scaffold (topBar = { CropTopAppBar () }) { padding ->

        Column(Modifier.padding(16.dp)) {
            crops.forEach { crop ->
                Button(
                    onClick = { onCropSelected(crop.name) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(crop.name)
                }
            }
        }
    //}
}
