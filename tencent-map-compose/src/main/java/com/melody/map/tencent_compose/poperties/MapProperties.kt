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

package com.melody.map.tencent_compose.poperties

import com.melody.map.tencent_compose.model.MapType
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle
import java.util.Objects

val DefaultMapProperties = MapProperties()

/**
 * @param isShowBuildings  是否显示3D楼块效果
 * @param isIndoorEnabled  是否显示室内地图，目前室内图这玩意，权限申请【没有开通在线申请】，如需要请联系室内图商务协助办理：
 *                         https://lbs.qq.com/mobile/androidMapSDK/developerGuide/indoor
 * @param enableMultipleInfoWindow 多窗口模式默认是关闭的，是否启用可以在地图上显示多个Marker覆盖物上方的信息窗口
 * @param isMyLocationEnabled 设置是否打开定位图层（myLocationOverlay）
 * @param isTrafficEnabled 是否打开交通路况图层
 * @param myLocationStyle 设置定位图层（myLocationOverlay）的样式
 * @param maxZoomPreference 设置地图最大缩放级别 缩放级别范围为[3, 20],超出范围将按最大级别计算
 * @param minZoomPreference 设置最小缩放级别 缩放级别范围为[3, 20],超出范围将按最小级别计算
 * @param mapShowLatLngBounds 设置地图显示范围，无论如何操作地图，显示区域都不能超过该矩形区域
 * @param mapType 设置地图模式，默认为：MAP_TYPE_NORMAL
 */
class MapProperties(
    val isShowBuildings: Boolean = false,
    val isIndoorEnabled: Boolean = false,
    val enableMultipleInfoWindow: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val myLocationStyle: MyLocationStyle? = null,
    val maxZoomPreference: Float = 21.0F,
    val minZoomPreference: Float = 0F,
    val mapShowLatLngBounds: LatLngBounds? = null,
    val mapType: MapType = MapType.NORMAL,
) {

    override fun equals(other: Any?): Boolean = other is MapProperties &&
        isShowBuildings == other.isShowBuildings &&
        isIndoorEnabled == other.isIndoorEnabled &&
        enableMultipleInfoWindow == other.enableMultipleInfoWindow &&
        isMyLocationEnabled == other.isMyLocationEnabled &&
        isTrafficEnabled == other.isTrafficEnabled &&
        myLocationStyle == other.myLocationStyle &&
        maxZoomPreference == other.maxZoomPreference &&
        minZoomPreference == other.minZoomPreference &&
        mapShowLatLngBounds == other.mapShowLatLngBounds &&
        mapType == other.mapType

    override fun hashCode(): Int = Objects.hash(
        isShowBuildings,
        isIndoorEnabled,
        enableMultipleInfoWindow,
        isMyLocationEnabled,
        isTrafficEnabled,
        myLocationStyle,
        maxZoomPreference,
        minZoomPreference,
        mapShowLatLngBounds,
        mapType
    )

    fun copy(
        isShowBuildings: Boolean = this.isShowBuildings,
        isIndoorEnabled: Boolean = this.isIndoorEnabled,
        enableMultipleInfoWindow: Boolean = this.enableMultipleInfoWindow,
        isMyLocationEnabled: Boolean = this.isMyLocationEnabled,
        isTrafficEnabled: Boolean = this.isTrafficEnabled,
        myLocationStyle: MyLocationStyle? = this.myLocationStyle,
        maxZoomPreference: Float = this.maxZoomPreference,
        minZoomPreference: Float = this.minZoomPreference,
        mapShowLatLngBounds: LatLngBounds? = this.mapShowLatLngBounds,
        mapType: MapType = this.mapType,
    ): MapProperties = MapProperties(
        isShowBuildings = isShowBuildings,
        isIndoorEnabled = isIndoorEnabled,
        enableMultipleInfoWindow = enableMultipleInfoWindow,
        isMyLocationEnabled = isMyLocationEnabled,
        isTrafficEnabled = isTrafficEnabled,
        myLocationStyle = myLocationStyle,
        maxZoomPreference = maxZoomPreference,
        minZoomPreference = minZoomPreference,
        mapShowLatLngBounds = mapShowLatLngBounds,
        mapType = mapType,
    )

    override fun toString(): String {
        return "MapProperties(isShowBuildings=$isShowBuildings, " +
                "isIndoorEnabled=$isIndoorEnabled, " +
                "enableMultipleInfoWindow=$enableMultipleInfoWindow, " +
                "isMyLocationEnabled=$isMyLocationEnabled, " +
                "isTrafficEnabled=$isTrafficEnabled, " +
                "myLocationStyle=$myLocationStyle, " +
                "maxZoomPreference=$maxZoomPreference, " +
                "minZoomPreference=$minZoomPreference, " +
                "mapShowLatLngBounds=$mapShowLatLngBounds, " +
                "mapType=$mapType)"
    }
}

