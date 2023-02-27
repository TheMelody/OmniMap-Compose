package com.melody.tencentmap.myapplication.repo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.melody.map.tencent_compose.model.MapType
import com.melody.map.tencent_compose.overlay.PolylineRainbow
import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import com.melody.tencentmap.myapplication.model.LogisticsRouteDataState
import com.tencent.lbssearch.TencentSearch
import com.tencent.lbssearch.httpresponse.HttpResponseListener
import com.tencent.lbssearch.`object`.param.DrivingParam
import com.tencent.lbssearch.`object`.result.DrivingResultObject
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * LogisticsRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/23 13:40
 */
object LogisticsRepository {
    /**
     * 勾选WebService API，点击签名校验，复制代码的话，【你要自己替换成你自己的SECRET_KEY】
     */
    private const val WEB_SERVICE_API_SECRET_KEY = "W79RgYY0lOIrzukvPoLM2E0DZjkKg4Cj"

    fun initMapUiSettings() : MapUiSettings {
        return MapUiSettings(
            isScrollGesturesEnabled = true,
            isZoomGesturesEnabled = true,
            isScaleControlsEnabled = true
        )
    }

    fun initMapProperties() : MapProperties {
        return MapProperties(mapType = MapType.NORMAL, enableMultipleInfoWindow = true)
    }

    private fun convertLatLngBounds(allPolyLines: List<LatLng>): LatLngBounds {
        val b: LatLngBounds.Builder = LatLngBounds.builder()
        for (point in allPolyLines) {
            b.include(point)
        }
        return b.build()
    }

    suspend fun getRoutePlan(fromPoint:LatLng, toPoint:LatLng): LogisticsRouteDataState {
        return suspendCancellableCoroutine { continuation ->
            val drivingParam = DrivingParam(fromPoint, toPoint) //创建导航参数
            drivingParam.roadType(DrivingParam.RoadType.ON_MAIN_ROAD)
            drivingParam.heading(90)
            drivingParam.accuracy(30)
            val tencentSearch = TencentSearch(
                SDKUtils.getApplicationContext(),
                WEB_SERVICE_API_SECRET_KEY
            )
            tencentSearch.getRoutePlan(drivingParam, object : HttpResponseListener<DrivingResultObject> {
                override fun onSuccess(p0: Int, p1 : DrivingResultObject?) {
                    if(p1?.result == null) {
                        continuation.resumeWith(Result.failure(NullPointerException("路线获取失败，reason：DrivingResultObject = null")))
                        return
                    }
                    val points= (p1.result.routes?.map { it.polyline }?: emptyList())
                    if(points.isEmpty()) {
                        continuation.resumeWith(Result.failure(NullPointerException("路线结果为空")))
                        return
                    }
                    // 这里模拟运送到3/4的位置，模拟模拟，请根据你自己的实际业务自己处理
                    // 这是模拟,这是模拟,这是模拟,这是模拟,这是模拟,这是模拟,这是模拟,这是模拟,这是模拟
                    val carLocation = points[0][points[0].size*3/4]
                    val carRotation = calcCarMoveRotation(carLocation, fromPoint)
                    continuation.resumeWith(Result.success(
                        LogisticsRouteDataState(
                            polylineWidth = 10F,
                            startPoint = fromPoint,
                            endPoint = toPoint,
                            carRotation = carRotation,
                            carLocation = carLocation,
                            // 这里模拟运送到3/4的位置了，实际应该根据当前位置和收货地址进行计算边界，模拟拼多多
                            latLngBounds = convertLatLngBounds(points[0].subList(points[0].size*3/4,points[0].size)),
                            rainbow = initPolylineRainbow(points[0].size),
                            points = points[0]
                        )
                    ))
                }

                override fun onFailure(p0: Int, p1: String?, p2: Throwable?) {
                    continuation.resumeWith(Result.failure(Throwable(p1)))
                }
            })
        }
    }

    /**
     * 计算物流的小车当前移动的方向，用于设置给Marker
     */
    private fun calcCarMoveRotation(
        locationLatLng: LatLng,
        fromPoint: LatLng
    ): Double {
        val slope =
            ((locationLatLng.latitude - fromPoint.latitude) / (locationLatLng.longitude - fromPoint.longitude))
        val radio: Double = Math.atan(slope)
        var angle: Double = 180 * (radio / Math.PI)
        angle = if (slope > 0) {
            if (locationLatLng.longitude < fromPoint.longitude) {
                -90 - angle
            } else {
                90 - angle
            }
        } else if (slope == 0.0) {
            if (locationLatLng.longitude < fromPoint.longitude) {
                -90.0
            } else {
                90.0
            }
        } else {
            if (locationLatLng.longitude < locationLatLng.latitude) {
                90 - angle
            } else {
                -90 - angle
            }
        }
        return angle
    }

    /**
     * 彩虹线段配置
     */
    fun initPolylineRainbow(totalSize:Int): PolylineRainbow {
        return PolylineRainbow.create(
            colors = listOf(
                Color(0xFF58C180).toArgb(),
                Color(0xFF99ECB9).toArgb(),
                Color(0xFF99ECB9).toArgb()
            ),
            indexes = listOf(0,totalSize*3/4,totalSize)
        )
    }

}