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

package com.melody.map.gd_compose.kernel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.*

/**
 * 核心取自【高德路径规划】示例代码里面的BusRouteOverlay，公交线路路径规划
 */
internal class KernelBusRouteOverlay(
    aMap: AMap,
    isSelected: Boolean,
    routeWidth: Float,
    polylineColor: Color,
    busLineSelectedTexture: BitmapDescriptor,
    busLineUnSelectedTexture: BitmapDescriptor,
    walkLineSelectedTexture: BitmapDescriptor?,
    walkLineUnSelectedTexture: BitmapDescriptor?,
    busNodeIcon: BitmapDescriptor?,
    walkNodeIcon: BitmapDescriptor?,
    startPoint: LatLng,
    endPoint: LatLng,
    private val busPath: BusPathV2
) : KernelRouteOverlay(
    aMap = aMap,
    isSelected = isSelected,
    routeWidth = routeWidth,
    polylineColor = polylineColor,
    walkLineSelectedTexture = walkLineSelectedTexture,
    walkLineUnSelectedTexture = walkLineUnSelectedTexture,
    busLineSelectedTexture = busLineSelectedTexture,
    busLineUnSelectedTexture = busLineUnSelectedTexture,
    driveLineSelectedTexture = null,
    driveLineUnSelectedTexture = null,
    busNodeIcon = busNodeIcon,
    walkNodeIcon = walkNodeIcon,
    driveNodeIcon = null,
    startPoint = startPoint,
    endPoint = endPoint
) {
    companion object {
        private const val TAG = "BusRouteOverlay"
    }
    private var latLng: LatLng? = null
    private var isSelectedBusLine = isSelected

    /**
     * 添加公交路线到地图上。
     *
     * 绘制节点和线，细节情况较多
     *
     * 两个step之间，用step和step1区分
     *
     * 1.一个step内可能有步行和公交，然后有可能他们之间连接有断开
     *
     * 2.step的公交和step1的步行，有可能连接有断开
     *
     * 3.step和step1之间是公交换乘，且没有步行，需要把step的终点和step1的起点连起来
     *
     * 4.公交最后一站和终点间有步行，加入步行线路，还会有一些步行marker
     *
     * 5.公交最后一站和终点间无步行，之间连起来
     */
    fun addToMap(fromCreateNode: Boolean) = asyncLaunch {
        removeFromMap()
        val result = kotlin.runCatching {
            val busSteps = busPath.steps
            for (i in busSteps.indices) {
                val busStep = busSteps[i]
                if (i < busSteps.size - 1) {
                    // 取得当前下一个BusStep对象
                    val busStep1 = busSteps[i + 1]
                    // 假如步行和公交之间连接有断开，就把步行最后一个经纬度点和公交第一个经纬度点连接起来，避免断线问题
                    if (busStep.walk != null
                        && busStep.busLines != null
                    ) {
                        checkWalkToBusline(busStep)
                    }

                    // 假如公交和步行之间连接有断开，就把上一公交经纬度点和下一步行第一个经纬度点连接起来，避免断线问题
                    if (busStep.busLines != null && busStep1.walk != null && busStep1.walk.steps.size > 0) {
                        checkBusLineToNextWalk(busStep, busStep1)
                    }
                    // 假如两个公交换乘中间没有步行，就把上一公交经纬度点和下一步公交第一个经纬度点连接起来，避免断线问题
                    if (busStep.busLines != null && busStep1.walk == null && busStep1.busLines != null) {
                        checkBusEndToNextBusStart(busStep, busStep1)
                    }
                    // 和上面的很类似
                    if (busStep.busLines != null && busStep1.walk == null && busStep1.busLines != null) {
                        checkBusToNextBusNoWalk(busStep, busStep1)
                    }
                    if (busStep.busLines != null
                        && busStep1.railway != null
                    ) {
                        checkBusLineToNextRailway(busStep, busStep1)
                    }
                    if (busStep1.walk != null && busStep1.walk.steps.size > 0 && busStep.railway != null) {
                        checkRailwayToNextWalk(busStep, busStep1)
                    }
                    if (busStep1.railway != null &&
                        busStep.railway != null
                    ) {
                        checkRailwayToNextRailway(busStep, busStep1)
                    }
                    if (busStep.railway != null &&
                        busStep1.taxi != null
                    ) {
                        checkRailwayToNextTaxi(busStep, busStep1)
                    }
                }
                if (busStep.walk != null
                    && busStep.walk.steps.size > 0
                ) {
                    addWalkSteps(busStep)
                } else {
                    if (busStep.busLines == null && busStep.railway == null && busStep.taxi == null) {
                        addWalkPolyline(latLng, endPoint)
                    }
                }
                if (busStep.busLines != null) {
                    val routeBusLineItem = busStep.busLines.getOrNull(0)
                    addBusLineSteps(routeBusLineItem)
                    addBusStationMarkers(routeBusLineItem)
                    if (i == busSteps.size - 1) {
                        addWalkPolyline(convertToLatLng(getLastBuslinePoint(busStep)), endPoint)
                    }
                }
                if (busStep.railway != null) {
                    addRailwayStep(busStep.railway)
                    addRailwayMarkers(busStep.railway)
                    if (i == busSteps.size - 1) {
                        addWalkPolyline(
                            convertToLatLng(busStep.railway.arrivalstop.location),
                            endPoint
                        )
                    }
                }
                if (busStep.taxi != null) {
                    addTaxiStep(busStep.taxi)
                    addTaxiMarkers(busStep.taxi)
                }
            }
        }
        if (fromCreateNode && isSelected) {
            zoomToSpan(boundsPadding = 200)
        }
        if (result.isFailure) {
            Log.e(TAG, "addToMap", result.exceptionOrNull())
        }
    }

    private fun checkRailwayToNextTaxi(busStep: BusStepV2, busStep1: BusStepV2) {
        val railwayLastPoint = busStep.railway.arrivalstop.location
        val taxiFirstPoint = busStep1.taxi.origin
        if (railwayLastPoint != taxiFirstPoint) {
            addWalkPolyLineByLatLonPoints(railwayLastPoint, taxiFirstPoint)
        }
    }

    private fun checkRailwayToNextRailway(busStep: BusStepV2, busStep1: BusStepV2) {
        val railwayLastPoint = busStep.railway.arrivalstop.location
        val railwayFirstPoint = busStep1.railway.departurestop.location
        if (railwayLastPoint != railwayFirstPoint) {
            addWalkPolyLineByLatLonPoints(railwayLastPoint, railwayFirstPoint)
        }
    }

    private fun checkBusLineToNextRailway(busStep: BusStepV2, busStep1: BusStepV2) {
        val busLastPoint = getLastBuslinePoint(busStep)
        val railwayFirstPoint = busStep1.railway.departurestop.location
        if (busLastPoint != railwayFirstPoint) {
            addWalkPolyLineByLatLonPoints(busLastPoint, railwayFirstPoint)
        }
    }

    private fun checkRailwayToNextWalk(busStep: BusStepV2, busStep1: BusStepV2) {
        val railwayLastPoint = busStep.railway.arrivalstop.location
        val walkFirstPoint = getFirstWalkPoint(busStep1)
        if (railwayLastPoint != walkFirstPoint) {
            addWalkPolyLineByLatLonPoints(railwayLastPoint, walkFirstPoint)
        }
    }

    private fun addRailwayStep(railway: RouteRailwayItem) {
        val railwaylistpoint: MutableList<LatLng> = ArrayList()
        val railwayStationItems: MutableList<RailwayStationItem> = ArrayList()
        railwayStationItems.add(railway.departurestop)
        railwayStationItems.addAll(railway.viastops)
        railwayStationItems.add(railway.arrivalstop)
        for (i in railwayStationItems.indices) {
            convertToLatLng(railwayStationItems[i].location)?.let { railwaylistpoint.add(it) }
        }
        //TODO:
        //addRailwayPolyline(railwaylistpoint)
    }

    private fun addTaxiStep(taxi: TaxiItemV2) {
        addPolyLine(
            PolylineOptions().width(routeWidth)
                .zIndex(if(isSelectedBusLine) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
                .setCustomTexture(if(isSelectedBusLine) busLineSelectedTexture else busLineUnSelectedTexture)
                .add(convertToLatLng(taxi.origin))
                .add(convertToLatLng(taxi.destination))
        )
    }

    private fun addWalkSteps(busStep: BusStepV2) {
        val routeBusWalkItem = busStep.walk
        val walkSteps = routeBusWalkItem.steps
        for (j in walkSteps.indices) {
            val walkStep = walkSteps[j]
            if (j == 0) {
                val latLng = convertToLatLng(walkStep.polyline[0])
                val road = walkStep.road // 道路名字
                val instruction = getWalkSnippet(walkSteps) // 步行导航信息
                addWalkStationMarkers(latLng, road, instruction)
            }
            val listWalkPolyline: List<LatLng> = convertArrList(walkStep.polyline)
            latLng = listWalkPolyline[listWalkPolyline.size - 1]
            addWalkPolyline(listWalkPolyline)

            // 假如步行前一段的终点和下的起点有断开，断画直线连接起来，避免断线问题
            if (j < walkSteps.size - 1) {
                val lastLatLng = listWalkPolyline[listWalkPolyline.size - 1]
                val firstlatLatLng = convertToLatLng(walkSteps[j + 1].polyline[0])
                if (lastLatLng != firstlatLatLng) {
                    addWalkPolyline(lastLatLng, firstlatLatLng)
                }
            }
        }
    }

    /**
     * 添加一系列的bus PolyLine
     */
    private fun addBusLineSteps(routeBusLineItem: RouteBusLineItem?) {
        addBusLineSteps(routeBusLineItem?.polyline?: emptyList())
    }

    private fun addBusLineSteps(listPoints: List<LatLonPoint>) {
        if (listPoints.isEmpty()) {
            return
        }
        addPolyLine(
            PolylineOptions().width(routeWidth)
                .zIndex(if(isSelectedBusLine) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
                .setCustomTexture(if(isSelectedBusLine) busLineSelectedTexture else busLineUnSelectedTexture)
                .addAll(convertArrList(listPoints))
        )
    }

    private fun addWalkStationMarkers(
        latLng: LatLng?,
        title: String,
        snippet: String
    ) {
        addStationMarker(
            MarkerOptions().position(latLng).title(title)
                .snippet(snippet).anchor(0.5f, 0.5f).visible(null != walkNodeIcon && nodeIconVisible)
                .icon(walkNodeIcon)
        )
    }

    private fun addBusStationMarkers(routeBusLineItem: RouteBusLineItem?) {
        if(null == routeBusLineItem) return
        val startBusStation = routeBusLineItem
            .departureBusStation
        val position = convertToLatLng(
            startBusStation
                .latLonPoint
        )
        val title = routeBusLineItem.busLineName
        val snippet = getBusSnippet(routeBusLineItem)
        addStationMarker(
            MarkerOptions().position(position).title(title)
                .snippet(snippet).anchor(0.5f, 0.5f).visible(null != busNodeIcon && nodeIconVisible)
                .icon(busNodeIcon)
        )
    }

    private fun addTaxiMarkers(taxiItem: TaxiItemV2) {
        val position = convertToLatLng(taxiItem.origin)
        val title = taxiItem.getmSname() + "打车"
        val snippet = "到终点"
        addStationMarker(
            MarkerOptions().position(position).title(title)
                .snippet(snippet).anchor(0.5f, 0.5f).visible(null != driveNodeIcon && nodeIconVisible)
                .icon(driveNodeIcon)
        )
    }

    private fun addRailwayMarkers(railway: RouteRailwayItem) {
        val departureposition = convertToLatLng(railway.departurestop.location)
        val departuretitle = railway.departurestop.name + "上车"
        val departuresnippet = railway.name
        addStationMarker(
            MarkerOptions()
                .position(departureposition)
                .title(departuretitle)
                .snippet(departuresnippet)
                .anchor(0.5f, 0.5f)
                .visible(null != busNodeIcon && nodeIconVisible)
                .icon(busNodeIcon)
        )
        val arrivalposition = convertToLatLng(railway.arrivalstop.location)
        val arrivaltitle = railway.arrivalstop.name + "下车"
        val arrivalsnippet = railway.name
        addStationMarker(
            MarkerOptions()
                .position(arrivalposition)
                .title(arrivaltitle)
                .snippet(arrivalsnippet)
                .anchor(0.5f, 0.5f)
                .visible(null != busNodeIcon && nodeIconVisible)
                .icon(busNodeIcon)
        )
    }

    /**
     * 如果换乘没有步行 检查bus最后一点和下一个step的bus起点是否一致
     */
    private fun checkBusToNextBusNoWalk(busStep: BusStepV2, busStep1: BusStepV2) {
        val endbusLatLng = convertToLatLng(getLastBuslinePoint(busStep))
        val startbusLatLng = convertToLatLng(getFirstBuslinePoint(busStep1))
        if (((startbusLatLng?.latitude?: 0.0) - (endbusLatLng?.latitude?: 0.0) > 0.0001)
            || ((startbusLatLng?.longitude?: 0.0) - (endbusLatLng?.longitude?: 0.0) > 0.0001)
        ) {
            // 断线用带箭头的直线连
            drawLineArrow(endbusLatLng, startbusLatLng)
        }
    }

    /**
     *
     * checkBusToNextBusNoWalk 和这个类似
     */
    private fun checkBusEndToNextBusStart(busStep: BusStepV2, busStep1: BusStepV2) {
        val busLastPoint = getLastBuslinePoint(busStep)
        val endbusLatLng = convertToLatLng(busLastPoint)
        val busFirstPoint = getFirstBuslinePoint(busStep1)
        val startbusLatLng = convertToLatLng(busFirstPoint)
        if (endbusLatLng != startbusLatLng) {
            drawLineArrow(endbusLatLng, startbusLatLng)
        }
    }

    /**
     * 检查bus最后一步和下一各step的步行起点是否一致
     */
    private fun checkBusLineToNextWalk(busStep: BusStepV2, busStep1: BusStepV2) {
        val busLastPoint = getLastBuslinePoint(busStep)
        val walkFirstPoint = getFirstWalkPoint(busStep1)
        if (busLastPoint != walkFirstPoint) {
            addWalkPolyLineByLatLonPoints(busLastPoint, walkFirstPoint)
        }
    }

    /**
     * 检查 步行最后一点 和 bus的起点 是否一致
     */
    private fun checkWalkToBusline(busStep: BusStepV2) {
        val walkLastPoint = getLastWalkPoint(busStep)
        val buslineFirstPoint = getFirstBuslinePoint(busStep)
        if (walkLastPoint != buslineFirstPoint) {
            addWalkPolyLineByLatLonPoints(walkLastPoint, buslineFirstPoint)
        }
    }

    private fun getFirstWalkPoint(busStep1: BusStepV2): LatLonPoint {
        return busStep1.walk.steps[0].polyline[0]
    }

    private fun addWalkPolyLineByLatLonPoints(
        pointFrom: LatLonPoint?,
        pointTo: LatLonPoint?
    ) {
        val latLngFrom = convertToLatLng(pointFrom)
        val latLngTo = convertToLatLng(pointTo)
        addWalkPolyline(latLngFrom, latLngTo)
    }

    private fun addWalkPolyline(latLngFrom: LatLng?, latLngTo: LatLng?) {
        if(null == latLngFrom || null == latLngTo) return
        addPolyLine(
            PolylineOptions().add(latLngFrom, latLngTo)
                .width(routeWidth)
                .zIndex(if(isSelectedBusLine) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
                .setCustomTexture(if (isSelectedBusLine) walkLineSelectedTexture else walkLineUnSelectedTexture)
                .color(
                    polylineColor.copy(if (isSelectedBusLine) ROUTE_SELECTED_TRANSPARENCY else ROUTE_UNSELECTED_TRANSPARENCY)
                        .toArgb()
                ).setDottedLine(true).setDottedLineType(1)
        )
    }

    private fun addWalkPolyline(listWalkPolyline: List<LatLng>) {
        addPolyLine(
            PolylineOptions().addAll(listWalkPolyline)
                .setCustomTexture(if (isSelectedBusLine) walkLineSelectedTexture else walkLineUnSelectedTexture)
                .width(routeWidth)
                .zIndex(if(isSelectedBusLine) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
                .color(
                    polylineColor.copy(if (isSelectedBusLine) ROUTE_SELECTED_TRANSPARENCY else ROUTE_UNSELECTED_TRANSPARENCY)
                        .toArgb()
                ).setDottedLine(true).setDottedLineType(1)
        )
    }

    /*private fun addRailwayPolyline(listPolyline: List<LatLng>) {
        addPolyLine(
            PolylineOptions().addAll(listPolyline)
                .zIndex(if(isSelectedBusLine) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
                .setCustomTexture(null).width(routeWidth)
        )
    }*/

    override fun setPolylineSelected(isSelected: Boolean) {
        isSelectedBusLine = isSelected
        synchronized(aMap) {
            addToMap(false)
        }
    }

    private fun getWalkSnippet(walkSteps: List<WalkStep>): String {
        var disNum = 0f
        for (step in walkSteps) {
            disNum += step.distance
        }
        return "\u6B65\u884C" + disNum + "\u7C73"
    }

    private fun drawLineArrow(latLngFrom: LatLng?, latLngTo: LatLng?) {
        if(null == latLngFrom || null == latLngTo) return
        // 绘制直线
        addPolyLine(
            PolylineOptions().add(latLngFrom, latLngTo)
                .width(routeWidth)
                .zIndex(if(isSelectedBusLine) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
                .setCustomTexture(if(isSelectedBusLine) busLineSelectedTexture else busLineUnSelectedTexture)
        )
    }

    private fun getBusSnippet(routeBusLineItem: RouteBusLineItem): String {
        return ("("
                + routeBusLineItem.departureBusStation.busStationName
                + "-->"
                + routeBusLineItem.arrivalBusStation.busStationName
                + ") \u7ECF\u8FC7" + (routeBusLineItem.passStationNum + 1)
                + "\u7AD9")
    }

    private fun getLastWalkPoint(busStep: BusStepV2): LatLonPoint {
        val walkSteps = busStep.walk.steps
        val walkStep = walkSteps[walkSteps.size - 1]
        val lonPoints = walkStep.polyline
        return lonPoints[lonPoints.size - 1]
    }

    private fun getExitPoint(busStep: BusStepV2): LatLonPoint? {
        val doorway = busStep.exit ?: return null
        return doorway.latLonPoint
    }

    private fun getLastBuslinePoint(busStep: BusStepV2): LatLonPoint? {
        val lonPoints = busStep.busLines.getOrNull(0)?.polyline
        return lonPoints?.getOrNull(lonPoints.size - 1)
    }

    private fun getEntrancePoint(busStep: BusStepV2): LatLonPoint? {
        val doorway = busStep.entrance ?: return null
        return doorway.latLonPoint
    }

    private fun getFirstBuslinePoint(busStep: BusStepV2): LatLonPoint? {
        return busStep.busLines.getOrNull(0)?.polyline?.getOrNull(0)
    }
}