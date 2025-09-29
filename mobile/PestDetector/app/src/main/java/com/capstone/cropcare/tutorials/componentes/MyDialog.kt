package com.capstone.cropcare.tutorials.componentes

import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.capstone.cropcare.R

@Composable
fun MyAlertDialog(modifier: Modifier = Modifier) {

    var showDialog: Boolean by remember { mutableStateOf(true) }

    if (showDialog) {

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = { Button(onClick = { showDialog = false }) { Text("Entendido!") } },
            dismissButton = {Button(onClick = {showDialog = false}) {Text("Cancelar") }},
            text = { Text("Se eliminara de forma permanente al trabajador x") },
            title = {Text("¿Estas seguro que quieres continuar?")},
            icon = { Icon(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = " ") },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White,
            iconContentColor = Color.Green,
            titleContentColor = Color.Black,
            textContentColor = Color.Gray,
            tonalElevation = 12.dp
        )

    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDateDialog(modifier: Modifier = Modifier) {

    var showDialog: Boolean by remember { mutableStateOf(true) }
    val calendar: Calendar = Calendar.getInstance()
    val datePickerState: DatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )


    if (showDialog){

        DatePickerDialog(
            onDismissRequest = {showDialog = false},
            confirmButton = {Button(onClick = {showDialog = false}) {Text(text = "Confirmar") }},
            colors = DatePickerDefaults.colors()
        ) {

            DatePicker(datePickerState)

        }

    }
    
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTimePicker(modifier: Modifier = Modifier) {

    var showTimePickerState: Boolean by remember { mutableStateOf(true) }
    val timePickerState: TimePickerState = rememberTimePickerState()

    if(showTimePickerState){
        Dialog(onDismissRequest = { showTimePickerState = false}) {
            Column(modifier = Modifier.background(Color.White).padding(24.dp)) {
                TimePicker(timePickerState)
            }
        }
    }
    
}