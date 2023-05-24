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

package com.melody.map.baidu_compose.poperties

import com.baidu.mapapi.map.MapLanguage
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.model.LatLngBounds
import com.melody.map.baidu_compose.model.MapType
import java.util.Objects

val DefaultMapProperties = MapProperties()

/**
 * 可在地图上修改属性的数据类
 * @param language 设置地图底图语言
 * @param isShowBuildings  是否显示3D楼块效果
 * @param isShowMapLabels  是否显示底图标注
 * @param isShowMapIndoorLabels  设置室内图标注是否显示
 * @param enableMultipleInfoWindow 多窗口模式默认是关闭的，是否启用可以在地图上显示多个Marker覆盖物上方的信息窗口
 * @param isIndoorEnabled  是否显示室内地图，默认室内图不显示 室内图只有在缩放级别【**17, 22**】范围才生效，但是只有在18级之上（包含18级）才会有楼层边条显示
 * @param isMyLocationEnabled 设置是否打开定位图层（myLocationOverlay）。
 * @param isTrafficEnabled 是否打开交通路况图层
 * @param myLocationStyle 设置定位图层配置信息，只有先允许定位图层[isMyLocationEnabled]后,设置定位图层配置信息才会生效
 * @param maxZoomPreference 设置地图最大缩放级别，缩放级别范围为:【**4, 21**】
 * @param minZoomPreference 设置最小缩放级别，缩放级别范围为:【**4, 21**】
 * @param mapShowLatLngBounds 设置地图显示范围，无论如何操作地图，显示区域都不能超过该矩形区域,该方法执行完成之后，不可再调用[setMapStatus(MapStatusUpdate)]，否则【**可能会**造成可移动区域设置**不生效**】，方法可以放在[setMapStatus(MapStatusUpdate)]方法之前执行。
 * @param mapType 设置地图模式，默认为：[MapType.NORMAL]
 */
class MapProperties(
    val language: MapLanguage = MapLanguage.CHINESE,
    val isShowBuildings: Boolean = false,
    val isShowMapLabels: Boolean = true,
    val isShowMapIndoorLabels: Boolean = true,
    val enableMultipleInfoWindow: Boolean = false,
    val isIndoorEnabled: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val myLocationStyle: MyLocationConfiguration? = null,
    val maxZoomPreference: Float = 21.0F,
    val minZoomPreference: Float = 4F,
    val mapShowLatLngBounds: LatLngBounds? = null,
    val mapType: MapType = MapType.NORMAL,
) {

    override fun equals(other: Any?): Boolean = other is MapProperties &&
        language == other.language &&
        isShowBuildings == other.isShowBuildings &&
        isShowMapLabels == other.isShowMapLabels &&
        enableMultipleInfoWindow == other.enableMultipleInfoWindow &&
        isShowMapIndoorLabels == other.isShowMapIndoorLabels &&
        isIndoorEnabled == other.isIndoorEnabled &&
        isMyLocationEnabled == other.isMyLocationEnabled &&
        isTrafficEnabled == other.isTrafficEnabled &&
        myLocationStyle == other.myLocationStyle &&
        maxZoomPreference == other.maxZoomPreference &&
        minZoomPreference == other.minZoomPreference &&
        mapShowLatLngBounds == other.mapShowLatLngBounds &&
        mapType == other.mapType

    override fun hashCode(): Int = Objects.hash(
        language,
        isShowBuildings,
        isShowMapLabels,
        isShowMapIndoorLabels,
        enableMultipleInfoWindow,
        isIndoorEnabled,
        isMyLocationEnabled,
        isTrafficEnabled,
        myLocationStyle,
        maxZoomPreference,
        minZoomPreference,
        mapShowLatLngBounds,
        mapType
    )

    fun copy(
        language: MapLanguage = this.language,
        isShowBuildings: Boolean = this.isShowBuildings,
        isShowMapLabels: Boolean = this.isShowMapLabels,
        enableMultipleInfoWindow: Boolean = this.enableMultipleInfoWindow,
        isShowMapIndoorLabels: Boolean = this.isShowMapIndoorLabels,
        isIndoorEnabled: Boolean = this.isIndoorEnabled,
        isMyLocationEnabled: Boolean = this.isMyLocationEnabled,
        isTrafficEnabled: Boolean = this.isTrafficEnabled,
        myLocationStyle: MyLocationConfiguration? = this.myLocationStyle,
        maxZoomPreference: Float = this.maxZoomPreference,
        minZoomPreference: Float = this.minZoomPreference,
        mapShowLatLngBounds: LatLngBounds? = this.mapShowLatLngBounds,
        mapType: MapType = this.mapType,
    ): MapProperties = MapProperties(
        language = language,
        isShowBuildings = isShowBuildings,
        isShowMapLabels = isShowMapLabels,
        isShowMapIndoorLabels = isShowMapIndoorLabels,
        enableMultipleInfoWindow = enableMultipleInfoWindow,
        isIndoorEnabled = isIndoorEnabled,
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
                "language=$language, " +
                "isShowMapLabels=$isShowMapLabels, " +
                "isShowMapIndoorLabels=$isShowMapIndoorLabels, " +
                "enableMultipleInfoWindow=$enableMultipleInfoWindow, " +
                "isIndoorEnabled=$isIndoorEnabled, " +
                "isMyLocationEnabled=$isMyLocationEnabled, " +
                "isTrafficEnabled=$isTrafficEnabled, " +
                "myLocationStyle=$myLocationStyle, " +
                "maxZoomPreference=$maxZoomPreference, " +
                "minZoomPreference=$minZoomPreference, " +
                "mapShowLatLngBounds=$mapShowLatLngBounds, " +
                "mapType=$mapType)"
    }
}

