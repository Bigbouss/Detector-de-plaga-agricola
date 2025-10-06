@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.workerViews.reports

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.io.File


@Composable
fun ReportScreenWorker(
    reportViewModel: ReportViewModel = hiltViewModel(),
    backToHome:() -> Unit

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
                    // Worker name (no editable)
                    item {
                        CropTextField(
                            text = state.workerName,
                            onValueChange = {},
                            label = stringResource(R.string.report_card_label_name),
                            enabled = false
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Diagnostic (no editable)
                    item {
                        CropTextField(
                            text = state.diagnostic,
                            onValueChange = {},
                            label = stringResource(R.string.report_card_label_diagnostic),
                            enabled = false
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Crop Zone (editable dropdown)
                    item {
                        CropDropdown(
                            selected = state.cropZone,
                            options = listOf("Zona 1", "Zona 2", "Zona 3"),
                            onSelect = { reportViewModel.setCropZone(it) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Photo
                    item {
                        val imageModel = state.analizedPhoto ?: state.localPhotoPath?.let { File(it) }

                        if (imageModel != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto analizada",
                                contentScale = ContentScale.Crop,
                                //placeholder = painterResource(R.drawable.ic_placeholder), // Opcional
                                //error = painterResource(R.drawable.ic_error), // Opcional
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_no_photo), // Tu icono
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "No hay foto seleccionada",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }


                    // Observation
                    item {
                        CropTextField(
                            text = state.observation,
                            onValueChange = { reportViewModel.setObservation(it) },
                            label = stringResource(R.string.report_card_label_observation),
                            placeholder = stringResource(R.string.report_card_placeholder_observation),
                            singleLine = false
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Button to save report
                    item {
                        CropButtonPrimary(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.report_card_button),
                            onClick = {
                                reportViewModel.saveReport()
                                backToHome()
                            }
                        )
                    }
                }
            }
        }
    }
}

