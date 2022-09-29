// Copyright 2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.melody.map.compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Polygon
import com.amap.api.maps.model.PolygonOptions
import com.melody.map.compose.MapApplier
import com.melody.map.compose.MapNode

internal class PolygonNode(
    val polygon: Polygon
) : MapNode {
    override fun onRemoved() {
        polygon.remove()
    }
}

/**
 * A composable for a polygon on the map.
 *
 * @param points the points comprising the vertices of the polygon
 * @param fillColor the fill color of the polygon
 * @param strokeColor the stroke color of the polygon
 * @param strokeWidth specifies the polygon's stroke width, in display pixels
 * @param visible the visibility of the polygon
 * @param zIndex the z-index of the polygon
 * @param onClick a lambda invoked when the polygon is clicked
 */
@Composable
fun Polygon(
    points: List<LatLng>,
    fillColor: Color = Color.Black,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 10f,
    visible: Boolean = true,
    zIndex: Float = 0f
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolygonNode, MapApplier>(
        factory = {
            val polygon = mapApplier?.map?.addPolygon(PolygonOptions().apply  {
                addAll(points)
                fillColor(fillColor.toArgb())
                strokeColor(strokeColor.toArgb())
                strokeWidth(strokeWidth)
                visible(visible)
                zIndex(zIndex)
            }) ?: error("Error adding polygon")
            PolygonNode(polygon)
        },
        update = {
            set(points) { this.polygon.points = it }
            set(fillColor) { this.polygon.fillColor = it.toArgb() }
            set(strokeColor) { this.polygon.strokeColor = it.toArgb() }
            set(strokeWidth) { this.polygon.strokeWidth = it }
            set(visible) { this.polygon.isVisible = it }
            set(zIndex) { this.polygon.zIndex = it }
        }
    )
}
