package com.melody.map.compose.overlay

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
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.melody.map.compose.MapApplier
import com.melody.map.compose.MapNode

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
 * A state object that can be hoisted to control and observe the marker state.
 *
 * @param position the initial marker position
 */
class MarkerState(
    position: LatLng = LatLng(0.0, 0.0)
) {
    /**
     * Current position of the marker.
     */
    var position: LatLng by mutableStateOf(position)

    /**
     * Current [DragState] of the marker.
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
     * Shows the info window for the underlying marker
     */
    fun showInfoWindow() {
        marker?.showInfoWindow()
    }

    /**
     * Hides the info window for the underlying marker
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
fun rememberMarkerState(
    key: String? = null,
    position: LatLng = LatLng(0.0, 0.0)
): MarkerState = rememberSaveable(key = key, saver = MarkerState.Saver) {
    MarkerState(position)
}

/**
 * A composable for a marker on the map.
 *
 * @param state the [MarkerState] to be used to control or observe the marker
 * state such as its position and info window
 * @param alpha the alpha (opacity) of the marker
 * @param anchor the anchor for the marker image
 * @param draggable sets the draggability for the marker
 * @param flat sets if the marker should be flat against the map
 * @param icon sets the icon for the marker
 * @param rotation the rotation of the marker in degrees clockwise about the marker's anchor point
 * @param snippet the snippet for the marker
 * @param tag optional tag to associate with the marker
 * @param title the title for the marker
 * @param visible the visibility of the marker
 * @param zIndex the z-index of the marker
 * @param onClick a lambda invoked when the marker is clicked
 * @param onInfoWindowClick a lambda invoked when the marker's info window is clicked
 */
@Composable
fun Marker(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    snippet: String? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    title: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        flat = flat,
        icon = icon,
        rotation = rotation,
        snippet = snippet,
        tag = tag,
        title = title,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        onInfoWindowClick = onInfoWindowClick,
    )
}

/**
 * A composable for a marker on the map wherein its entire info window can be
 * customized. If this customization is not required, use
 * [com.amap.api.maps.model.Marker].
 *
 * @param state the [MarkerState] to be used to control or observe the marker
 * state such as its position and info window
 * @param alpha the alpha (opacity) of the marker
 * @param anchor the anchor for the marker image
 * @param draggable sets the draggability for the marker
 * @param flat sets if the marker should be flat against the map
 * @param icon sets the icon for the marker
 * @param rotation the rotation of the marker in degrees clockwise about the marker's anchor point
 * @param snippet the snippet for the marker
 * @param tag optional tag to associate with the marker
 * @param title the title for the marker
 * @param visible the visibility of the marker
 * @param zIndex the z-index of the marker
 * @param onClick a lambda invoked when the marker is clicked
 * @param onInfoWindowClick a lambda invoked when the marker's info window is clicked
 * @param content optional composable lambda expression for customizing the
 * info window's content
 */
@Composable
fun MarkerInfoWindow(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    snippet: String? = null,
    title: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
    content: (@Composable (Marker) -> Unit)? = null
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        flat = flat,
        icon = icon,
        snippet = snippet,
        rotation = rotation,
        title = title,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        onInfoWindowClick = onInfoWindowClick,
        infoWindow = content,
    )
}

/**
 * A composable for a marker on the map wherein its info window contents can be
 * customized. If this customization is not required, use
 * [com.amap.api.maps.model.Marker].
 *
 * @param state the [MarkerState] to be used to control or observe the marker
 * state such as its position and info window
 * @param alpha the alpha (opacity) of the marker
 * @param anchor the anchor for the marker image
 * @param draggable sets the draggability for the marker
 * @param flat sets if the marker should be flat against the map
 * @param icon sets the icon for the marker
 * @param rotation the rotation of the marker in degrees clockwise about the marker's anchor point
 * @param snippet the snippet for the marker
 * @param tag optional tag to associate with the marker
 * @param title the title for the marker
 * @param visible the visibility of the marker
 * @param zIndex the z-index of the marker
 * @param onClick a lambda invoked when the marker is clicked
 * @param onInfoWindowClick a lambda invoked when the marker's info window is clicked
 * @param content optional composable lambda expression for customizing the
 * info window's content
 */
@Composable
fun MarkerInfoWindowContent(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    snippet: String? = null,
    title: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
    content: (@Composable (Marker) -> Unit)? = null
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        flat = flat,
        icon = icon,
        snippet = snippet,
        rotation = rotation,
        title = title,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        onInfoWindowClick = onInfoWindowClick,
        infoContent = content,
    )
}

/**
 * Internal implementation for a marker on a GD map.
 *
 * @param state the [MarkerState] to be used to control or observe the marker
 * state such as its position and info window
 * @param alpha the alpha (opacity) of the marker
 * @param anchor the anchor for the marker image
 * @param draggable sets the draggability for the marker
 * @param flat sets if the marker should be flat against the map
 * @param icon sets the icon for the marker
 * @param rotation the rotation of the marker in degrees clockwise about the marker's anchor point
 * @param snippet the snippet for the marker
 * @param tag optional tag to associate with the marker
 * @param title the title for the marker
 * @param visible the visibility of the marker
 * @param zIndex the z-index of the marker
 * @param onClick a lambda invoked when the marker is clicked
 * @param onInfoWindowClick a lambda invoked when the marker's info window is clicked
 * @param infoWindow optional composable lambda expression for customizing
 * the entire info window. If this value is non-null, the value in infoContent]
 * will be ignored.
 * @param infoContent optional composable lambda expression for customizing
 * the info window's content. If this value is non-null, [infoWindow] must be null.
 */
@Composable
private fun MarkerImpl(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    rotation: Float = 0.0f,
    tag: Any? = null,
    snippet: String? = null,
    title: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
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
                    anchor(anchor.x, anchor.y)
                    draggable(draggable)
                    icon(icon)
                    isFlat = flat
                    rotateAngle(rotation)
                    position(state.position)
                    snippet(snippet)
                    title(title)
                    visible(visible)
                    zIndex(zIndex)
                }
            ) ?: error("Error adding marker")
            marker.`object` = tag
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
            set(anchor) { this.marker.setAnchor(it.x, it.y) }
            set(draggable) { this.marker.isDraggable = it }
            set(flat) { this.marker.isFlat = it }
            set(icon) { this.marker.setIcon(it) }
            set(rotation) { this.marker.rotateAngle = rotation }
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
        }
    )
}
