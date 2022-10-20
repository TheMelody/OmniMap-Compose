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

package com.melody.map.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.TileProvider
import com.amap.api.maps.model.UrlTileProvider
import com.google.accompanist.flowlayout.FlowRow
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.gd_compose.overlay.Arc
import com.melody.map.gd_compose.overlay.Circle
import com.melody.map.gd_compose.overlay.GroundOverlay
import com.melody.map.gd_compose.overlay.GroundOverlayPosition
import com.melody.map.gd_compose.overlay.MarkerInfoWindow
import com.melody.map.gd_compose.overlay.MarkerInfoWindowContent
import com.melody.map.gd_compose.overlay.Polygon
import com.melody.map.gd_compose.overlay.Polyline
import com.melody.map.gd_compose.overlay.TileOverlay
import com.melody.map.gd_compose.overlay.rememberMarkerState
import com.melody.map.gd_compose.position.rememberCameraPositionState
import com.melody.map.myapplication.R
import com.melody.sample.common.utils.showToast
import com.melody.ui.components.MapMenuButton
import java.net.URL

/**
 * OverlayScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/12 10:06
 */
@Composable
internal fun OverlayScreen() {
    val singaporeState = rememberMarkerState(position = LatLng(39.903787, 116.426095))
    val circleCenter by remember { mutableStateOf(LatLng(39.903787, 116.426095)) }
    val cameraPositionState = rememberCameraPositionState()
    var showWFJGroupOverlay by remember { mutableStateOf(false) }
    var showTileOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(showWFJGroupOverlay) {
        if(showWFJGroupOverlay) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(39.936713,116.386475), 18f))
        } else {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(39.91, 116.40), 11f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GDMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                isZoomGesturesEnabled = true,
                isScrollGesturesEnabled = true,
                isZoomEnabled = true
            )
        ) {
            MarkerInfoWindow(icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = rememberMarkerState(position = LatLng(39.93, 116.13)),
                content = {
                    Card(modifier = Modifier.size(66.dp)) {
                        Text(text = "我是一个卖报的小画家")
                    }
                }
            )

            MarkerInfoWindowContent(
                icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = singaporeState,
                title = "炎将军在此"
            ) {
                Card(modifier = Modifier.size(120.dp)) {
                    Text(it.title ?: "Title", color = Color.Red)
                }
            }

            Circle(
                center = circleCenter,
                fillColor = MaterialTheme.colors.secondary,
                strokeColor = MaterialTheme.colors.secondaryVariant,
                radius = 1000.0
            )

            Polygon(
                points = listOf(LatLng(39.88, 116.41), LatLng(39.87, 116.49), LatLng(39.82, 116.38)),
                strokeColor = Color.Red,
                fillColor = Color.Yellow
            )

            // 可使用:com.melody.map.gd_compose.utils.PathSmoothTool
            // 使轨迹更加平滑，如需,请参考：com.melody.map.myapplication.repo.MovingTrackRepository.readLatLngList里面的方法使用
            Polyline(
                points = listOf(
                    LatLng(39.92, 116.34),
                    LatLng(39.93, 116.34),
                    LatLng(39.92, 116.35)
                ),
                width = 10F,
                onClick = {
                    showToast("点击了Polyline,一共有：${it.points.size}个point")
                }
            )

            Arc(
                startPoint = LatLng(39.80, 116.09),
                passedPoint = LatLng(39.77, 116.28),
                endPoint = LatLng(39.78, 116.46),
                strokeColor = Color.Red
            )

            if(showWFJGroupOverlay) {
                // 覆盖物
                GroundOverlay(
                    position = GroundOverlayPosition.create(latLngBounds = LatLngBounds(LatLng(39.935029, 116.384377),LatLng(39.939577, 116.388331))),
                    image = BitmapDescriptorFactory.fromResource(R.drawable.groundoverlay),
                    transparency = 0.1F
                )
            }

            if(showTileOverlay) {
                // 贴图，像热力图也是用TileOverlay渲染的
                val tileProvider: TileProvider = object : UrlTileProvider(256, 256) {
                    override fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
                        return URL("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
                    }
                }
                TileOverlay(tileProvider = tileProvider)
            }
        }

        FlowRow(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3F)).padding(10.dp)) {
            MapMenuButton(
                onClick = { showWFJGroupOverlay = !showWFJGroupOverlay },
                text = "在王府井显示：GroundOverlay"
            )

            MapMenuButton(
                onClick = { showTileOverlay = !showTileOverlay },
                text = "显示：TileOverlay"
            )
        }
    }
}