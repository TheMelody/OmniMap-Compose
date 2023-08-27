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

package com.melody.map.petal_compose.overlay

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
import com.huawei.hms.maps.model.BitmapDescriptor
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.maps.model.animation.Animation
import com.melody.map.petal_compose.MapApplier
import com.melody.map.petal_compose.MapNode
import com.melody.map.petal_compose.model.HWMapComposable

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
        marker.remove()
    }

    override fun onCleared() {
        markerState.marker = null
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
@HWMapComposable
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
 * @param flat Marker覆盖物是否平贴在地图上
 * @param icon Marker覆盖物的图标
 * @param rotation Marker覆盖物基于锚点旋转的角度
 * @param tag Marker覆盖物的附加信息对象
 * @param title Marker覆盖物的标题
 * @param snippet Marker 覆盖物的文字片段
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param isSetTop 设置为true: 则当前marker在最上面
 * @param animation 动画包含，旋转，缩放，消失，平移以及它们的组合动画
 * @param animationListener 动画监听，包含动画开始和结束时的回调
 * @param onClick 标注点击事件回调
 * @param onInfoWindowClick InfoWindow的点击事件回调
 */
@Composable
@HWMapComposable
fun Marker(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    isClickable: Boolean = true,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    title: String? = null,
    snippet: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    isSetTop: Boolean = false,
    animation: Animation? = null,
    animationListener: Animation.AnimationListener? = null,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        isClickable = isClickable,
        flat = flat,
        icon = icon,
        rotation = rotation,
        snippet = snippet,
        tag = tag,
        title = title,
        visible = visible,
        zIndex = zIndex,
        isSetTop = isSetTop,
        onClick = onClick,
        animation = animation,
        animationListener = animationListener,
        onInfoWindowClick = onInfoWindowClick,
    )
}

/**
 * 覆盖物[Marker]，此组合项可定制整个InfoWindow信息窗口，如果不需要此自定义，请使用 [com.melody.map.gd_compose.overlay.Marker].
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param flat Marker覆盖物是否平贴在地图上
 * @param icon Marker覆盖物的图标
 * @param rotation Marker覆盖物基于锚点旋转的角度
 * @param tag Marker覆盖物的附加信息对象
 * @param title Marker覆盖物的标题
 * @param snippet Marker 覆盖物的文字片段
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param isSetTop true: 设置当前marker在最上面
 * @param animation 动画包含，旋转，缩放，消失，平移以及它们的组合动画
 * @param animationListener 动画监听，包含动画开始和结束时的回调
 * @param onClick 标注点击事件回调
 * @param onInfoWindowClick InfoWindow的点击事件回调
 * @param content 【可选】，用于自定义整个信息窗口。
 */
@Composable
@HWMapComposable
fun MarkerInfoWindow(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    isClickable: Boolean = true,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    title: String? = null,
    snippet: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    isSetTop: Boolean = false,
    animation: Animation? = null,
    animationListener: Animation.AnimationListener? = null,
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
        flat = flat,
        icon = icon,
        rotation = rotation,
        tag = tag,
        title = title,
        snippet = snippet,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        isSetTop = isSetTop,
        animation = animation,
        animationListener = animationListener,
        onInfoWindowClick = onInfoWindowClick,
        infoWindow = content,
    )
}

/**
 * 覆盖物[Marker]，此组合项可定制InfoWindow信息窗口内容，如果不需要此自定义，请使用 [com.melody.map.gd_compose.overlay.Marker].
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param flat Marker覆盖物是否平贴在地图上
 * @param icon Marker覆盖物的图标
 * @param rotation Marker覆盖物基于锚点旋转的角度
 * @param tag Marker覆盖物的附加信息对象
 * @param title Marker覆盖物的标题
 * @param snippet Marker 覆盖物的文字片段
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param isSetTop true: 设置当前marker在最上面
 * @param animation 动画包含，旋转，缩放，消失，平移以及它们的组合动画
 * @param animationListener 动画监听，包含动画开始和结束时的回调
 * @param onClick 标注点击事件回调
 * @param onInfoWindowClick InfoWindow的点击事件回调
 * @param content 【可选】，用于自定义信息窗口的内容。
 */
@Composable
@HWMapComposable
fun MarkerInfoWindowContent(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    isClickable: Boolean = true,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    title: String? = null,
    snippet: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    isSetTop: Boolean = false,
    animation: Animation? = null,
    animationListener: Animation.AnimationListener? = null,
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
        flat = flat,
        icon = icon,
        rotation = rotation,
        tag = tag,
        title = title,
        snippet = snippet,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        isSetTop = isSetTop,
        animation = animation,
        animationListener = animationListener,
        onInfoWindowClick = onInfoWindowClick,
        infoContent = content,
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
 * @param flat Marker覆盖物是否平贴在地图上
 * @param icon Marker覆盖物的图标
 * @param rotation Marker覆盖物基于锚点旋转的角度
 * @param tag Marker覆盖物的附加信息对象
 * @param title Marker覆盖物的标题
 * @param snippet Marker 覆盖物的文字片段
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param isSetTop true: 设置当前marker在最上面
 * @param animation 动画包含，旋转，缩放，消失，平移以及它们的组合动画
 * @param animationListener 动画监听，包含动画开始和结束时的回调
 * @param onClick 标注点击事件回调
 * @param onInfoWindowClick InfoWindow的点击事件回调
 * @param infoWindow 【可选】，用于自定义整个信息窗口。如果此值为非空，则[infoContent]中的值将被忽略。
 * @param infoContent 【可选】，用于自定义信息窗口的内容。如果此值为非 null，则 [infoWindow] 必须为 null。
 */
@Composable
@HWMapComposable
private fun MarkerImpl(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    isClickable: Boolean = true,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    title: String? = null,
    snippet: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    isSetTop: Boolean = false,
    animation: Animation? = null,
    animationListener: Animation.AnimationListener? = null,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
    infoWindow: (@Composable (Marker) -> Unit)? = null,
    infoContent: (@Composable (Marker) -> Unit)? = null,
) {
    val mapApplier = currentComposer.applier as? MapApplier
    val compositionContext = rememberCompositionContext()
    ComposeNode<MarkerNode, MapApplier>(
        factory = {
            val marker = mapApplier?.map?.addMarker(
                MarkerOptions().apply {
                    alpha(alpha)
                    anchorMarker(anchor.x, anchor.y)
                    draggable(draggable)
                    icon(icon)
                    flat(flat)
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
            set(anchor) { this.marker.setMarkerAnchor(it.x, it.y) }
            set(draggable) { this.marker.isDraggable = it }
            set(flat) { this.marker.isFlat = it }
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
            set(zIndex) { this.marker.zIndex = it }
            set(animation) {
                if(null != animation) {
                    marker.setAnimation(animation)
                    marker.startAnimation()
                }
            }
        }
    )
}