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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.melody.map.gd_compose.model.GDMapComposable
import com.melody.map.gd_compose.overlay.BusRouteOverlay
import com.melody.map.myapplication.model.BusRouteDataState

/**
 * BusRouteOverlayContent
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/18 11:43
 */
@Composable
@GDMapComposable
internal fun BusRouteOverlayContent(data: BusRouteDataState) {
    var polylineSelectedIndex by rememberSaveable { mutableStateOf(0) }
    StartAndTargetPosMarker(data)
    data.busPathV2List.forEachIndexed { index, busPathV2 ->
        BusRouteOverlay(
            isSelected = polylineSelectedIndex == index,
            startPoint = data.startPos,
            endPoint = data.targetPos,
            routeWidth = data.routeWidth,
            busLineSelectedTexture = data.busLineSelectedTexture,
            busLineUnSelectedTexture = data.busLineUnSelectedTexture,
            walkLineSelectedTexture = null,
            walkLineUnSelectedTexture = null,
            busPath = busPathV2,
            onPolylineClick = {
                polylineSelectedIndex = index
            }
        )
    }
}