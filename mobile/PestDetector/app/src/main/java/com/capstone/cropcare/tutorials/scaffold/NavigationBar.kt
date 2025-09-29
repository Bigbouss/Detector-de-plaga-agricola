package com.capstone.cropcare.tutorials.scaffold

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.tutorials.componentes.NavItem

@Composable
fun MyNavigationBar(modifier: Modifier = Modifier) {

    val itemlist = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Fav", Icons.Default.Favorite),
        NavItem("Profile", Icons.Default.Person)
    )

    var selectedIndex: Int by remember { mutableIntStateOf(0) }

    NavigationBar(
        containerColor = Color.Red,
        tonalElevation = 10.dp

    ){

        itemlist.forEachIndexed { index, item ->
            ItemsMenu(navItem = item, isSelected = index == selectedIndex){
                selectedIndex = index
            }
        }



    }

}


@Composable
fun RowScope.ItemsMenu (navItem: NavItem, isSelected: Boolean, onItemClick:() -> Unit) {
    NavigationBarItem(
        selected = isSelected,
        onClick = {onItemClick()},
        icon = {
            Icon(
                navItem.icon,
                contentDescription = " "
            )
        },
        label = { Text(navItem.name) })
}