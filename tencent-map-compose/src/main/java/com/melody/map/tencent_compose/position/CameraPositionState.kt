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

package com.melody.map.tencent_compose.position

import androidx.annotation.UiThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.melody.map.tencent_compose.model.TXCameraPosition
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.Projection
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CameraPositionState
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/8/31 10:10
 */
@Composable
inline fun rememberCameraPositionState(
    key: String? = null,
    crossinline init: CameraPositionState.() -> Unit = {}
): CameraPositionState = rememberSaveable(key = key, saver = CameraPositionState.Saver) {
    CameraPositionState().apply(init)
}

/**
 * 不设置TXCameraPosition，则使用默认设置北京天安门为地图中心，也保证没有获取到位置的时候，能先渲染出来，
 * 如需修改默认值，请参考这样使用：
 * rememberCameraPositionState {
 *    position = TXCameraPosition(LatLng(0.0, 0.0), 11f, 0f, 0f)
 * }
 */
class CameraPositionState(position: TXCameraPosition = TXCameraPosition(LatLng(39.91, 116.40), 11f, 0f, 0f)) {

    /**
     * Whether the camera is currently moving or not. This includes any kind of movement:
     * panning, zooming, or rotation.
     */
    var isMoving: Boolean by mutableStateOf(false)
        internal set

    /**
     * Returns the current [Projection] to be used for converting between screen
     * coordinates and lat/lng.
     */
    val projection: Projection?
        get() = tMap?.projection

    /**
     * Local source of truth for the current camera position.
     * While [map] is non-null this reflects the current position of [map] as it changes.
     * While [map] is null it reflects the last known map position, or the last value set by
     * explicitly setting [position].
     */
    private var rawPosition by mutableStateOf(position)

    /**
     * 注意：这里要转换一下CameraPosition，腾讯地图没有实现Parcelable，我们这里转换一下，才能用rememberSaveable
     */
    fun transformToTxCameraPosition(cameraPosition: CameraPosition) {
        rawPosition = TXCameraPosition(cameraPosition.target,cameraPosition.zoom,cameraPosition.tilt,cameraPosition.bearing)
    }

