package com.capstone.cropcare.view.core.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.capstone.cropcare.R


//Wrapper composable TEXT GENERAL ====================================================================
@Composable
fun CropTextTitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.displayLarge

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

@Composable
fun CropTextTopBar(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.White,
    style: TextStyle = MaterialTheme.typography.titleLarge

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}
@Composable
fun CropTextSubSection(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onBackground,
    style: TextStyle = MaterialTheme.typography.headlineMedium

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

@Composable
fun CropTextBody(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onBackground,
    style: TextStyle = MaterialTheme.typography.bodyMedium

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

//Wrapper composable TEXT TEXTFIELD =================================================================

@Composable
fun CropTextLabels(
    text: String,
    //color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.labelLarge

) {
    Text(text = text, style = style)
}

@Composable
fun CropTextPlaceHolder(
    text: String,
    style: TextStyle = MaterialTheme.typography.labelMedium

) {
    Text(text = text, style = style)
}

@Composable
fun CropTextTextFieldExtra(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.error,
    style: TextStyle = MaterialTheme.typography.labelSmall

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

//Wrapper composable TEXT BUTTONS ====================================================================
@Composable
fun CropTextButtonPrincipal(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    style: TextStyle = MaterialTheme.typography.labelLarge

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

@Composable
fun CropTextButtonSecondary(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSecondary,
    style: TextStyle = MaterialTheme.typography.labelMedium

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

@Composable
fun CropTextButtonLogReg(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.White,
    style: TextStyle = MaterialTheme.typography.labelLarge

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

//Wrapper composable TEXT CARDS ====================================================================
@Composable
fun CropTextCardTitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.titleMedium

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

@Composable
fun CropTextCardTitleAnalysisVersion(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight = FontWeight.SemiBold

) {
    Text(text = text, modifier = modifier, color = color, style = style, fontWeight = fontWeight)
}

@Composable
fun CropTextCardDescription(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyMedium

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}


@Composable
fun CropTextCardDialog(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface,


) {
    Row(modifier = modifier) {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.alignByBaseline()
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.alignByBaseline()
        )
    }

}

@Composable
fun CropTextCardDescriptionSmall(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodySmall

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

@Composable
fun CropTextCardExtra(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.labelMedium

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

//Wrapper composable TEXT LIST ====================================================================

@Composable
fun CropTextListItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.titleSmall


) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

@Composable
fun CropTextListItemTitleAnalysisVersion(
    modifier: Modifier = Modifier,
    text: String,
    goodColor: Color = Color.Green,
    badColor: Color = MaterialTheme.colorScheme.error,
    tryColor: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    val color = when (text) {
        stringResource(R.string.analysis_result_good_title) -> goodColor
        stringResource(R.string.analysis_result_bad_title) -> badColor
        stringResource(R.string.analysis_result_try_again_title) -> tryColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style,
        fontWeight = fontWeight
    )
}



@Composable
fun CropTextListItemDescription(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight? = null

) {
    Text(text = text, modifier = modifier, color = color, style = style, fontWeight =fontWeight)
}

@Composable
fun CropTextListChips(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.labelSmall

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}

@Composable
fun CropTextFeedback(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.labelSmall

) {
    Text(text = text, modifier = modifier, color = color, style = style)
}