package com.melody.bdmap.myapplication.repo

import com.baidu.mapapi.map.CircleHoleOptions
import com.baidu.mapapi.map.PolygonHoleOptions
import com.baidu.mapapi.model.LatLng


/**
 * OverlayRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/27 15:12
 */
object OverlayRepository {
    fun initPolygonPointList(): List<LatLng>{
        return listOf(
            LatLng(39.984864, 116.305756),
            LatLng(39.983618, 116.305848),
            LatLng(39.982347, 116.305966),
            LatLng(39.982412, 116.308111),
            LatLng(39.984122, 116.308224),
            LatLng(39.984955, 116.308099),
            LatLng(39.984864, 116.305756)
        )
    }
    fun initPolygonHoleOption(): PolygonHoleOptions{
        val holeOptions = PolygonHoleOptions()
        holeOptions.addPoints(listOf(
            LatLng(39.982347, 116.305966),
            LatLng(39.982412, 116.308111),
            LatLng(39.984122, 116.308224)
        ))
        return holeOptions
    }

    fun initCircleHoleOptions():CircleHoleOptions {
        return CircleHoleOptions().center(LatLng(39.97923, 116.357428)).radius(1800)
    }
}