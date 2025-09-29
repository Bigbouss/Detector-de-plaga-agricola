@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.workerViews.reports

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.components.CropButtonPrimary
import com.capstone.cropcare.view.core.components.CropCard
import com.capstone.cropcare.view.core.components.CropDropdown
import com.capstone.cropcare.view.core.components.CropTextField
import com.capstone.cropcare.view.core.components.CropTopAppBar
import androidx.compose.ui.graphics.asImageBitmap


@Composable
fun ReportScreenWorker(
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.state.collectAsState()

    Scaffold(
        topBar = { CropTopAppBar() }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            CropCard(
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxSize(),
                textTitle = stringResource(R.string.report_card_title)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    //FORM REPORT --------------------------
                    //Worker name
                    item {
                        CropTextField(
                            text = state.workerName,
                            onValueChange = {}, // No editable
                            label = stringResource(R.string.report_card_label_name),
                            placeholder = "",
                            enabled = false
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Diagnostic
                    item {
                        CropTextField(
                            text = state.diagnostic,
                            onValueChange = {},
                            label =  stringResource(R.string.report_card_label_diagnostic),
                            placeholder = "",
                            enabled = false
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Crop Zone
                    item {
                        CropDropdown(
                            selected = state.cropZone,
                            options = listOf("Zona 1", "Zona 2", "Zona 3"), // luego vienen del backend
                            onSelect = { reportViewModel.setCropZone(it) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }



                    // Photo
                    item {
                        state.analizedPhoto?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto analizada",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        } ?: Text("No hay foto seleccionada")
                        Spacer(Modifier.height(16.dp))
                    }

                    //Observation
                    item {
                        CropTextField(
                            text = state.observation,
                            onValueChange = { reportViewModel.setObservation(it) },
                            label =  stringResource(R.string.report_card_label_observation),
                            placeholder = stringResource(R.string.report_card_placeholder_observation),
                            singleLine = false
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Button
                    item {
                        CropButtonPrimary(
                            modifier = Modifier.fillMaxWidth(),
                            text =  stringResource(R.string.report_card_button),
                            onClick = { }
                            //onClick = { reportViewModel.saveReport() }
                        )
                    }
                }
            }
        }
    }
}

