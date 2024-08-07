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
import com.melody.map.tencent_compose.overlay.PolylineCustomTexture
import com.melody.map.tencent_compose.overlay.rememberMarkerState
import com.melody.tencentmap.myapplication.R
import com.melody.tencentmap.myapplication.model.DrivingRouteDataState
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory

/**
 * DrivingRouteOverlayContent
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/20 15:25
 */
@TXMapComposable
@Composable
internal fun DrivingRouteOverlayContent(dataState: DrivingRouteDataState) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }

    dataState.points.forEachIndexed { index, pointList ->
        PolylineCustomTexture(
            isLineCap = true,
            points = pointList,
            width = dataState.polylineWidth,
            borderWidth = dataState.polylineBorderWidth,
            animation = dataState.polylineAnim,
            zIndex = if(selectedIndex == index) 2F else 1F,
            polylineColor = if(selectedIndex == index) Color(0xFF58C180) else Color(0xFF83DAA4),
            polylineBorderColor = if(selectedIndex == index) Color(0xFF387C54) else Color(0xFF76BB93),
            customTexture = PolylineCustomTexture.create(arrowSpacing = 80, arrowTexture = BitmapDescriptorFactory.fromResource(
                R.drawable.color_arrow_texture
            )),
            onClick = {
                selectedIndex = index
            }
        )
    }
    Marker(
        visible = dataState.isAnimationStart,
        anchor = Offset(0.5f,0.5f),
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_start_guide_icon),
        state = rememberMarkerState(position = dataState.startPoint)
    )
    Marker(
        visible = dataState.isAnimationEnd,
        anchor = Offset(0.5f,0.5f),
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_end_guide_icon),
        state = rememberMarkerState(position = dataState.endPoint)
    )
    Marker(
        visible = dataState.isAnimationStart,
        anchor = Offset(0.5F,1F),
        zIndex = 1F,
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.bus_start_icon),
        state = rememberMarkerState(position = dataState.startPoint)
    )
    Marker(
        visible = dataState.isAnimationEnd,
        anchor = Offset(0.5F,1F),
        zIndex = 1F,
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.bus_end_icon),
        state = rememberMarkerState(position = dataState.endPoint)
    )
}