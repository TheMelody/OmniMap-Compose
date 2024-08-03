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

package com.melody.petal.map_sample.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.MapStyleOptions
import com.huawei.hms.maps.model.SquareCap
import com.huawei.hms.maps.model.TileProvider
import com.huawei.hms.maps.model.UrlTileProvider
import com.melody.map.petal_compose.HWMap
import com.melody.map.petal_compose.overlay.*
import com.melody.map.petal_compose.poperties.MapProperties
import com.melody.map.petal_compose.poperties.MapUiSettings
import com.melody.map.petal_compose.position.rememberCameraPositionState
import com.melody.petal.map_sample.viewmodel.OverlayViewModel
import com.melody.petal.myapplication.R
import com.melody.sample.common.utils.showToast
import com.melody.ui.components.MapMenuButton
import java.net.URL

/**
 * OverlayScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/08/27 16:02
 */
@Composable
internal fun OverlayScreen() {
    val viewModel: OverlayViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    var mapProperties by remember { mutableStateOf(MapProperties()) }

    LaunchedEffect(uiState.isShowWFJGroupOverlay) {
        mapProperties = if(uiState.isShowWFJGroupOverlay) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(uiState.wfjCenter, 18f))
            mapProperties.copy(mapShowLatLngBounds = uiState.wfjLatLngBounds)
        } else {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(uiState.mapCenter, 11f))
            mapProperties.copy(mapShowLatLngBounds = null)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HWMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                isZoomGesturesEnabled = !uiState.isShowWFJGroupOverlay,
                isScrollGesturesEnabled = true,
                isZoomEnabled = true
            ),
            properties = mapProperties
        ) {
            MarkerInfoWindow(icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = rememberMarkerState(position = uiState.infoWindowLatLng),
                title = "我是一个卖报的小画家，嘎嘎香",
                content = {
                    Card(modifier = Modifier.requiredSizeIn(maxWidth = 88.dp, minHeight = 66.dp)) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = it.title?:""
                        )
                    }
                }
            )

            MarkerInfoWindowContent(
                icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = rememberMarkerState(position = uiState.circleCenter),
                snippet = "头戴三叉束发紫金冠体挂西川红棉百花袍身披兽面吞头连环铠腰系勒甲玲珑狮蛮带手持方天画戟坐下嘶风赤兔马之吕小布是也"
            ) {
                FlowRow(modifier = Modifier
                    .width(120.dp)
                    .wrapContentHeight()) {
                    Text(it.snippet ?: "", color = Color.Red)
                    Image(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(id = R.mipmap.ic_nabi),
                        contentDescription = null
                    )
                }
            }

            Circle(
                center = uiState.circleCenter,
                fillColor = MaterialTheme.colors.secondary,
                strokeColor = MaterialTheme.colors.secondaryVariant,
                radius = 1000.0,
                onCircleClick = {
                    showToast("没错,华为圆形也可以点击，遥遥领先~~~")
                }
            )

            Polygon(
                points = uiState.polygonTriangleList,
                strokeColor = Color.Red,
                fillColor = Color.Yellow,
                isClickable = false
            )

            Marker(
                icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = rememberMarkerState(position = uiState.polygonCornerLatLng),
                title = "看到了不？",
                snippet = "我的下方还有个多边形记得放大查看!",
                zIndex = 1F
            )

            Polygon(
                // 看到了，我在这~~~
                points = uiState.polygonHolePointList,
                strokeColor = Color(0XFF1033F6),
                fillColor = Color(0X4DDAD589),
                strokeWidth = 5F,
                patternItems = uiState.polygonPatterns,
                isClickable = true,
                onPolygonClick = {
                    showToast("很好，这个Polygon是可以点击的,遥遥领先~~~")
                }
            )

            PolylineRainbow(
                points = uiState.polylineAnimPointList,
                width = 8F,
                patternItems = emptyList(),
                rainbow = uiState.polylineRainbow,
                startCap = SquareCap(),
                useGradient = true,
                onClick = {
                    showToast("彩虹线段Polyline,一共有：${it.points.size}个point")
                }
            )

            Polyline(
                points = uiState.polylineList,
                width = 10F,
                onClick = {
                    showToast("点击了Polyline,一共有：${it.points.size}个point")
                }
            )

            if(uiState.isShowWFJGroupOverlay) {
                // 覆盖物
                GroundOverlay(
                    position = GroundOverlayPosition.create(latLngBounds = uiState.wfjLatLngBounds),
                    image = BitmapDescriptorFactory.fromResource(R.drawable.groundoverlay),
                    transparency = 0.1F,
                    onGroundOverlayClick = {
                        showToast("点击了GroundOverlay：$it")
                    }
                )
            }

            if(uiState.isShowTileOverlay) {
                val tileProvider: TileProvider = object : UrlTileProvider(256, 256) {
                    override fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
                        return URL("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
                    }
                }
                TileOverlay(tileProvider = tileProvider)
            }
        }

        FlowRow(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3F))
            .padding(10.dp)) {
            MapMenuButton(
                onClick = viewModel::toggleWFJGroupOverlay,
                text = "在王府井显示：GroundOverlay"
            )

            MapMenuButton(
                onClick = viewModel::toggleTileOverlay,
                text = "显示：TileOverlay"
            )
        }
    }
}