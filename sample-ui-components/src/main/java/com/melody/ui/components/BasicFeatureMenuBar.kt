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
 * @author TheMelody
 * email developer_melody@163.com
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