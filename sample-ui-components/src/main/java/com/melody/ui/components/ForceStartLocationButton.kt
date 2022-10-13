package com.melody.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * ForceStartLocationButton
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/09 17:55
 */
@Composable
fun BoxScope.ForceStartLocationButton(onClick: () -> Unit) {
    val currentOnClick by rememberUpdatedState(newValue = onClick)
    Box(
        modifier = Modifier
            .size(60.dp)
            .align(Alignment.BottomEnd)
            .clickable(
                onClick = currentOnClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() })
    ) {
        Icon(
            modifier = Modifier.size(60.dp),
            tint = Color.Unspecified,
            painter = painterResource(id = R.drawable.ic_map_start_location),
            contentDescription = null
        )
    }
}