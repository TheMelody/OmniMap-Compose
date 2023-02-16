package com.melody.tencentmap.myapplication.repo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.melody.map.tencent_compose.overlay.PolylineRainbow
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.map.tencent_compose.utils.PathSmoothTool
import com.melody.sample.common.utils.SDKUtils
import com.tencent.tencentmap.mapsdk.maps.model.Animation
import com.tencent.tencentmap.mapsdk.maps.model.EmergeAnimation
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * MovementTrackRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/16 16:38
 */
object MovementTrackRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isScaleControlsEnabled = true,
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
            isZoomEnabled = true
        )
    }

    fun parseLocationsData(filePath: String): List<LatLng> {
        val locLists: MutableList<LatLng> = mutableListOf()
        var input: InputStream? = null
        var inputReader: InputStreamReader? = null
        var bufReader: BufferedReader? = null
        try {
            input = SDKUtils.getApplicationContext().assets.open(filePath)
            inputReader = InputStreamReader(input)
            bufReader = BufferedReader(inputReader)
            var line:String?
            while (bufReader.readLine().also { line = it } != null) {
                val strArray: Array<String>? = line?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                if (null != strArray) {
                    val newpoint = LatLng(strArray[0].toDouble(), strArray[1].toDouble())
                    if (locLists.size == 0 || newpoint.toString() !== locLists[locLists.size - 1].toString()) {
                        locLists.add(newpoint)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (bufReader != null) {
                    bufReader.close()
                    bufReader = null
                }
                if (inputReader != null) {
                    inputReader.close()
                    inputReader = null
                }
                if (input != null) {
                    input.close()
                    input = null
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // 轨迹平滑处理
        val pathSmoothTool = PathSmoothTool()
        // 设置平滑处理的等级
        pathSmoothTool.intensity = 4
        return pathSmoothTool.pathOptimize(locLists)
    }

    fun getTrackLatLngBounds(pointList: List<LatLng>): LatLngBounds {
        val b = LatLngBounds.builder()
        if (pointList.isEmpty()) {
            return b.build()
        }
        for (i in pointList.indices) {
            b.include(pointList[i])
        }
        return b.build()
    }

    /**
     * 彩虹线段配置
     */
    fun initPolylineRainbow(totalSize:Int): PolylineRainbow {
        return PolylineRainbow.create(
            colors = listOf(
                Color(0xFF9FD555).toArgb(),
                Color(0xFFD247EB).toArgb(),
                Color(0xFF41DD5B).toArgb(),
                Color(0xFFF38D0F).toArgb()
            ),
            // 腾讯地图内部会根据取完颜色值，填充到对应的index位置处
            indexes = listOf(0,totalSize/3,totalSize/8,totalSize)
        )
    }

    /**
     * 线段动画
     */
    fun initPolylineAnimation(startLatLng: LatLng,totalDuration:Int): Animation {
        return EmergeAnimation(startLatLng).apply {
            duration = totalDuration.toLong()
        }
    }

}