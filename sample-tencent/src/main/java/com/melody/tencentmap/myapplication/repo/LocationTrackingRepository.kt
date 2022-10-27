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

package com.melody.tencentmap.myapplication.repo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.location.Location
import android.os.Looper
import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import com.melody.ui.components.R
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import com.tencent.tencentmap.mapsdk.maps.LocationSource
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle

/**
 * LocationTrackingRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 17:50
 */
object LocationTrackingRepository {

    fun initMyLocationStyle(): MyLocationStyle {
        // 注意：保证地图初始化完成之后，再调用，否则getActiveMapContext是null，fromBitmap返回的也是null
        val locationIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(SDKUtils.getApplicationContext().resources,R.drawable.ic_map_location_self))
        return MyLocationStyle().apply {
            // 设置小蓝点的图标
            icon(locationIcon)
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
            // 设置圆形的边框颜色
            strokeColor(Color.argb(100, 0, 0, 180))
            // 设置圆形的填充颜色
            fillColor(Color.argb(100, 0, 0, 180))
            // 设置圆形的边框粗细
            strokeWidth(1)
        }
    }

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
        )
    }

    inline fun initLocation(block: (TencentLocationManager?, TencentLocationRequest?) -> Unit) {
        //用于访问腾讯定位服务的类, 周期性向客户端提供位置更新
        val locationManager = TencentLocationManager.getInstance(SDKUtils.getApplicationContext())
        //设置坐标系
        locationManager?.coordinateType = TencentLocationManager.COORDINATE_TYPE_GCJ02
        //创建定位请求
        val locationRequest = TencentLocationRequest.create()
        //设置定位周期（位置监听器回调周期）为3s
        locationRequest?.interval = 3000
        block.invoke(locationManager, locationRequest)
    }

    inline fun requestLocationUpdates(
        locationManager: TencentLocationManager?,
        locationRequest: TencentLocationRequest?,
        locationListener: TencentLocationListener,
        block: (String?) -> Unit
    ) {
        when (locationManager?.requestLocationUpdates(locationRequest, locationListener, Looper.myLooper())) {
            1 -> block.invoke("设备缺少使用腾讯定位服务需要的基本条件")
            2 -> block.invoke("manifest 中配置的 key 不正确")
            3 -> block.invoke("自动加载libtencentloc.so失败")
            else -> block.invoke(null)
        }
    }

    inline fun handleLocationChange(
        errorCode: Int,
        tencentLocation: TencentLocation?,
        locationChangedListener: LocationSource.OnLocationChangedListener?,
        block: (TencentLocation) -> Unit
    ) {
        if (errorCode == TencentLocation.ERROR_OK && locationChangedListener != null && null != tencentLocation) {
            val location = Location(tencentLocation.provider)
            //设置经纬度以及精度
            location.latitude = tencentLocation.latitude
            location.longitude = tencentLocation.longitude
            location.accuracy = tencentLocation.accuracy
            //设置定位标的旋转角度，注意 tencentLocation.getBearing() 只有在 gps 时才有可能获取
            location.bearing = tencentLocation.bearing
            locationChangedListener.onLocationChanged(location)
            block.invoke(tencentLocation)
        }
    }
}