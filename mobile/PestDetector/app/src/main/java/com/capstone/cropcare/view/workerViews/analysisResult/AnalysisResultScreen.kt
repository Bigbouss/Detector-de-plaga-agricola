@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.workerViews.analysisResult


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.capstone.cropcare.R
import com.capstone.cropcare.view.ui.theme.checkColorIcon
import com.capstone.cropcare.view.ui.theme.customGreen
import com.capstone.cropcare.view.core.components.AnalysisResultCard
import com.capstone.cropcare.view.core.components.CropTopAppBar

@Composable
fun AnalysisScreen(
    analysisViewModel: AnalysisViewModel = hiltViewModel(),
    backToHome: () -> Unit,
    backToCamera: () -> Unit,
    navigateToReport: () -> Unit
) {
    val state by analysisViewModel.state.collectAsState()

    Scaffold(
        topBar = { CropTopAppBar() },
    ) { paddingValues ->




        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Spacer(Modifier.weight(0.3f))

                when (state) {
                    is AnalysisState.Loading -> CircularProgressIndicator()

                    is AnalysisState.Good -> AnalysisResultCard(
                        title = stringResource(R.string.analysis_result_good_title),
                        description = stringResource(R.string.analysis_result_good_description),
                        buttonText = stringResource(R.string.analysis_result_back_button),
                        iconRes = R.drawable.ic_check,
                        tint = checkColorIcon,
                        onButtonClick = { backToHome() }
                    )

                    is AnalysisState.Bad -> AnalysisResultCard(
                        title = stringResource(R.string.analysis_result_bad_title),
                        description = stringResource(R.string.analysis_result_bad_description),
                        buttonText = stringResource(R.string.analysis_result_report_button),
                        iconRes = R.drawable.ic_plague_analysis,
                        tint = Color.Unspecified,
                        onButtonClick = { navigateToReport() }
                    )

                    is AnalysisState.TryAgain -> AnalysisResultCard(
                        title = stringResource(R.string.analysis_result_try_again_title),
                        description = stringResource(R.string.analysis_result_try_again_description),
                        buttonText = stringResource(R.string.analysis_result_try_again_button),
                        iconRes = R.drawable.ic_no_photo,
                        tint = customGreen,
                        onButtonClick = { backToCamera() }
                    )
                }

                Spacer(Modifier.weight(1f))
            }
        }
    }
}

//@Composable
//fun AnalysisScreen(
//    analysisViewModel: AnalysisViewModel = hiltViewModel(),
//    backToHome: () -> Unit,
//    backToCamera: () -> Unit,
//    navigateToReport: () -> Unit
//) {
//    val state by analysisViewModel.state.collectAsState()
//    val diseaseInfo by analysisViewModel.diseaseInfo.collectAsState()
//
//    Scaffold(
//        topBar = { CropTopAppBar() },
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.background),
//            contentAlignment = Alignment.Center
//        ) {
//            Column {
//                Spacer(Modifier.weight(0.3f))
//
//                when (state) {
//                    is AnalysisState.Loading -> CircularProgressIndicator()
//
//                    is AnalysisState.Good -> AnalysisResultCard(
//                        title = "¡Planta Saludable!",
//                        description = diseaseInfo?.let {
//                            "Tu ${it.plantType} está en excelente estado.\nConfianza: ${(it.confidence * 100).toInt()}%"
//                        } ?: stringResource(R.string.analysis_result_good_description),
//                        buttonText = stringResource(R.string.analysis_result_back_button),
//                        iconRes = R.drawable.ic_check,
//                        tint = checkColorIcon,
//                        onButtonClick = { backToHome() }
//                    )
//
//                    is AnalysisState.Bad -> AnalysisResultCard(
//                        title = "Enfermedad Detectada",
//                        description = diseaseInfo?.let {
//                            "Planta: ${it.plantType}\n" +
//                                    "Enfermedad: ${it.diseaseName}\n" +
//                                    "Confianza: ${(it.confidence * 100).toInt()}%\n\n" +
//                                    "Genera un reporte para más información."
//                        } ?: stringResource(R.string.analysis_result_bad_description),
//                        buttonText = stringResource(R.string.analysis_result_report_button),
//                        iconRes = R.drawable.ic_plague_analysis,
//                        tint = Color.Unspecified,
//                        onButtonClick = { navigateToReport() }
//                    )
//
//                    is AnalysisState.TryAgain -> AnalysisResultCard(
//                        title = stringResource(R.string.analysis_result_try_again_title),
//                        description = diseaseInfo?.let {
//                            when {
//                                it.rawLabel == "Unknown" ->
//                                    "No se reconoce el tipo de planta. Por favor, toma una foto de manzana, maíz o papa."
//                                it.confidence < 0.60f ->
//                                    "La confianza es muy baja (${(it.confidence * 100).toInt()}%). Intenta con mejor iluminación."
//                                else ->
//                                    stringResource(R.string.analysis_result_try_again_description)
//                            }
//                        } ?: stringResource(R.string.analysis_result_try_again_description),
//                        buttonText = stringResource(R.string.analysis_result_try_again_button),
//                        iconRes = R.drawable.ic_no_photo,
//                        tint = customGreen,
//                        onButtonClick = { backToCamera() }
//                    )
//                }
//
//                Spacer(Modifier.weight(1f))
//            }
//        }
//    }
//}
