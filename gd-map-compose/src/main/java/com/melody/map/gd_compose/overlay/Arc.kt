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

package com.melody.map.gd_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.model.Arc
import com.amap.api.maps.model.ArcOptions
import com.amap.api.maps.model.LatLng
import com.melody.map.gd_compose.MapApplier
import com.melody.map.gd_compose.MapNode
import com.melody.map.gd_compose.model.GDMapComposable

internal class ArcNode(
    val arc: Arc
) : MapNode {
    override fun onRemoved() {
        arc.remove()
    }
}

/**
 * A composable for a Arc on the map.
 * @param strokeColor the stroke color of the Arc
 * @param strokeWidth the width of the Arc's outline in screen pixels
 * @param visible the visibility of the Arc
 * @param zIndex the z-index of the Arc
 */
@Composable
@GDMapComposable
fun Arc(
    startPoint: LatLng,
    passedPoint: LatLng,
    endPoint: LatLng,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 10f,
    visible: Boolean = true,
    zIndex: Float = 0f,
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<ArcNode, MapApplier>(
        factory = {
            val arc = mapApplier?.map?.addArc(
                ArcOptions().apply  {
                    point(startPoint,passedPoint,endPoint)
                    strokeColor(strokeColor.toArgb())
                    strokeWidth(strokeWidth)
                    visible(visible)
                    zIndex(zIndex)
                }
            ) ?: error("Error adding arc")
            ArcNode(arc)
        },
        update = {
            set(strokeColor) { this.arc.strokeColor = it.toArgb() }
            set(strokeWidth) { this.arc.strokeWidth = it }
            set(visible) { this.arc.isVisible = it }
            set(zIndex) { this.arc.zIndex = it }
        }
    )
}