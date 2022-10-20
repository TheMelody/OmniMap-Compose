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

package com.melody.map.myapplication.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.amap.api.maps.model.BitmapDescriptor
import com.melody.map.gd_compose.model.GDMapComposable
import com.melody.map.gd_compose.overlay.Marker
import com.melody.map.gd_compose.overlay.MarkerState
import com.melody.map.gd_compose.overlay.rememberMarkerState
import com.melody.map.myapplication.model.BaseRouteDataState

/**
 * StartAndTargetPosMarker
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/20 14:56
 */
@Composable
@GDMapComposable
internal fun StartAndTargetPosMarker(data: BaseRouteDataState) {
    NoFocusMarker(
        zIndex = 1F,
        anchor = Offset(0.5F,1F),
        state = rememberMarkerState(position = data.startPos),
        icon = data.startMarkerIcon
    )
    NoFocusMarker(
        zIndex = 1F,
        anchor = Offset(0.5F,1F),
        state = rememberMarkerState(position = data.targetPos),
        icon = data.endMarkerIcon
    )
}

@Composable
@GDMapComposable
internal fun StartAndTargetGuidePosMarker(data: BaseRouteDataState) {
    NoFocusMarker(
        zIndex = 0.5F,
        anchor = Offset(0.5F, 0.5F),
        state = rememberMarkerState(position = data.startPos),
        icon = data.startGuideIcon
    )
    NoFocusMarker(
        zIndex = 0.5F,
        anchor = Offset(0.5F, 0.5F),
        state = rememberMarkerState(position = data.targetPos),
        icon = data.endGuideIcon
    )
    StartAndTargetPosMarker(data)
}

@Composable
@GDMapComposable
private fun NoFocusMarker(
    state: MarkerState = rememberMarkerState(),
    icon: BitmapDescriptor?,
    anchor: Offset,
    zIndex: Float
) {
    Marker(
        icon = icon,
        state = state,
        zIndex = zIndex,
        anchor = anchor,
        onClick = { true }
    )
}