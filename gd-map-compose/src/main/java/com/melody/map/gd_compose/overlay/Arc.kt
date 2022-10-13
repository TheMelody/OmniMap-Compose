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