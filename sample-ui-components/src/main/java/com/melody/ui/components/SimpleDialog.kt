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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 统一的Dialog样式
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 10:54
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimpleDialog(
    @StringRes positiveButtonResId: Int,
    @StringRes negativeButtonResId: Int,
    content: String,
    onPositiveClick:()->Unit,
    onNegativeClick:()->Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(283.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(4.dp)),
            shape = RoundedCornerShape(4.dp),
            color = Color(0XFFFFFFFF)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .padding(19.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = content,
                        style = TextStyle(
                            color = Color(0XFF333333),
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(start = 19.dp, end = 19.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .height(42.dp)
                ) {

                    SubmitButton(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                        buttonHeight = 42.dp,
                        background = Color(0XFFF0F2F5),
                        textColor = Color(0xFF668EF7),
                        buttonTextRes = negativeButtonResId,
                        onClick = onNegativeClick
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    SubmitButton(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                        background = Color(0xFF668EF7),
                        textColor = Color(0XFFFFFFFF),
                        buttonHeight = 42.dp,
                        buttonTextRes = positiveButtonResId,
                        onClick = onPositiveClick
                    )
                }
            }
        }
    }
}