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

package com.melody.map.tencent_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.melody.map.tencent_compose.MapApplier
import com.melody.map.tencent_compose.MapNode
import com.melody.map.tencent_compose.model.TXMapComposable
import com.tencent.tencentmap.mapsdk.maps.model.BaseAnimation
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.Marker
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions

internal class MarkerNode(
    val compositionContext: CompositionContext,
    val marker: Marker,
    val markerState: MarkerState,
    var onMarkerClick: (Marker) -> Boolean,
    var onInfoWindowClick: (Marker) -> Unit,
    var infoWindow: (@Composable (Marker) -> Unit)?,
    var infoContent: (@Composable (Marker) -> Unit)?,
) : MapNode {
    override fun onAttached() {
        markerState.marker = marker
    }
    override fun onRemoved() {
        markerState.marker = null
        marker.setAnimation(null)
        marker.remove()
    }

    override fun onCleared() {
        markerState.marker = null
        marker.setAnimation(null)
        marker.remove()
    }
}

@Immutable
enum class DragState {
    START, DRAG, END
}

/**
 * 控制和观察[Marker]状态的状态对象。
 *
 * @param position Marker覆盖物的位置坐标
 */
class MarkerState(
    position: LatLng = LatLng(0.0, 0.0)
) {
    /**
     * 当前Marker覆盖物的位置
     */
    var position: LatLng by mutableStateOf(position)

    /**
     * 当前Marker拖拽的状态
     */
    var dragState: DragState by mutableStateOf(DragState.END)
        internal set

    // The marker associated with this MarkerState.
    internal var marker: Marker? = null
        set(value) {
            if (field == null && value == null) return
            if (field != null && value != null) {
                error("MarkerState may only be associated with one Marker at a time.")
            }
            field = value
        }

    /**
     * 显示 Marker 覆盖物的信息窗口
     */
    fun showInfoWindow() {
        marker?.showInfoWindow()
    }

    /**
     * 隐藏Marker覆盖物的信息窗口。如果Marker本身是不可以见的，此方法将不起任何作用
     */
    fun hideInfoWindow() {
        marker?.hideInfoWindow()
    }

    companion object {
        /**
         * The default saver implementation for [MarkerState]
         */
        val Saver: Saver<MarkerState, LatLng> = Saver(
            save = { it.position },
            restore = { MarkerState(it) }
        )
    }
}

@Composable
@TXMapComposable
fun rememberMarkerState(
    key: String? = null,
    position: LatLng = LatLng(0.0, 0.0)
): MarkerState = rememberSaveable(key = key, saver = MarkerState.Saver) {
    MarkerState(position)
}

/**
 * 默认覆盖物Marker， [Marker]是在地图上的一个点绘制图标。这个图标和屏幕朝向一致，和地图朝向无关，也不会受地图的旋转、倾斜、缩放影响
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param isFlat【初始化配置，**不支持二次更新**】Marker覆盖物是否平贴在地图上
 * @param isClockwise【初始化配置，**不支持二次更新**】Marker覆盖物，旋转角度是否沿顺时针方向
 * @param icon Marker覆盖物的图标
 * @param rotation 标注的旋转角度
 * @param tag Marker覆盖物的附加信息对象
 * @param title 标注的InfoWindow(气泡)的标题
 * @param snippet 标注的InfoWindow(气泡)的内容
 * @param visible 标注是否可见
 * @param zIndex 标注显示的层级
 * @param animation 动画包含，旋转，缩放，消失，平移以及它们的组合动画
 * @param runAnimation 【只有配置了**animation**才有效】设置为true，启动动画，设置为false取消动画，设置为null，不触发动画
 * @param onClick 标注点击事件回调
 * @param onInfoWindowClick InfoWindow的点击事件回调
 */
@Composable
@TXMapComposable
fun Marker(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    isClickable: Boolean = true,
    isFlat: Boolean = false,
    isClockwise: Boolean = true,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    title: String? = null,
    snippet: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    animation: BaseAnimation? = null,
    runAnimation: Boolean? = null,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        isClickable = isClickable,
        isFlat = isFlat,
        isClockwise = isClockwise,
        icon = icon,
        rotation = rotation,
        snippet = snippet,
        tag = tag,
        title = title,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        animation = animation,
        runAnimation = runAnimation,
        onInfoWindowClick = onInfoWindowClick,
        infoContent = null,
        infoWindow = null
    )
}

