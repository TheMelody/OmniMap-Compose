// MIT License
//
// Copyright (c) 2023 被风吹过的夏天
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

package com.melody.tencentmap.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melody.map.tencent_compose.TXMap
import com.melody.map.tencent_compose.overlay.ClusterOverlay
import com.melody.map.tencent_compose.position.rememberCameraPositionState
import com.melody.sample.common.utils.showToast
import com.melody.tencentmap.myapplication.model.MapClusterItem
import com.melody.tencentmap.myapplication.viewmodel.MarkerClusterViewModel
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory

/**
 * MarkerClusterScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/01 09:46
 */
@Composable
internal fun MarkerClusterScreen() {
    val viewModel: MarkerClusterViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    Box(modifier = Modifier.fillMaxSize()) {
        TXMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiState.mapUiSettings,
            onMapLoaded = viewModel::handleMapLoaded
        ) {
            uiState.mapClusterItems?.let { items ->
                ClusterOverlay(
                    clusterItems = items,
                    clusterColor = Color(0xFF5AC95A),
                    onClusterItemClick = {
                        showToast("单个Marker被点击:"+it?.position.toString())
                        false
                    },
                    onClustersClick = {
                        // 你也可以，在这里自行缩放地图，触发：聚合点展开
                        showToast("聚合点被点击:"+it?.position.toString())
                        false
                    },
                    onClusterItemInfoWindow = { clusterItem ->
                        Column(modifier = Modifier
                            .width(100.dp)
                            .wrapContentHeight()
                            .background(Color.White)
                            .padding(16.dp)) {
                            if(clusterItem is MapClusterItem) {
                                // TAG数据，根据你自己的业务去做定制，懂？
                                Text(text = "我是单个Marker的ItemInfoWindow，获取TAG数据：${clusterItem.tag.toString()}")
                            }else {
                                // .....
                            }
                        }
                    }
                )
            }
        }
    }
}