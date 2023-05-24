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

package com.melody.bdmap.myapplication.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.melody.bdmap.myapplication.model.BusRouteDataState
import com.melody.map.baidu_compose.overlay.BusRouteOverlay
import com.melody.map.baidu_compose.overlay.Marker
import com.melody.map.baidu_compose.overlay.rememberMarkerState

/**
 * BusRouteOverlayContent
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/05/23 14:05
 */
@Composable
internal fun BusRouteOverlayContent(dataState: BusRouteDataState) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    dataState.listOverlayOptions?.forEachIndexed { index, overlayOptions ->
        BusRouteOverlay(
            latLngBounds = dataState.latLngBounds,
            overlayOptions = overlayOptions?: emptyList(),
            selectedTexture = dataState.selectTexture,
            unSelectedTexture = dataState.unSelectTexture,
            isSelected = selectedIndex == index,
            onPolylineClick = {
                selectedIndex = index
                true
            }
        )
    }
    Marker(
        anchor = Offset(0.5f,0.5f),
        zIndex = 3,
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_start_guide_icon),
        state = rememberMarkerState(position = dataState.startPoint)
    )
    Marker(
        anchor = Offset(0.5f,0.5f),
        zIndex = 3,
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_end_guide_icon),
        state = rememberMarkerState(position = dataState.endPoint)
    )
    Marker(
        anchor = Offset(0.5F,1F),
        zIndex = 4,
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.bus_start_icon),
        state = rememberMarkerState(position = dataState.startPoint)
    )
    Marker(
        anchor = Offset(0.5F,1F),
        zIndex = 4,
        icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.bus_end_icon),
        state = rememberMarkerState(position = dataState.endPoint)
    )
}