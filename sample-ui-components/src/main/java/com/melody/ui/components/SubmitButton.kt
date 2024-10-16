// MIT License
//
// Copyright (c) 2022 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 提交按钮
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 10:58
 */
@Composable
fun SubmitButton(
    modifier: Modifier,
    enabled: Boolean = true,
    @StringRes buttonTextRes: Int? = null,
    buttonText: String? = null,
    buttonHeight: Dp,
    shapeRadius: Dp = 4.dp,
    fontSize: TextUnit = 14.sp,
    background:Color = Color(0xFF668EF7),
    textColor:Color = Color(0XFFFFFFFF),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = ripple()
    Box(modifier = modifier
        .height(buttonHeight)
        .background(
            color = background,
            shape = RoundedCornerShape(shapeRadius)
        )
        .clip(RoundedCornerShape(shapeRadius))
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = indication
        ) {
            onClick.invoke()
        }
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = if (null != buttonTextRes) stringResource(id = buttonTextRes) else buttonText
                ?: "",
            style = TextStyle(
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium
            )
        )
    }
}