/**
 * 覆盖物[Marker]，此组合项可定制整个InfoWindow信息窗口，如果不需要此自定义，请使用 [com.melody.map.tencent_compose.overlay.Marker].
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param isFlat【初始化配置，**不支持二次更新**】Marker覆盖物是否平贴在地图上
 * @param isClockwise【初始化配置，**不支持二次更新**】Marker覆盖物，旋转角度是否沿顺时针方向
 * @param icon Marker覆盖物的图标
 * @param rotation 标注的旋转角度
 * @param tag Marker覆盖物的附加信息对象
 * @param title 标注的InfoWindow(气泡)的标题
 * @param snippet 标注的InfoWindow(气泡)的内容
 * @param visible 标注是否可见
 * @param zIndex 标注显示的层级
 * @param animation 动画包含，旋转，缩放，消失，平移以及它们的组合动画
 * @param runAnimation 【只有配置了**animation**才有效】设置为true，启动动画，设置为false取消动画，设置为null，不触发动画
 * @param onClick 标注点击事件回调
 * @param onInfoWindowClick InfoWindow的点击事件回调
 * @param content 【可选】，用于自定义整个信息窗口，【里面动态的内容，建议通过title、snippet、tag的方式获取】
 */
@Composable
@TXMapComposable
fun MarkerInfoWindow(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    isClickable: Boolean = true,
    isFlat: Boolean = false,
    isClockwise: Boolean = true,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    title: String? = null,
    snippet: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    animation: BaseAnimation? = null,
    runAnimation: Boolean? = null,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
    content: (@Composable (Marker) -> Unit)? = null
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        isClickable = isClickable,
        isFlat = isFlat,
        isClockwise = isClockwise,
        icon = icon,
        rotation = rotation,
        tag = tag,
        title = title,
        snippet = snippet,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        animation = animation,
        runAnimation = runAnimation,
        onInfoWindowClick = onInfoWindowClick,
        infoWindow = content,
        infoContent = null,
    )
}

/**
 * 覆盖物[Marker]，此组合项可定制InfoWindow信息窗口内容，如果不需要此自定义，请使用 [com.melody.map.tencent_compose.overlay.Marker].
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param isFlat【初始化配置，**不支持二次更新**】Marker覆盖物是否平贴在地图上
 * @param isClockwise【初始化配置，**不支持二次更新**】Marker覆盖物，旋转角度是否沿顺时针方向
 * @param icon Marker覆盖物的图标
 * @param rotation 标注的旋转角度
 * @param tag Marker覆盖物的附加信息对象
 * @param title 标注的InfoWindow(气泡)的标题
 * @param snippet 标注的InfoWindow(气泡)的内容
 * @param visible 标注是否可见
 * @param zIndex 标注显示的层级
 * @param animation 动画包含，旋转，缩放，消失，平移以及它们的组合动画
 * @param runAnimation 【只有配置了**animation**才有效】设置为true，启动动画，设置为false取消动画，设置为null，不触发动画
 * @param onClick 标注点击事件回调
 * @param onInfoWindowClick InfoWindow的点击事件回调
 * @param content (可选)，用于自定义信息窗口的内容，【里面动态的内容，建议通过title、snippet、tag的方式获取】
*/
@Composable
@TXMapComposable
fun MarkerInfoWindowContent(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    isClickable: Boolean = true,
    isFlat: Boolean = false,
    isClockwise: Boolean = true,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    title: String? = null,
    snippet: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    animation: BaseAnimation? = null,
    runAnimation :Boolean? = null,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
    content: (@Composable (Marker) -> Unit)? = null
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        isClickable = isClickable,
        isFlat = isFlat,
        isClockwise = isClockwise,
        icon = icon,
        rotation = rotation,
        tag = tag,
        title = title,
        snippet = snippet,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        animation = animation,
        runAnimation = runAnimation,
        onInfoWindowClick = onInfoWindowClick,
        infoContent = content,
        infoWindow = null
    )
}

