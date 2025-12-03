@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.workerViews.homeHistory

import com.capstone.cropcare.domain.utils.toDateString
import com.capstone.cropcare.domain.utils.toTimeString
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.capstone.cropcare.view.core.components.CropCard
import com.capstone.cropcare.view.core.components.CropCardItemList
import com.capstone.cropcare.view.core.components.CropReportDetailsDialog

@Composable
fun HomeHistoryScreen(
    homeHistoryViewModel: HistoryViewModel = hiltViewModel(),
) {
    val groups by homeHistoryViewModel.groups.collectAsState()
    var selectedReport by remember { mutableStateOf<ReportModel?>(null) }
    var expandedSessionId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CropCard(
            modifier = Modifier.padding(bottom = 5.dp),
            textTitle = stringResource(R.string.home_history_list_title)
        ) {
            if (groups.isEmpty()) {
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
                    items(groups) { group ->

                        val isExpanded = expandedSessionId == group.sessionId

                        // Header de la sesión
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    expandedSessionId =
                                        if (isExpanded) null else group.sessionId
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = group.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = group.date.toDateString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Reportes: ${group.reports.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    painter = painterResource(
                                        id = if (isExpanded)
                                            R.drawable.ic_dropup
                                        else
                                            R.drawable.ic_dropdown
                                    ),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Lista de reportes de esa sesión
                            if (isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    group.reports.forEach { report ->
                                        CropCardItemList(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedReport = report },
                                            issueType = report.diagnostic,
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
