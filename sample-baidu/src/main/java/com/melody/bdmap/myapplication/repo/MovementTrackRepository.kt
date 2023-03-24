package com.melody.bdmap.myapplication.repo

import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.melody.bdmap.myapplication.utils.PathSmoothTool
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * MovementTrackRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/20 11:38
 */
object MovementTrackRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isScaleControlsEnabled = true,
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true
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
        val b = LatLngBounds.Builder()
        if (pointList.isEmpty()) {
            return b.build()
        }
        for (i in pointList.indices) {
            b.include(pointList[i])
        }
        return b.build()
    }
}