/**
 * Marker覆盖物的内部实现
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param isFlat【初始化配置，**不支持二次更新**】Marker覆盖物是否平贴在地图上
 * @param isClockwise【初始化配置，**不支持二次更新**】Marker覆盖物，旋转角度是否沿顺时针方向
 * @param icon Marker覆盖物的图标
 * @param rotation Marker覆盖物基于锚点旋转的角度
 * @param tag Marker覆盖物的附加信息对象
 * @param title Marker覆盖物的标题
 * @param snippet Marker 覆盖物的文字片段
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param animation 动画包含，旋转，缩放，消失，平移以及它们的组合动画
 * @param runAnimation 【只有配置了**animation**才有效】设置为true，启动动画，设置为false取消动画，设置为null，不触发动画
 * @param onClick 标注点击事件回调
 * @param onInfoWindowClick InfoWindow的点击事件回调
 * @param infoWindow 【可选】，用于自定义整个信息窗口。如果此值为非空，则[infoContent]中的值将被忽略。
 * @param infoContent 【可选】，用于自定义信息窗口的内容。如果此值为非 null，则 [infoWindow] 必须为 null。
 */
@Composable
@TXMapComposable
private fun MarkerImpl(
    state: MarkerState,
    alpha: Float,
    anchor: Offset,
    draggable: Boolean,
    isClickable: Boolean,
    isFlat: Boolean,
    isClockwise: Boolean,
    icon: BitmapDescriptor?,
    rotation: Float,
    tag: Any?,
    title: String?,
    snippet: String?,
    visible: Boolean,
    zIndex: Float,
    animation: BaseAnimation?,
    runAnimation :Boolean?,
    onClick: (Marker) -> Boolean,
    onInfoWindowClick: (Marker) -> Unit = {},
    infoWindow: (@Composable (Marker) -> Unit)?,
    infoContent: (@Composable (Marker) -> Unit)?,
) {
    val mapApplier = currentComposer.applier as? MapApplier
    val compositionContext = rememberCompositionContext()
    ComposeNode<MarkerNode, MapApplier>(
        factory = {
            val marker = mapApplier?.map?.addMarker(
                MarkerOptions(state.position).apply {
                    alpha(alpha)
                    anchor(anchor.x, anchor.y)
                    draggable(draggable)
                    icon(icon)
                    flat(isFlat)
                    clockwise(isClockwise)
                    rotation(rotation)
                    position(state.position)
                    snippet(snippet)
                    title(title)
                    visible(visible)
                    zIndex(zIndex)
                }
            ) ?: error("Error adding marker")
            marker.tag = tag
            marker.isClickable = isClickable
            MarkerNode(
                compositionContext = compositionContext,
                marker = marker,
                markerState = state,
                onMarkerClick = onClick,
                onInfoWindowClick = onInfoWindowClick,
                infoContent = infoContent,
                infoWindow = infoWindow,
            )
        },
        update = {
            update(onClick) { this.onMarkerClick = it }
            update(onInfoWindowClick) { this.onInfoWindowClick = it }
            update(infoContent) { this.infoContent = it }
            update(infoWindow) { this.infoWindow = it }

            set(alpha) { this.marker.alpha = it }
            set(isClickable) { this.marker.isClickable = it }
            set(anchor) { this.marker.setAnchor(it.x, it.y) }
            set(draggable) { this.marker.isDraggable = it }
            // Marker#setMarkerOptions 方法已废弃
            //set(isFlat) { this.marker.setMarkerOptions(this.marker.options.flat(it)) }
            //set(clockwise) { this.marker.setMarkerOptions(this.marker.options.clockwise(it)) }
            set(icon) { this.marker.setIcon(it) }
            set(rotation) { this.marker.rotation = rotation }
            set(state.position) {
                this.marker.position = it
            }
            set(snippet) {
                this.marker.snippet = it
                if (this.marker.isInfoWindowShown) {
                    this.marker.showInfoWindow()
                }
            }
            set(title) {
                this.marker.title = it
                if (this.marker.isInfoWindowShown) {
                    this.marker.showInfoWindow()
                }
            }
            set(visible) { this.marker.isVisible = it }
            set(zIndex) { this.marker.setZIndex(it) }
            set(runAnimation) {
                if(it == true) {
                    if(!this.marker.startAnimation()) {
                        this.marker.startAnimation(animation)
                    }
                } else if(it == false) {
                    this.marker.setAnimation(null)
                }
            }
            set(animation) {
                this.marker.setAnimation(it)
            }
        }
    )
}