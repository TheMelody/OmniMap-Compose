package com.melody.bdmap.myapplication.utils

import android.graphics.Color
import android.os.Bundle
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.map.PolylineDottedLineType
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.route.BikingRouteLine
import com.baidu.mapapi.search.route.DrivingRouteLine
import com.baidu.mapapi.search.route.DrivingRouteLine.DrivingStep
import com.baidu.mapapi.search.route.TransitRouteLine
import com.baidu.mapapi.search.route.WalkingRouteLine


/**
 * BDRoutePlanUtils
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/05/12 11:50
 */
object BDRoutePlanUtils {

    fun getOverlayLatLngBounds(fromPoint:LatLng, toPoint:LatLng): LatLngBounds {
        val builder = LatLngBounds.Builder().include(fromPoint).include(toPoint)
        return builder.build()
    }

    fun getDrivingOverlayOptions(routeLine: DrivingRouteLine?): List<OverlayOptions>? {
        if (routeLine == null) {
            return null
        }
        val overlayOptions: MutableList<OverlayOptions> = ArrayList()
        if (routeLine.allStep != null && routeLine.allStep.size > 0) {
            val steps: List<DrivingStep> = routeLine.allStep
            val stepNum = steps.size
            val points: MutableList<LatLng> = ArrayList()
            val traffics = ArrayList<Int>()
            var totalTraffic = 0
            for (i in 0 until stepNum) {
                if (i == stepNum - 1) {
                    points.addAll(steps[i].wayPoints)
                } else {
                    points.addAll(steps[i].wayPoints.subList(0, steps[i].wayPoints.size - 1))
                }
                totalTraffic += steps[i].wayPoints.size - 1
                if (steps[i].trafficList != null && steps[i].trafficList.isNotEmpty()) {
                    for (j in steps[i].trafficList.indices) {
                        traffics.add(steps[i].trafficList[j])
                    }
                }
            }
            var isDotLine = false
            if (traffics.size > 0) {
                isDotLine = true
            }
            val option = PolylineOptions()
                .points(points).textureIndex(traffics)
                .width(7).dottedLine(isDotLine).zIndex(0)
            overlayOptions.add(option)
        }
        return overlayOptions
    }

    fun getBusOverlayOptions(routeLine: TransitRouteLine?): List<OverlayOptions>? {
        if (routeLine == null) {
            return null
        }
        val overlayOptions: MutableList<OverlayOptions> = ArrayList()
        routeLine.allStep?.forEach { step ->
            if (step.wayPoints != null) {
                val isWalking =
                    step.stepType == TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING
                overlayOptions.add(
                    PolylineOptions()
                        .extraInfo(Bundle().apply { putBoolean("render_point", isWalking) })
                        .points(step.wayPoints)
                        .dottedLine(isWalking)
                        .dottedLineType(PolylineDottedLineType.DOTTED_LINE_CIRCLE)
                        .lineCapType(PolylineOptions.LineCapType.LineCapRound)
                        .zIndex(0)
                )
            }
        }
        return overlayOptions
    }

    fun getWalkOverlayOptions(routeLine: WalkingRouteLine?): List<OverlayOptions>? {
        if (routeLine == null) {
            return null
        }
        val overlayOptions: MutableList<OverlayOptions> = ArrayList()
        var lastStepLastPoint: LatLng? = null
        routeLine.allStep?.forEach { step ->
            val watPoints = step.wayPoints
            if (watPoints != null) {
                val points: MutableList<LatLng> = ArrayList()
                if (lastStepLastPoint != null) {
                    points.add(lastStepLastPoint!!)
                }
                points.addAll(watPoints)
                overlayOptions.add(
                    PolylineOptions().points(points)
                        .dottedLine(true)
                        .dottedLineType(PolylineDottedLineType.DOTTED_LINE_CIRCLE)
                        .lineCapType(PolylineOptions.LineCapType.LineCapRound)
                        .zIndex(0)
                )
                lastStepLastPoint = watPoints[watPoints.size - 1]
            }
        }
        return overlayOptions
    }

    fun getRideOverlayOptions(routeLine: BikingRouteLine?): List<OverlayOptions>? {
        if (routeLine == null) {
            return null
        }
        val overlayOptions: MutableList<OverlayOptions> = ArrayList()
        var lastStepLastPoint: LatLng? = null
        routeLine.allStep?.forEach { step ->
            val watPoints = step.wayPoints
            if (watPoints != null) {
                val points: MutableList<LatLng> = ArrayList()
                if (lastStepLastPoint != null) {
                    points.add(lastStepLastPoint!!)
                }
                points.addAll(watPoints)
                overlayOptions.add(
                    PolylineOptions()
                    .points(points).zIndex(0)
                )
                lastStepLastPoint = watPoints[watPoints.size - 1]
            }
        }
        return overlayOptions
    }

    fun getDrivingRouteSelectTextureList(): List<BitmapDescriptor> {
        val list = ArrayList<BitmapDescriptor>()
        list.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_default_selected.png"))
        list.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_green_selected.png"))
        list.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_yellow_selected.png"))
        list.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_red_selected.png"))
        list.add(BitmapDescriptorFactory.fromAsset("Icon_road_nofocus.png"))
        return list
    }

    fun getDrivingRouteUnSelectTextureList(): List<BitmapDescriptor> {
        val list = ArrayList<BitmapDescriptor>()
        list.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_default.png"))
        list.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_green.png"))
        list.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_yellow.png"))
        list.add(BitmapDescriptorFactory.fromAsset("ic_map_route_status_red.png"))
        list.add(BitmapDescriptorFactory.fromAsset("Icon_road_nofocus.png"))
        return list
    }

    fun getOtherRouteSelectTexture(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromAsset("ic_map_route_status_green_selected.png")
    }

    fun getOtherRouteUnSelectTexture(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromAsset("ic_map_route_status_green.png")
    }
}