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

package com.melody.map.petal_compose.poperties

import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.model.LatLngBounds
import com.huawei.hms.maps.model.MyLocationStyle
import java.util.Objects

val DefaultMapProperties = MapProperties()

/**
 * 可在地图上修改属性的数据类
 * @param language 设置地图底图语言 [点击查看LanguageCode，请使用BCP 47那一栏的编码](https://developer.huawei.com/consumer/cn/doc/development/HMSCore-References/js-hwmap-0000001050990165#section144819546170)
 * @param isShowBuildings  是否显示3D楼块效果
 * @param showBuildingsTilt  此属性需配合[isShowBuildings]属性一起使用，当启用3D楼块效果之后，默认启用的倾斜角度为60°，读者自行修改后调用，关闭之后默认为0不可修改。
 * @param isIndoorEnabled  是否显示室内地图
 * @param isMyLocationEnabled 设置是否打开定位图层（myLocationOverlay）。
 * @param isTrafficEnabled 是否打开交通路况图层
 * @param myLocationStyle 设置定位图层（myLocationOverlay）的样式。
 * @param maxZoomPreference 设置地图最大缩放级别 缩放级别范围为[3, 20],超出范围将按最大级别计算
 * @param minZoomPreference 设置最小缩放级别 缩放级别范围为[3, 20],超出范围将按最小级别计算
 * @param mapShowLatLngBounds 设置地图显示范围，无论如何操作地图，显示区域都不能超过该矩形区域
 * @param mapType 设置地图模式，默认为：MAP_TYPE_NORMAL，[点击查看全部类型](https://developer.huawei.com/consumer/cn/doc/development/HMSCore-References/huaweimap-0000001050151757#section181450139302)
 */
class MapProperties(
    val language: String = "zh",
    val isShowBuildings: Boolean = false,
    val showBuildingsTilt: Float = 60F,
    val isIndoorEnabled: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val myLocationStyle: MyLocationStyle? = null,
    val maxZoomPreference: Float = 20.0F,
    val minZoomPreference: Float = 3F,
    val mapShowLatLngBounds: LatLngBounds? = null,
    val mapType: Int = HuaweiMap.MAP_TYPE_NORMAL,
) {

    override fun equals(other: Any?): Boolean = other is MapProperties &&
        language == other.language &&
        isShowBuildings == other.isShowBuildings &&
        showBuildingsTilt == other.showBuildingsTilt &&
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
        showBuildingsTilt,
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
        language: String = this.language,
        isShowBuildings: Boolean = this.isShowBuildings,
        showBuildingsTilt: Float = this.showBuildingsTilt,
        isIndoorEnabled: Boolean = this.isIndoorEnabled,
        isMyLocationEnabled: Boolean = this.isMyLocationEnabled,
        isTrafficEnabled: Boolean = this.isTrafficEnabled,
        myLocationStyle: MyLocationStyle? = this.myLocationStyle,
        maxZoomPreference: Float = this.maxZoomPreference,
        minZoomPreference: Float = this.minZoomPreference,
        mapShowLatLngBounds: LatLngBounds? = this.mapShowLatLngBounds,
        mapType: Int = this.mapType,
    ): MapProperties = MapProperties(
        language = language,
        isShowBuildings = isShowBuildings,
        showBuildingsTilt = showBuildingsTilt,
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
        return "MapProperties(language=$language, " +
                "isShowBuildings=$isShowBuildings, " +
                "showBuildingsTilt=$showBuildingsTilt, " +
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

