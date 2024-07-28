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

package com.melody.bdmap.myapplication.ui

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.TileProvider
import com.baidu.mapapi.map.UrlTileProvider
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.google.accompanist.flowlayout.FlowRow
import com.melody.bdmap.myapplication.R
import com.melody.bdmap.myapplication.viewmodel.OverlayViewModel
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.extensions.getSnippetExt
import com.melody.map.baidu_compose.extensions.getTitleExt
import com.melody.map.baidu_compose.overlay.Arc
import com.melody.map.baidu_compose.overlay.Circle
import com.melody.map.baidu_compose.overlay.CircleGradient
import com.melody.map.baidu_compose.overlay.GroundOverlay
import com.melody.map.baidu_compose.overlay.GroundOverlayPosition
import com.melody.map.baidu_compose.overlay.Marker
import com.melody.map.baidu_compose.overlay.MarkerInfoWindow
import com.melody.map.baidu_compose.overlay.MarkerInfoWindowContent
import com.melody.map.baidu_compose.overlay.Polygon
import com.melody.map.baidu_compose.overlay.Polyline
import com.melody.map.baidu_compose.overlay.PolylineCustomTexture
import com.melody.map.baidu_compose.overlay.PolylineRainbow
import com.melody.map.baidu_compose.overlay.TextOverlay
import com.melody.map.baidu_compose.overlay.TileOverlay
import com.melody.map.baidu_compose.overlay.rememberMarkerState
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.map.baidu_compose.position.rememberCameraPositionState
import com.melody.sample.common.utils.showToast
import com.melody.ui.components.MapMenuButton

/**
 * OverlayScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/12 10:06
 */
@Composable
internal fun OverlayScreen() {
    val viewModel: OverlayViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(currentState.isShowWFJGroupOverlay) {
        if(currentState.isShowWFJGroupOverlay) {
            cameraPositionState.move(MapStatusUpdateFactory.newLatLngZoom(currentState.wfjCenter, 18f))
        } else {
            cameraPositionState.move(MapStatusUpdateFactory.newLatLngZoom(currentState.mapCenter, 11f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BDMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                isZoomGesturesEnabled = true,
                isScrollGesturesEnabled = true,
                isDoubleClickZoomEnabled = true,
                isZoomEnabled = true
            )
        ) {
            MarkerInfoWindow(icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = rememberMarkerState(position = currentState.infoWindowLatLng),
                infoWindowYOffset = -60,
                title = "我是一个卖报的小画家，嘎嘎香",
                content = {
                    Card(modifier = Modifier.requiredSizeIn(maxWidth = 88.dp, minHeight = 66.dp)) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = it.getTitleExt() ?: ""
                        )
                    }
                }
            )

            MarkerInfoWindowContent(
                icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = rememberMarkerState(position = currentState.lvbuCenter),
                snippet = "头戴三叉束发紫金冠体挂西川红棉百花袍身披兽面吞头连环铠腰系勒甲玲珑狮蛮带手持方天画戟坐下嘶风赤兔马之吕小布是也。"
            ) {
                FlowRow(modifier = Modifier
                    .width(120.dp)
                    .wrapContentHeight()) {
                    Text(it.getSnippetExt() ?: "", color = Color.Red)
                    Image(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = null
                    )
                }
            }


            Polygon(
                points = currentState.polygonTriangleList,
                strokeColor = Color.Red,
                fillColor = Color.Yellow
            )

            Marker(
                icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = rememberMarkerState(position = currentState.polygonCornerLatLng),
                title = "看到了不？",
                snippet = "我的下方还有个多边形记得放大查看!",
                zIndex = 1
            )

            Polygon(
                // 看到了，我在这~~~
                points = currentState.polygonPointList,
                strokeColor = Color(0XFF1033F6),
                fillColor = Color(0X4DDAD589),
                strokeWidth = 5,
                isDottedStroke = true,
                // 阅读到此处的同学，自行尝试holeOption效果哦
                //holeOption = currentState.polygonHoleOption
            )

            PolylineRainbow(
                useGradient = true,
                rainbow = PolylineRainbow.create(colors = currentState.polylineColorList, points = currentState.polylineList),
                width = 15,
                zIndex = 2,
                onClick = {
                    showToast("点击了Polyline,一共有：${it.points.size}个point")
                    true
                }
            )

            // 读者请自己，运行下面的代码
            /*val points: MutableList<LatLng> = ArrayList()
            points.add(LatLng(39.865, 116.444))
            points.add(LatLng(39.825, 116.494))
            points.add(LatLng(39.855, 116.534))
            points.add(LatLng(39.805, 116.594))

            val textureList: MutableList<BitmapDescriptor> = ArrayList()
            // 保证线段纹理图片的大小 16*64px，每个图片大小一致，否则会出现有空格，还有【只能放在assets目录】，否则也可能会有空格
            textureList.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_deepred_selected.png"))
            textureList.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_red_selected.png"))
            textureList.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_yellow_selected.png"))

            val indexList: MutableList<Int> = ArrayList()
            indexList.add(0)
            indexList.add(1)
            indexList.add(2)
            PolylineCustomTexture(
                width = 20,
                customTexture = PolylineCustomTexture.create(
                    points = points,
                    textureList = textureList,
                    indexList = indexList
                ),
                onClick = {
                    showToast("你大爷,始终是你大爷")
                    false
                }
            )*/

            Arc(
                startPoint = currentState.arcStartPoint,
                passedPoint = currentState.arcPassPoint,
                endPoint = currentState.arcEndPoint,
                strokeColor = Color.Red
            )

            TextOverlay(
                text = "百度地图文字覆盖物",
                position = currentState.textOverlayPos,
                fontSize = 26,
                fontColor = Color(0xFFF7F7F7),
                // TODO: 百度地图SDK更新之后，这个属性失效了，等百度官方给答复
                backgroundColor = Color(0xFF00C3E6),
            )

            Circle(
                radius = 2800,
                isDottedStroke = true,
                center = currentState.holeCirclePos,
                fillColor = Color(0x4DFF0000),
                strokeColor = Color(0xFF0088FF),
                holeOptions = currentState.circleHoleOptions
            )

            CircleGradient(
                radius = 2800,
                center = currentState.circleGradientPos,
                centerColor = Color(0x4D5DFAE8),
                strokeWidth = 8,
                strokeColor = Color(0x7AFFC000),
                sideColor = Color(0xFF5DE9CC)
            )

            if(currentState.isShowWFJGroupOverlay) {
                // 覆盖物
                GroundOverlay(
                    position = GroundOverlayPosition.create(latLngBounds = currentState.wfjLatLngBounds),
                    image = BitmapDescriptorFactory.fromResource(R.drawable.groundoverlay),
                    transparency = 0.8F
                )
            }

            if(currentState.isShowTileOverlay) {
                val tileProvider: TileProvider = object : UrlTileProvider() {
                    override fun getMaxDisLevel(): Int = 21

                    override fun getMinDisLevel(): Int = 4

                    override fun getTileUrl(): String {
                        return "http://online1.map.bdimg.com/tile/?qt=vtile&x={x}&y={y}&z={z}&styles=pl&scaler=1&udt=20190528"
                    }
                }
                TileOverlay(
                    tileProvider = tileProvider,
                    // 构造显示瓦片图范围，当前为世界范围
                    latLngBounds = LatLngBounds.Builder()
                        .include(LatLng(80.0, 180.0))
                        .include(LatLng(-80.0, -180.0)).build()
                )
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