package com.capstone.cropcare.view.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.R


@Composable
fun CropCard(
    modifier: Modifier = Modifier,
    textTitle: String,
    content: @Composable () -> Unit = {}


) {


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
    ) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp)
            ) {
                CropTextCardTitleAnalysisVersion(text = textTitle)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 1.dp
                )
                Spacer(Modifier.height(5.dp))
            }



            content()


        }


    }

}


@Composable
fun CropCardAdmin(
    modifier: Modifier = Modifier,
    textTitle: String,
    iconCard: Int,
    iconAction: () -> Unit,
    content: @Composable () -> Unit = {},



) {


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
    ) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp)
            ) {
                Row(Modifier.padding(end = 10.dp)) {
                    CropTextCardTitleAnalysisVersion(text = textTitle)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(iconCard),
                        contentDescription = "add worker by code",
                        modifier = Modifier.clickable(onClick = {iconAction()}))

                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 1.dp
                )
                Spacer(Modifier.height(5.dp))
            }



            content()


        }


    }

}

@Composable
fun CropCardWeather(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}

) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
    ) {
        content()
    }
}

@Composable
fun CropCardItemList(
    modifier: Modifier = Modifier,
    issueType: String,
    // issueName: String,
    zoneName: String,
    cropName: String,
    date: String,
    hour: String

) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(vertical = 5.dp)

    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Red)


            ) {}

            Spacer(Modifier.width(15.dp))
            Column(
                modifier = Modifier.padding(vertical = 5.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {

                Row {
                    CropTextListItemDescription(
                        text = stringResource(R.string.home_history_card_item_diagnostic),
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        CropTextListItemDescription(text = "  ")
                        CropTextListItemDescription(text = issueType)
                        //CropTextListItemDescription(text = " / ")
                        //CropTextListItemDescription(text = issueName)
                    }

                }

                Row {
                    CropTextListItemDescription(
                        text = stringResource(R.string.home_history_card_item_ubication),
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        CropTextListItemDescription(text = "  ")
                        CropTextListItemDescription(text = zoneName)
                        CropTextListItemDescription(text = " - ")
                        CropTextListItemDescription(text = cropName)
                    }

                }




                Row {
                    CropTextListChips(text = date)
                    CropTextListChips(text = " ")
                    CropTextListChips(text = hour)
                }
            }

        }
    }

}
@Composable
fun CropCardItemListWorker(
    modifier: Modifier = Modifier,
    nameWorker: String,
    emailWorker: String,
    onOptionsClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nameWorker.firstOrNull()?.uppercase() ?: "W",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(15.dp))

            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 5.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = nameWorker,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = emailWorker,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Options button
            IconButton(onClick = onOptionsClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_options_list),
                    contentDescription = "Opciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AnalysisResultCard(
    title: String,
    description: String,
    buttonText: String,
    @DrawableRes iconRes: Int,
    onButtonClick: () -> Unit = {},
    tint: Color,
) {
    CropCard(
        textTitle = title, modifier = Modifier
            .height(550.dp)
            .padding(horizontal = 15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(Modifier.height(20.dp))

            // Icono dinámico
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(230.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = tint
                )
            }

            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    CropTextCardDescription(text = description)
                    Spacer(Modifier.weight(1f))
                    CropButtonPrimary(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 15.dp),
                        text = buttonText,
                        onClick = onButtonClick
                    )
                }
            }
        }
    }
}