    var position: TXCameraPosition
        get() = rawPosition
        set(value) {
            synchronized(lock) {
                val map = tMap
                if (map == null) {
                    rawPosition = value
                } else {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(value.latLng,value.zoom,value.tilt,value.bearing)))
                }
            }
        }

    private var tMap: TencentMap? = null

    private val lock = Any()

    // An action to run when the map becomes available or unavailable.
    // represents a mutually exclusive mutation to perform while holding `lock`.
    // Guarded by `lock`.
    private var onMapChanged: OnMapChangedCallback? = null

    /**
     * Set [onMapChanged] to [callback], invoking the current callback's
     * [OnMapChangedCallback.onCancelLocked] if one is present.
     */
    private fun doOnMapChangedLocked(callback: OnMapChangedCallback) {
        onMapChanged?.onCancelLocked()
        onMapChanged = callback
    }

    // A token representing the current owner of any ongoing motion in progress.
    // Used to determine if map animation should stop when calls to animate end.
    // Guarded by `lock`.
    private var movementOwner: Any? = null

    /**
     * Used with [onMapChangedLocked] to execute one-time actions when a map becomes available
     * or is made unavailable. Cancellation is provided in order to resume suspended coroutines
     * that are awaiting the execution of one of these callbacks that will never come.
     */
    private fun interface OnMapChangedCallback {
        fun onMapChangedLocked(newMap: TencentMap?)
        fun onCancelLocked() {}
    }

    internal fun setMap(map: TencentMap?) {
        synchronized(lock) {
            if (this.tMap == null && map == null) return
            if (this.tMap != null && map != null) {
                error("CameraPositionState may only be associated with one TencentMap at a time")
            }
            this.tMap = map
            if (map == null) {
                isMoving = false
            } else {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(position.latLng,position.zoom,position.tilt,position.bearing)))
            }
            onMapChanged?.let {
                // Clear this first since the callback itself might set it again for later
                onMapChanged = null
                it.onMapChangedLocked(map)
            }
        }
    }

    /**
     * Animate the camera position as specified by [update], returning once the animation has
     * completed. [position] will reflect the position of the camera as the animation proceeds.
     *
     * [animate] will throw [CancellationException] if the animation does not fully complete.
     * This can happen if:
     *
     * * The user manipulates the map directly
     * * [position] is set explicitly, e.g. `state.position = CameraPosition(...)`
     * * [animate] is called again before an earlier call to [animate] returns
     * * [move] is called
     * * The calling job is [cancelled][kotlinx.coroutines.Job.cancel] externally
     *
     * If this [CameraPositionState] is not currently bound to a [TencentMap] this call will
     * suspend until a map is bound and animation will begin.
     *
     * This method should only be called from a dispatcher bound to the map's UI thread.
     *
     * @param update the change that should be applied to the camera
     * @param durationMs The duration of the animation in milliseconds. If [Int.MAX_VALUE] is
     * provided, the default animation duration will be used. Otherwise, the value provided must be
     * strictly positive, otherwise an [IllegalArgumentException] will be thrown.
     */
    @UiThread
    suspend fun animate(update: CameraUpdate, durationMs: Int = Integer.MAX_VALUE) {
        val myJob = currentCoroutineContext()[Job]
        try {
            suspendCancellableCoroutine { continuation ->
                synchronized(lock) {
                    movementOwner = myJob
                    val map = tMap
                    if (map == null) {
                        // Do it later
                        val animateOnMapAvailable = object : OnMapChangedCallback {
                            override fun onMapChangedLocked(newMap: TencentMap?) {
                                if (newMap == null) {
                                    // Cancel the animate caller and crash the map setter
                                    continuation.resumeWithException(CancellationException(
                                        "internal error; no TencentMap available"))
                                    error(
                                        "internal error; no TencentMap available to animate position"
                                    )
                                }
                                performAnimateCameraLocked(newMap, update, durationMs, continuation)
                            }

                            override fun onCancelLocked() {
                                continuation.resumeWithException(
                                    CancellationException("Animation cancelled")
                                )
                            }
                        }
                        doOnMapChangedLocked(animateOnMapAvailable)
                        continuation.invokeOnCancellation {
                            synchronized(lock) {
                                if (onMapChanged === animateOnMapAvailable) {
                                    // External cancellation shouldn't invoke onCancel
                                    // so we set this to null directly instead of going through
                                    // doOnMapChangedLocked(null).
                                    onMapChanged = null
                                }
                            }
                        }
                    } else {
                        performAnimateCameraLocked(map, update, durationMs, continuation)
                    }
                }
            }
        } finally {
            // continuation.invokeOnCancellation might be called from any thread, so stop the
            // animation in progress here where we're guaranteed to be back on the right dispatcher.
            synchronized(lock) {
                if (myJob != null && movementOwner === myJob) {
                    movementOwner = null
                    tMap?.stopAnimation()
                }
            }
        }
    }

    private fun performAnimateCameraLocked(
        map: TencentMap,
        update: CameraUpdate,
        durationMs: Int,
        continuation: CancellableContinuation<Unit>
    ) {
        val cancelableCallback = object : TencentMap.CancelableCallback {
            override fun onCancel() {
                continuation.resumeWithException(CancellationException("Animation cancelled"))
            }

            override fun onFinish() {
                continuation.resume(Unit)
            }
        }
        if (durationMs == Integer.MAX_VALUE) {
            map.animateCamera(update, cancelableCallback)
        } else {
            map.animateCamera(update, durationMs.toLong(), cancelableCallback)
        }
        doOnMapChangedLocked {
            check(it == null) {
                "New TencentMap unexpectedly set while an animation was still running"
            }
            map.stopAnimation()
        }
    }

    /**
     * Move the camera instantaneously as specified by [update]. Any calls to [animate] in progress
     * will be cancelled. [position] will be updated when the bound map's position has been updated,
     * or if the map is currently unbound, [update] will be applied when a map is next bound.
     * Other calls to [move], [animate], or setting [position] will override an earlier pending
     * call to [move].
     *
     * This method must be called from the map's UI thread.
     */
    @UiThread
    fun move(update: CameraUpdate) {
        synchronized(lock) {
            val map = tMap
            movementOwner = null
            if (map == null) {
                // Do it when we have a map available
                doOnMapChangedLocked { it?.moveCamera(update) }
            } else {
                map.moveCamera(update)
            }
        }
    }
    companion object {
        val Saver: Saver<CameraPositionState, TXCameraPosition> = Saver(
            save = { it.position },
            restore = { CameraPositionState(it) }
        )
    }
}