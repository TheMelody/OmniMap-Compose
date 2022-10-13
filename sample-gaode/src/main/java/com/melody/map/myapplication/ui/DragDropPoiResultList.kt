package com.melody.map.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amap.api.services.core.PoiItemV2
import com.melody.ui.components.EmptyResultText
import com.melody.ui.components.MapPoiItem

@Composable
internal fun DragDropPoiResultList(
    poiItemList:  List<PoiItemV2>?,
    onItemClick: (PoiItemV2) -> Unit
) {
    val currentOnItemClick by rememberUpdatedState(newValue = onItemClick)
    Box(modifier = Modifier.fillMaxSize().background(Color(0XFFFAFAFC))) {
        poiItemList?.let { list ->
            if (list.isEmpty()) {
                EmptyResultText(
                    modifier = Modifier
                        .padding(15.dp)
                        .align(Alignment.Center),
                    text = "没有搜索到结果"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(15.dp)
                ) {
                    items(items = list, key = { it.poiId }) {
                        MapPoiItem(
                            title = it.title,
                            addressName = it.adName,
                            cityName = it.cityName,
                            snippet = it.snippet
                        ) {
                            currentOnItemClick.invoke(it)
                        }
                    }
                }
            }
        }
    }
}