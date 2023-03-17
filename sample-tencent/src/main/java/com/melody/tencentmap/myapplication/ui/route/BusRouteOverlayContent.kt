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

package com.melody.tencentmap.myapplication.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.melody.map.tencent_compose.model.TXMapComposable
import com.melody.map.tencent_compose.overlay.Marker
import com.melody.map.tencent_compose.overlay.Polyline
import com.melody.map.tencent_compose.overlay.PolylineCustomTexture
import com.melody.map.tencent_compose.overlay.rememberMarkerState
import com.melody.tencentmap.myapplication.R
import com.melody.tencentmap.myapplication.model.BusRouteDataState
import com.tencent.lbssearch.`object`.result.TransitResultObject
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions

/**
 * BusRouteOverlayContent
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/20 15:44
 */
@TXMapComposable
@Composable
internal fun BusRouteOverlayContent(dataState: BusRouteDataState) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    dataState.routeList.forEachIndexed { index, route ->
        route.steps.forEach { segment ->
            if (segment is TransitResultObject.Transit) {
                PolylineCustomTexture(
                    points = segment.lines[0].polyline,
                    width = dataState.polylineWidth,
                    borderWidth = dataState.polylineBorderWidth,
                    animation = null,
                    isLineCap = true,
                    zIndex = if(selectedIndex == index) 2F else 1F,
                    polylineColor = if(selectedIndex == index) Color(0xFF58C180) else Color(0xFF83DAA4),
                    polylineBorderColor = if(selectedIndex == index) Color(0xFF387C54) else Color(0xFF76BB93),
                    customTexture = PolylineCustomTexture.create(
                        arrowSpacing = 80, arrowTexture = BitmapDescriptorFactory.fromResource(
                            R.drawable.color_arrow_texture
                        )
                    ),
                    onClick = {
                        selectedIndex = index
                    }
                )
            } else if (segment is TransitResultObject.Walking) {
                Polyline(
                    isLineCap = true,
                    polylineColor = if(selectedIndex == index) Color(0xFFFF0404) else Color(0xFFEE8D8D),
                    polylineBorderColor = if(selectedIndex == index) Color(0xFFFF0404) else Color(0xFFEE8D8D),
                    borderWidth = 1F,
                    points = segment.polyline,
                    lineType = PolylineOptions.LineType.LINE_TYPE_IMAGEINARYLINE,
                    pattern = listOf(35, 20),
                    zIndex = if(selectedIndex == index) 3F else 2F,
                    onClick = {
                        selectedIndex = index
                    }
                )
            }
        }
    }
    Marker(
        anchor = Offset(0.5f, 0.5f),
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_start_guide_icon),
        state = rememberMarkerState(position = dataState.startPoint)
    )
    Marker(
        anchor = Offset(0.5f, 0.5f),
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_end_guide_icon),
        state = rememberMarkerState(position = dataState.endPoint)
    )
    Marker(
        anchor = Offset(0.5F, 1F),
        zIndex = 1F,
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.bus_start_icon),
        state = rememberMarkerState(position = dataState.startPoint)
    )
    Marker(
        anchor = Offset(0.5F, 1F),
        zIndex = 1F,
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.bus_end_icon),
        state = rememberMarkerState(position = dataState.endPoint)
    )
}