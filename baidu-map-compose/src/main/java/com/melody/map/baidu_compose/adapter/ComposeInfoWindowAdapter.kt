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

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import com.baidu.mapapi.map.InfoWindow
import com.melody.map.baidu_compose.R
import com.melody.map.baidu_compose.extensions.getInfoWindowYOffset
import com.melody.map.baidu_compose.extensions.getSnippetExt
import com.melody.map.baidu_compose.extensions.getTitleExt
import com.melody.map.baidu_compose.overlay.ClusterOverlayNode
import com.melody.map.baidu_compose.overlay.MarkerNode
import com.melody.map.baidu_compose.utils.clustering.ClusterItem

internal class ComposeInfoWindowAdapter(private val mapContext: Context) {

    fun getInfoContents(markerNode: MarkerNode): InfoWindow {
        val infoContent = markerNode.infoContent
        val view = if(infoContent == null) { // infoContent为空，使用默认对外提供的内容样式
            val title = markerNode.marker.getTitleExt()
            val snippet = markerNode.marker.getSnippetExt()
            getDefaultInfoContent(title, snippet)
        } else {
            applyAndRemove(false, markerNode.compositionContext) {
                infoContent(markerNode.marker)
            }
        }
        return InfoWindow(
            view,
            markerNode.marker.position,
            markerNode.marker.getInfoWindowYOffset()
        )
    }

    fun getInfoWindow(markerNode: MarkerNode): InfoWindow {
        val infoWindow = markerNode.infoWindow!!
        val view = applyAndRemove(true, markerNode.compositionContext) {
            infoWindow(markerNode.marker)
        }
        return InfoWindow(
            view,
            markerNode.marker.position,
            markerNode.marker.getInfoWindowYOffset()
        )
    }

    fun getInfoWindow(clusterItem: ClusterItem, clusterItemNode: ClusterOverlayNode): InfoWindow {
        val view = applyAndRemove(true, clusterItemNode.compositionContext) {
            clusterItemNode.onClusterItemInfoWindow?.invoke(clusterItem)
        }
        return InfoWindow(
            view,
            clusterItem.getPosition(),
            clusterItem.getInfoWindowYOffset()
        )
    }

    private fun applyAndRemove(
        fromInfoWindow: Boolean,
        parentContext: CompositionContext,
        content: @Composable () -> Unit
    ):  ViewGroup{
        return FrameLayout(mapContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (fromInfoWindow) {
                // 去除地图默认气泡背景, 如果是InfoContent，则只定制内容，不修改窗口背景和样式
                setBackgroundColor(Color.TRANSPARENT)
            }else {
                setBackgroundResource(R.drawable.infowindow_bg)
            }
            addView(
                ComposeView(mapContext).apply {
                    setParentCompositionContext(parentContext)
                    setContent {
                        content.invoke()
                    }
                }
            )
        }
    }

    /**
     * 针对普通的Marker
     */
    private fun getDefaultInfoContent(title: String?,snippet: String?): LinearLayout {
        return LinearLayout(mapContext).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.drawable.infowindow_bg)
            orientation = LinearLayout.VERTICAL
            if (title?.isNotBlank() == true) {
                addView(
                    TextView(mapContext).apply {
                        gravity = Gravity.CENTER
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                        setTextColor(Color.BLACK)
                        text = title
                    }
                )
            }
            addView(
                Space(mapContext).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.height = dp2px(2F)
                }
            )
            if (snippet?.isNotBlank() == true) {
                addView(
                    TextView(mapContext).apply {
                        gravity = Gravity.CENTER
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                        setTextColor(Color.BLACK)
                        text = snippet
                    }
                )
            }
        }
    }

    private fun dp2px(dpValue: Float): Int {
        return (0.5f + dpValue * Resources.getSystem().displayMetrics.density).toInt()
    }
}