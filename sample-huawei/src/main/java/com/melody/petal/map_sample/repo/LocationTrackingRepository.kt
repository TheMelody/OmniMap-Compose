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

package com.melody.petal.map_sample.repo

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.LocationManager
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.MyLocationStyle
import com.melody.map.petal_compose.poperties.MapProperties
import com.melody.map.petal_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import com.melody.ui.components.R

/**
 * LocationTrackingRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/9/6 11:01
 */
object LocationTrackingRepository {

    fun checkGPSIsOpen(): Boolean {
        val locationManager: LocationManager? = SDKUtils.getApplicationContext()
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?: false
    }

    fun initMapProperties() : MapProperties {
        val iconBitmap = BitmapFactory.decodeResource(SDKUtils.getApplicationContext().resources, R.drawable.ic_map_location_self)
        val locationIcon = BitmapDescriptorFactory.fromBitmap(iconBitmap)
        return MapProperties(
            isMyLocationEnabled = false, // 需要先授权，否则闪退
            myLocationStyle = MyLocationStyle().apply {
                // 设置小蓝点的图标
                myLocationIcon(locationIcon)
                // 设置圆形的填充颜色
                radiusFillColor(Color.argb(100, 0, 0, 180))
            }
        )
    }

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            myLocationButtonEnabled = true,
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
        )
    }
}