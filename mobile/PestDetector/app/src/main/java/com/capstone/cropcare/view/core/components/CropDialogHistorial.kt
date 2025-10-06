package com.capstone.cropcare.view.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton (onClick = onDismiss) {
                Text(text = stringResource(R.string.text_btn_close))
            }
        },
        title = {
            CropTextCardTitle(text = stringResource(R.string.history_dialog_title))
        },
        text = {
            Column (modifier = Modifier.padding(top = 8.dp)) {

                CropTextCardDialog(
                    label = stringResource(R.string.history_dialog_diagnostic),
                    value = report.diagnostic
                )
                CropTextCardDialog(
                    label = stringResource(R.string.history_dialog_zone) ,
                    value=report.cropZone
                )

                CropTextCardDialog(
                    label = stringResource(R.string.history_dialog_date),
                    value = report.timestamp.toDateString()
                )
                CropTextCardDialog(
                    label = stringResource(R.string.history_dialog_time),
                    value = report.timestamp.toTimeString()
                )
                if (report.observation.isNotBlank()) {
                    CropTextCardDialog(
                        label = stringResource(R.string.history_dialog_observation),
                        value = report.observation
                    )
                }

                report.localPhotoPath?.let { path ->
                    AsyncImage(
                        model = File(path),
                        contentDescription = " ",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        shape = MaterialTheme.shapes.large
    )
}
