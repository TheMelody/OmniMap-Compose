package com.melody.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun EmptyResultText(modifier: Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            color = Color(0XFF555556),
            fontSize = 15.sp
        )
    )
}