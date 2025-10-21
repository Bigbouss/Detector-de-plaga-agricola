@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.adminViews.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.components.CropCardAdmin
import com.capstone.cropcare.view.core.components.CropCardItemListWorker

@Composable
fun HomeAdminScreen(goInvitationCode: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        CropCardAdmin(
            textTitle = stringResource(R.string.admin_home_screen_card_title),
            iconCard = R.drawable.ic_add_worker,
            iconAction = {goInvitationCode()},
            modifier = Modifier
                .padding(vertical = 20.dp)
                .padding(horizontal = 5.dp)
        ) {
            CropCardItemListWorker(
                nameWorker = "Test",
                emailWorker = "email.com"
            )
        }


    }
}
    
