package com.capstone.cropcare.view.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


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
            }



            content()


        }


    }

}

@Composable
fun CropCardItemList(
    modifier: Modifier = Modifier,
    issueTipe: String,
    issueName: String,
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
                    CropTextListItemDescription(text = "Diagnosis: ", fontWeight = FontWeight.SemiBold)
                    Row {
                        CropTextListItemDescription(text = issueTipe)
                        CropTextListItemDescription(text = "/")
                        CropTextListItemDescription(text = issueName)
                    }

                }

                Row {
                    CropTextListItemDescription(text = "Ubication: ", fontWeight = FontWeight.SemiBold)
                    Row {
                        CropTextListItemDescription(text = zoneName)
                        CropTextListItemDescription(text = cropName)
                    }

                }



                //Spacer(modifier = Modifier.padding(vertical = 2.dp))
                Row {
                    CropTextListChips(text = date)
                    CropTextListChips(text = hour)
                }
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
    CropCard(textTitle = title, modifier = Modifier
        .height(550.dp)
        .padding(horizontal = 15.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                ,
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
