package com.capstone.cropcare.tutorials.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.tutorials.componentes.states.CheckBoxStates


@Composable
fun CheckBoxParent(modifier: Modifier = Modifier) {
    var state: List<CheckBoxStates> by remember {
        mutableStateOf(
            listOf(
                CheckBoxStates("terms", "aceptar terminos y condiciones"),
                CheckBoxStates("newsletter", "Recibir Newsletter", true),
                CheckBoxStates("update", "Recibir update"),
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {

        state.forEach { myState ->
            CheckBoxWithText(checkBoxStates = myState) {
                state = state.map {
                    if (it.id == myState.id) {
                        myState.copy(check = !myState.check)
                    } else {
                        it
                    }

                }
            }
        }

    }

}


@Composable
fun MySwitch(modifier: Modifier) {
    var switchState: Boolean by remember { mutableStateOf(true) }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Switch(
            checked = switchState,
            onCheckedChange = { switchState = it },
            thumbContent = { Text(text = "D") },//Puede ir cualquier composable, como Icon por ejemplo
            enabled = true,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Red)
        )
    }
}

@Composable
fun MyCheckBox(modifier: Modifier = Modifier) {
    var state: Boolean by remember { mutableStateOf(true) }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { state = !state }) {
            Checkbox(
                checked = state,
                onCheckedChange = { state = it },
                enabled = true,
            )
            Spacer(Modifier.width(12.dp))
            Text(text = "Aceptar codiciones de uso.")
        }
    }
}


@Composable
fun CheckBoxWithText(
    modifier: Modifier = Modifier,
    checkBoxStates: CheckBoxStates,
    onCheckedChange: (CheckBoxStates) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onCheckedChange(checkBoxStates) }) {
        Checkbox(
            checked = checkBoxStates.check,
            onCheckedChange = { onCheckedChange(checkBoxStates) },
            enabled = true,
        )
        Spacer(Modifier.width(12.dp))
        Text(checkBoxStates.label)
    }
}

//parent
@Composable
fun TriCheckBox(modifier: Modifier = Modifier) {

    var parentState: ToggleableState by remember { mutableStateOf(ToggleableState.Off) }
    var child1: Boolean by remember { mutableStateOf(false) }
    var child2: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(child1, child2) {
        parentState = when {
            child1 && child2 -> ToggleableState.On  //cuando todos sus hijos son true, tambien es true
            !child1 && !child2 -> ToggleableState.On //cuando todos sus hijos son false, tambien es false
            else -> ToggleableState.Indeterminate //si no se cumple las condiciones queda indeterminado
        }
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriStateCheckbox(parentState, onClick = {
                val newState = parentState != ToggleableState.On
                child1 = newState
                child2 = child1

            })
            Text("Seleccionar Todo")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Checkbox(child1, onCheckedChange = { child1 = it })
            Text("Ejemplo 1")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Checkbox(child2, onCheckedChange = { child2 = it })
            Text("Ejemplo 2")
        }
    }

}


@Composable
fun MyRatioButton(modifier: Modifier = Modifier) {

    var state: Boolean by remember { mutableStateOf(false) }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        RadioButton(

            selected = true,
            onClick = { state = true },
            enabled = true,
            colors = RadioButtonDefaults.colors(selectedColor = Color.Red)
        )
        Text("Seleccionar")
    }
}

@Composable
fun MyRadioButtonList(modifier: Modifier = Modifier) {

    var selectedName by remember { mutableStateOf("") }

    Column {
        RadioButtonComponent(name = "Box 1", selectedName = selectedName) {selectedName = it }
        RadioButtonComponent(name = "Test", selectedName = selectedName) {selectedName = it }
        RadioButtonComponent(name = "LALA", selectedName = selectedName) {selectedName = it }
        RadioButtonComponent(name = "BCir", selectedName = selectedName) {selectedName = it }

    }

}

@Composable
fun RadioButtonComponent(name: String, selectedName: String, onItemSelected: (String) -> Unit) {
    Row {
        RadioButton(selected = name == selectedName, onClick = { onItemSelected(name) })
        Text(name)
    }

}