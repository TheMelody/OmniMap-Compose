package com.melody.map.baidu_compose.utils

import com.baidu.mapapi.model.LatLng
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.sqrt

/**
 * BDTrackMoveUtils
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/16 15:55
 */
class BDTrackMoveUtils {

    companion object {
        private const val DISTANCE = 0.00002
    }

    // 循环中暂停移动标识
    private var isPause: Boolean = true

    /**
     * 开始移动的【开关标识】
     */
    fun startMove() {
        this.isPause = false
    }

    /**
     * 暂停移动的【开关标识】
     */
    fun pauseMove() {
        this.isPause = true
    }

    /**
     * 从头开始移动，由于是在循环中进行位置更新，所以：【**暂停和恢复**】，请使用：[pauseMove]以及[startMove]
     * @param timeInterval 移动的间隔时长
     * @param points 移动的路径顶点坐标
     * @param onPositionCallback 移动的位置坐标回调
     * @param onRotateCallback 移动的时候需要旋转的角度回调
     */
    suspend fun restart(
        timeInterval: Long,
        points: List<LatLng>,
        onPositionCallback: (Boolean,LatLng) -> Unit,
        onRotateCallback: (Float) -> Unit
    ) {
        for (i in 0 until points.size - 1) {
            calcMoveData(
                timeInterval = timeInterval,
                startPoint = points[i],
                endPoint = points[i + 1],
                onPositionCallback = {
                    onPositionCallback.invoke((i == points.size - 2),it)
                },
                onRotateCallback = onRotateCallback
            )
        }
    }

    private suspend fun calcMoveData(
        timeInterval: Long,
        startPoint: LatLng,
        endPoint: LatLng,
        onPositionCallback: (LatLng) -> Unit,
        onRotateCallback: (Float) -> Unit
    ) {
        onPositionCallback(startPoint)
        onRotateCallback(getAngle(startPoint, endPoint))
        val slope = getSlope(startPoint, endPoint)
        // 是不是正向的标示
        val isYReverse = startPoint.latitude > endPoint.latitude
        val isXReverse = startPoint.longitude > endPoint.longitude
        val intercept = getInterception(slope, startPoint)
        val xMoveDistance =
            if (isXReverse) getXMoveDistance(slope) else -1 * getXMoveDistance(slope)
        val yMoveDistance =
            if (isYReverse) getYMoveDistance(slope) else -1 * getYMoveDistance(slope)
        var j = startPoint.latitude
        var k = startPoint.longitude
        while (!((j > endPoint.latitude) xor isYReverse) && !((k > endPoint.longitude) xor isXReverse)) {
            var latLng: LatLng?
            when (slope) {
                Double.MAX_VALUE -> {
                    latLng = LatLng(j, k)
                    j -= yMoveDistance
                }
                0.0 -> {
                    latLng = LatLng(j, k - xMoveDistance)
                    k -= xMoveDistance
                }
                else -> {
                    latLng = LatLng(j, (j - intercept) / slope)
                    j -= yMoveDistance
                }
            }
            val finalLatLng: LatLng = latLng
            if (finalLatLng.latitude == 0.0 && finalLatLng.longitude == 0.0) {
                continue
            }
            onPositionCallback(finalLatLng)
            var done = false
            while(!done) {
                // 暂停移动的标识
                if (!isPause) done = true
            }
            delay(timeInterval)
        }
    }

    /**
     * 返回当前位置计算出来的角度
     */
    fun getAngle(startIndex: Int, points: List<LatLng>): Float {
        if (startIndex + 1 >= points.size) {
            throw RuntimeException("index out of bonds")
        }
        val startPoint: LatLng = points[startIndex]
        val endPoint: LatLng = points[startIndex + 1]
        return getAngle(startPoint, endPoint)
    }

    /**
     * 根据两点算取图标转的角度
     */
    fun getAngle(
        fromPoint: LatLng,
        toPoint: LatLng
    ): Float {
        val slope: Double = getSlope(fromPoint, toPoint)
        if (slope == Double.MAX_VALUE) {
            return if (toPoint.latitude > fromPoint.latitude) {
                0F
            } else {
                180F
            }
        } else if (slope == 0.0) {
            return if (toPoint.longitude > fromPoint.longitude) {
                (-90).toFloat()
            } else {
                90F
            }
        }
        var deltAngle = 0f
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180f
        }
        val radio = atan(slope)
        return (180 * (radio / Math.PI) + deltAngle - 90).toFloat()
    }

    /**
     * 根据点和斜率算取截距
     */
    private fun getInterception(
        slope: Double,
        point: LatLng
    ): Double {
        return point.latitude - slope * point.longitude
    }

    /**
     * 算斜率
     */
    private fun getSlope(
        fromPoint: LatLng,
        toPoint: LatLng
    ): Double {
        return if (toPoint.longitude == fromPoint.longitude) {
            Double.MAX_VALUE
        } else (toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude)
    }

    /**
     * 计算x方向每次移动的距离
     */
    private fun getXMoveDistance(slope: Double): Double {
        return if (slope == Double.MAX_VALUE || slope == 0.0) {
            DISTANCE
        } else abs(DISTANCE * 1 / slope / sqrt(1 + 1 / (slope * slope)))
    }

    /**
     * 计算y方向每次移动的距离
     */
    private fun getYMoveDistance(slope: Double): Double {
        return if (slope == Double.MAX_VALUE || slope == 0.0) {
            DISTANCE
        } else abs(DISTANCE * slope / sqrt(1 + slope * slope))
    }

}