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

package com.melody.map.baidu_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.map.Polyline
import com.baidu.mapapi.model.LatLngBounds
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.kernel.KernelRouteOverlay
import com.melody.map.baidu_compose.model.BDMapComposable
import com.melody.map.baidu_compose.model.RoutePlanType

internal class RoutePlanOverlayNode(
    val routePlanOverlay: KernelRouteOverlay? = null,
    var onPolylineClick: (Polyline) -> Boolean
) : MapNode {
    override fun onRemoved() {
        routePlanOverlay?.removeFromMap()
    }
}

/**
 * 步行路径规划
 * @param routeWidth 路径的宽度
 * @param isSelected 路径是否被选中
 * @param latLngBounds 视野范围边距
 * @param overlayOptions OverlayOptions集合
 * @param selectedTexture 选中状态下步行线路纹理图片
 * @param unSelectedTexture 未选中状态下步行线路纹理图片
 * @param onPolylineClick polyline的点击事件回调
 */
@Composable
@BDMapComposable
fun WalkRouteOverlay(
    routeWidth: Int = 18,
    isSelected: Boolean = false,
    latLngBounds: LatLngBounds,
    overlayOptions: List<OverlayOptions>,
    polylineColor: Color = Color(0XFF5584EB),
    selectedTexture: BitmapDescriptor?,
    unSelectedTexture: BitmapDescriptor?,
    onPolylineClick: (Polyline) -> Boolean = { false }
){
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<RoutePlanOverlayNode, MapApplier>(
        factory = {
            val baiduMap = mapApplier?.map?: error("Error adding WalkRouteOverlay")
            val walkRouteOverlay = KernelRouteOverlay(
                baiduMap = baiduMap,
                isSelected = isSelected,
                routeWidth = routeWidth,
                latLngBounds = latLngBounds,
                polylineColor = polylineColor,
                routePlanType = RoutePlanType.WALKING,
                selectTextureList = listOf(selectedTexture),
                unSelectTextureList = listOf(unSelectedTexture),
                overlayOptions = overlayOptions
            )
            walkRouteOverlay.addToMap()
            RoutePlanOverlayNode(
                routePlanOverlay = walkRouteOverlay,
                onPolylineClick = onPolylineClick
            )
        },
        update = {
            update(onPolylineClick) { this.onPolylineClick = it }

            set(isSelected) { this.routePlanOverlay?.setPolylineSelected(it) }
        }
    )
}

/**
 * 骑行路径规划
 * @param routeWidth 路径的宽度
 * @param isSelected 路径是否被选中
 * @param latLngBounds 视野范围边距
 * @param overlayOptions OverlayOptions集合
 * @param selectedTexture 选中状态下骑行线路纹理图片
 * @param unSelectedTexture 未选中状态下骑行线路纹理图片
 * @param onPolylineClick polyline的点击事件回调
 */
@Composable
@BDMapComposable
fun RideRouteOverlay(
    routeWidth: Int = 18,
    isSelected: Boolean = false,
    latLngBounds: LatLngBounds,
    overlayOptions: List<OverlayOptions>,
    polylineColor: Color = Color(0XFF5584EB),
    selectedTexture: BitmapDescriptor?,
    unSelectedTexture: BitmapDescriptor?,
    onPolylineClick: (Polyline)  -> Boolean = { false }
){
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<RoutePlanOverlayNode, MapApplier>(
        factory = {
            val baiduMap = mapApplier?.map?: error("Error adding RideRouteOverlay")
            val rideRouteOverlay = KernelRouteOverlay(
                baiduMap = baiduMap,
                isSelected = isSelected,
                routeWidth = routeWidth,
                latLngBounds = latLngBounds,
                polylineColor = polylineColor,
                routePlanType = RoutePlanType.BIKING,
                selectTextureList = listOf(selectedTexture),
                unSelectTextureList = listOf(unSelectedTexture),
                overlayOptions = overlayOptions
            )
            rideRouteOverlay.addToMap()
            RoutePlanOverlayNode(
                routePlanOverlay = rideRouteOverlay,
                onPolylineClick = onPolylineClick
            )
        },
        update = {
            update(onPolylineClick) { this.onPolylineClick = it }

            set(isSelected) { this.routePlanOverlay?.setPolylineSelected(it) }
        }
    )
}

