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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.melody.sample.common.model.ImmutableListWrapper

/**
 * BasicFeatureMenuBar
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/09 14:17
 */
@Composable
fun BasicFeatureMenuBar(
    modifier: Modifier,
    listWrapper: ImmutableListWrapper<String>,
    onStatusLabel:(@Composable (String) -> Unit),
    onItemClick: (String) -> Unit
) {
    var expandableState by rememberSaveable {  mutableStateOf(false) }
    Column(modifier = modifier) {
        MapMenuButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text =  if(expandableState) "收起" else "展开菜单",
            onClick = {
                expandableState = !expandableState
            }
        )
        ExpandableMenuList(
            visible = expandableState,
            listWrapper = listWrapper,
            onStatusLabel = onStatusLabel,
            onItemClick = onItemClick
        )
    }
}

@Composable
private fun ExpandableMenuList(
    visible: Boolean,
    listWrapper: ImmutableListWrapper<String>,
    onStatusLabel: (@Composable (String) -> Unit),
    onItemClick: (String) -> Unit
) {
    val currentOnItemClick by rememberUpdatedState(newValue = onItemClick)
    ExpandableBox(
        initialState = false,
        visible = visible,
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().verticalScroll( rememberScrollState()),
            mainAxisSpacing = 8.dp
        ) {
            listWrapper.items.forEach {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MapMenuButton(
                        text = it,
                        onClick = {
                            currentOnItemClick.invoke(it)
                        }
                    )
                    onStatusLabel(it)
                }
            }
        }
    }
}