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

package com.melody.map.baidu_compose.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import com.baidu.mapapi.map.InfoWindow
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker
import com.melody.map.baidu_compose.R
import com.melody.map.baidu_compose.overlay.MarkerNode

/**
 * 百度地图【不支持多个InfoWindow同时显示】:
 * https://lbsyun.baidu.com/index.php?title=FAQ/Android/map
 */
internal class ComposeInfoWindowAdapter(private val mapView: MapView) {
    companion object {
        internal const val KEY_INFO_WINDOW_Y_OFFSET = "InfoWindowYOffset"
        private const val DEFAULT_INFO_WINDOW_Y_OFFSET = -52
    }

    private val infoWindowView: ComposeView
        get() = ComposeView(mapView.context).apply {
            mapView.addView(
                this,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

    fun getInfoContents(marker: Marker, markerNode: MarkerNode): InfoWindow {
        val infoContent = markerNode.infoContent!!
        val view = infoWindowView.applyAndRemove(false, markerNode.compositionContext) {
            infoContent(marker)
        }
        return InfoWindow(
            view,
            marker.position,
            marker.extraInfo.getInt(KEY_INFO_WINDOW_Y_OFFSET, DEFAULT_INFO_WINDOW_Y_OFFSET)
        )
    }

    fun getInfoWindow(marker: Marker, markerNode: MarkerNode): InfoWindow {
        val infoWindow = markerNode.infoWindow!!
        val view = infoWindowView.applyAndRemove(true, markerNode.compositionContext) {
            infoWindow(marker)
        }
        return InfoWindow(
            view,
            marker.position,
            marker.extraInfo.getInt(KEY_INFO_WINDOW_Y_OFFSET, DEFAULT_INFO_WINDOW_Y_OFFSET)
        )
    }

    private fun ComposeView.applyAndRemove(
        fromInfoWindow: Boolean,
        parentContext: CompositionContext,
        content: @Composable () -> Unit
    ): ComposeView {
        val result = this.apply {
            setParentCompositionContext(parentContext)
            setContent {
                setBackgroundColor(Color.TRANSPARENT)
                if (!fromInfoWindow) {
                    setBackgroundResource(R.drawable.infowindow_bg)
                }
                content.invoke()
            }
        }
        (this.parent as? MapView)?.removeView(this)
        return result
    }
}