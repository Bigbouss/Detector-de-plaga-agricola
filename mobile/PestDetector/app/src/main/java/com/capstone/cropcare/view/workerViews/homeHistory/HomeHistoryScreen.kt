@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.workerViews.homeHistory

import com.capstone.cropcare.domain.utils.toDateString
import com.capstone.cropcare.domain.utils.toTimeString
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.capstone.cropcare.R
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.view.core.components.CropBottomBar
import com.capstone.cropcare.view.core.components.CropCard
import com.capstone.cropcare.view.core.components.CropCardItemList
import com.capstone.cropcare.view.core.components.CropReportDetailsDialog
import com.capstone.cropcare.view.core.components.CropTopAppBar
import com.capstone.cropcare.view.core.navigation.NavItems
import kotlin.collections.listOf

@Composable
fun HomeHistoryScreen(
    homeHistoryViewModel: HistoryViewModel = hiltViewModel(),
    navigateToHome: () -> Unit,
) {
    val reports by homeHistoryViewModel.reports.collectAsState()
    var selectedReport by remember { mutableStateOf<ReportModel?>(null) }

    Scaffold(
        topBar = { CropTopAppBar() },
        bottomBar = {
            CropBottomBar(
                itemList = listOf(
                    NavItems(stringResource(R.string.home_bottom_bar_home), R.drawable.ic_home, onClick = { navigateToHome() }),
                    NavItems(stringResource(R.string.home_bottom_bar_history), R.drawable.ic_history, onClick = {})
                ),
                selectedIndex = 1,
                onItemSelected = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CropCard(
                modifier = Modifier.padding(bottom = 5.dp),
                textTitle = stringResource(R.string.home_history_list_title)
            ) {
                if (reports.isEmpty()) {
                    // Estado vacío
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_history),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.home_history_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.home_history_empty_list),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(reports) { report ->
                            CropCardItemList(
                                modifier = Modifier.clickable {
                                    selectedReport = report
                                },
                                issueType = report.diagnostic,
                                //issueName = report.workerName,
                                zoneName = report.zone.name,
                                cropName = report.crop.name,
                                date = report.timestamp.toDateString(),
                                hour = report.timestamp.toTimeString()
                            )
                        }
                    }
                }
            }
        }

        // Dialog de detalles
        selectedReport?.let { report ->
            CropReportDetailsDialog(
                report = report,
                onDismiss = { selectedReport = null }
            )
        }
    }
}



