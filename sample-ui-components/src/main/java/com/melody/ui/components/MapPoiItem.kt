package com.melody.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MapPoiItem(
    title: String?,
    cityName: String?,
    addressName: String?,
    snippet: String?,
    onItemClick: () -> Unit
) {
    val currentOnItemClick by rememberUpdatedState(newValue = onItemClick)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = currentOnItemClick
            )
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title ?: "-",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0XFF222126)
            )
        )
        Text(
            text = (cityName ?: "").plus(addressName ?: "").plus(snippet ?: ""),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontSize = 12.5.sp,
                color = Color(0XFF555556)
            )
        )
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.66.dp,
            color = Color(0x73DEDFE1)
        )
    }
}