package com.capstone.cropcare.tutorials.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun MyBasicList(modifier: Modifier = Modifier, onItemClick: (String) -> Unit) {

    val names: List<String> = listOf(
        "Abradolf Lincler",
        "Baby Legs",
        "Beta VII",
        " Beth Smith",
        "Birdperson",
        "Evil Morty",
        "Gazorpazorpfield",
        "Jerry's Mytholog",
        " Krombopulos Michael",
        "Lighthouse Chief",
        "Mr. Needful",
        "Revolio 'Gearhead' Clockberg, Jr.",
        "Rick Sanchez",
        " Scary Terry",
        "Shrimply Pibbles",
        "Squanchy",
        "Stealy",
        "Summer Smith",
        "Tammy Gueterman",
        "Tinkles"
    )
    LazyColumn {  //Es como recycler view, si uso LazyRow es lo mismo pero en horizontal
        items(names) {
            Text(
                it, modifier = Modifier
                    .padding(24.dp)
                    .clickable { onItemClick(it) }
            )
        }
    }

}

@Composable
fun MyAdvanceList(modifier: Modifier = Modifier) {
    var items: List<String> by remember { mutableStateOf(List(100) { "Item número $it" }) }

    LazyColumn {
        itemsIndexed(items, key = { _, item -> item }) { index, item ->
            Row {
                Text(text = item + " item $index")
                Spacer(modifier = Modifier.weight(1f))
                TextButton({
                    items = items.toMutableList().apply {
                        remove(item)
                    }
                }) { Text("Eliminar") }
                Spacer(modifier = Modifier.width(24.dp))
            }
        }
    }
}

@Composable
fun ScrollList(modifier: Modifier = Modifier) {
    val listState: LazyListState = rememberLazyListState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 5 }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        LazyColumn(state = listState) {
            items(100) {
                Text("Item $it", modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp))
            }
        }
    }

    if (showButton) {
        FloatingActionButton(
            onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
            modifier = Modifier.padding(16.dp)) {

            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = " ")
        }
    }

}

@Composable
fun MyGridList(modifier: Modifier = Modifier) {
    val numbers: MutableState<List<Int>> = remember { mutableStateOf(List(50){ Random.nextInt(0, 6)}) }
    val colors: List<Color> = listOf(
        Color(0xFFF44336),
        Color(0xFF9C27B0),
        Color(0xFF673AB7),
        Color(0xFF3F51B5),
        Color(0xFFFFEB3B),
        Color(0xFF4CAF50),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().padding(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(numbers.value) { randomNumber ->
            Box(modifier = Modifier.background(colors[randomNumber]),
                contentAlignment = Alignment.Center
                ){
                Text(randomNumber.toString(), color = Color.White, fontSize = 25.sp)
            }
        }
    }
}