/**
 * 公交车路径规划
 * @param routeWidth 路径的宽度
 * @param isSelected 路径是否被选中
 * @param latLngBounds 视野范围边距
 * @param overlayOptions OverlayOptions集合
 * @param selectedTexture 选中状态下公交线路纹理图片
 * @param unSelectedTexture 未选中状态下公交线路纹理图片
 * @param onPolylineClick polyline的点击事件回调
 */
@Composable
@BDMapComposable
fun BusRouteOverlay(
    routeWidth: Int = 18,
    isSelected: Boolean = false,
    latLngBounds: LatLngBounds,
    overlayOptions: List<OverlayOptions>,
    polylineColor: Color = Color(0XFF5584EB),
    selectedTexture: BitmapDescriptor?,
    unSelectedTexture: BitmapDescriptor?,
    onPolylineClick: (Polyline)  -> Boolean = { false }
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<RoutePlanOverlayNode, MapApplier>(
        factory = {
            val baiduMap = mapApplier?.map?: error("Error adding BusRouteOverlay")
            val busRouteOverlay = KernelRouteOverlay(
                baiduMap = baiduMap,
                isSelected = isSelected,
                routeWidth = routeWidth,
                latLngBounds = latLngBounds,
                polylineColor = polylineColor,
                routePlanType = RoutePlanType.BUS_LINE,
                selectTextureList = listOf(selectedTexture),
                unSelectTextureList = listOf(unSelectedTexture),
                overlayOptions = overlayOptions
            )
            busRouteOverlay.addToMap()
            RoutePlanOverlayNode(
                routePlanOverlay = busRouteOverlay,
                onPolylineClick = onPolylineClick
            )
        },
        update = {
            update(onPolylineClick) { this.onPolylineClick = it }

            set(isSelected) { this.routePlanOverlay?.setPolylineSelected(it) }
        }
    )
}

/**
 * 驾车规划的路径
 * @param routeWidth 路径的宽度
 * @param isSelected 路径是否被选中
 * @param latLngBounds 视野范围边距
 * @param overlayOptions OverlayOptions集合
 * @param selectTextureList 选中状态下驾车线路纹理图片
 * @param unSelectTextureList 选中状态下驾车线路纹理图片
 * @param onPolylineClick polyline的点击事件回调
 */
@Composable
@BDMapComposable
fun DrivingRouteOverlay(
    routeWidth: Int = 18,
    isSelected: Boolean = false,
    latLngBounds: LatLngBounds,
    overlayOptions: List<OverlayOptions>,
    polylineColor: Color = Color(0XFF5584EB),
    selectTextureList: List<BitmapDescriptor?>?,
    unSelectTextureList: List<BitmapDescriptor?>?,
    onPolylineClick: (Polyline)  -> Boolean = { false }
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<RoutePlanOverlayNode, MapApplier>(
        factory = {
            val baiduMap = mapApplier?.map?: error("Error adding DrivingRouteOverlay")
            val drivingRouteOverlay = KernelRouteOverlay(
                baiduMap = baiduMap,
                isSelected = isSelected,
                routeWidth = routeWidth,
                latLngBounds = latLngBounds,
                polylineColor = polylineColor,
                routePlanType = RoutePlanType.DRIVING,
                selectTextureList = selectTextureList,
                unSelectTextureList = unSelectTextureList,
                overlayOptions = overlayOptions
            )
            drivingRouteOverlay.addToMap()
            RoutePlanOverlayNode(
                routePlanOverlay = drivingRouteOverlay,
                onPolylineClick = onPolylineClick
            )
        },
        update = {
            update(onPolylineClick) { this.onPolylineClick = it }

            set(isSelected) { this.routePlanOverlay?.setPolylineSelected(it) }
        }
    )
}

