package com.capstone.cropcare.view.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.capstone.cropcare.R
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.utils.toDateString
import com.capstone.cropcare.domain.utils.toTimeString
import java.io.File

@Composable
fun CropReportDetailsDialog(
    report: ReportModel,
    onDismiss: () -> Unit
) {
    Dialog (onDismissRequest = onDismiss) {
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Título
                Text(
                    text = stringResource(R.string.home_history_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Imagen
                report.photoPath?.let { path ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(path))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto del cultivo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Información del reporte
                //DetailRow(label = "Trabajador", value = report.workerName)
                DetailRow(label = stringResource(R.string.home_history_dialog_diagnostic), value = report.diagnostic)
                DetailRow(label = stringResource(R.string.home_history_dialog_zone), value = report.zone.name)
                DetailRow(label = stringResource(R.string.home_history_dialog_crop), value = report.crop.name)
                DetailRow(
                    label = stringResource(R.string.home_history_dialog_date),
                    value = "${report.timestamp.toDateString()} - ${report.timestamp.toTimeString()}"
                )

                if (report.observation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_history_dialog_observation),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = report.observation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Estado de sincronización
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (report.syncedWithBackend) R.drawable.ic_check
                            else R.drawable.ic_launcher_foreground
                        ),
                        contentDescription = null,
                        tint = if (report.syncedWithBackend)
                            Color.Green
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (report.syncedWithBackend)
                            stringResource(R.string.home_history_dialog_status_sync)
                        else
                            stringResource(R.string.home_history_dialog_status_no_sync),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón cerrar
                CropButtonPrimary(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.home_history_dialog_btn),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}