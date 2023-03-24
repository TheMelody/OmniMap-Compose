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

package com.melody.bdmap.myapplication.viewmodel

import android.animation.TypeEvaluator
import android.view.animation.LinearInterpolator
import com.baidu.mapapi.animation.Animation
import com.baidu.mapapi.animation.Transformation
import com.baidu.mapapi.model.LatLng
import com.melody.bdmap.myapplication.contract.SmoothMoveContract
import com.melody.bdmap.myapplication.repo.SmoothMoveRepository
import com.melody.map.baidu_compose.overlay.MarkerCustomAnimation
import com.melody.map.baidu_compose.utils.BDTrackMoveUtils
import com.melody.sample.common.base.BaseViewModel
import kotlinx.coroutines.Dispatchers

/**
 * SmoothMoveViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/20 16:36
 */
class SmoothMoveViewModel : BaseViewModel<SmoothMoveContract.Event,SmoothMoveContract.State,SmoothMoveContract.Effect>(),
    TypeEvaluator<LatLng> {
    private val bdTrackMoveUtils = BDTrackMoveUtils()

    override fun createInitialState(): SmoothMoveContract.State {
        return SmoothMoveContract.State(
            showStopLabel = false,
            needRestart = true,
            isMapLoaded = false,
            bounds = null,
            trackPoints = emptyList(),
            bitmapTexture = null,
            movingTrackMarker = null,
            trackMarkerAnim = null,
            trackMarkerRotate = 0F,
            uiSettings = SmoothMoveRepository.initMapUiSettings()
        )
    }

    override fun handleEvents(event: SmoothMoveContract.Event) {
        when(event) {
            is SmoothMoveContract.Event.PlayPauseEvent -> {
                if(!currentState.showStopLabel) {
                    // 设置动画
                    val transformation = Transformation(*currentState.trackPoints.toTypedArray())
                    transformation.setDuration(10000)
                    transformation.setInterpolator(LinearInterpolator())
                    transformation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart() {}
                        override fun onAnimationEnd() {
                            setState { copy(showStopLabel = false) }
                        }
                        override fun onAnimationCancel() {}
                        override fun onAnimationRepeat() {}
                    })
                    setState {
                        copy(
                            trackMarkerAnim = MarkerCustomAnimation.create(
                                animation = transformation,
                                typeEvaluator = this@SmoothMoveViewModel
                            )
                        )
                    }
                }
                setState { copy(showStopLabel = !showStopLabel) }
            }
        }
    }

    init {
        asyncLaunch(Dispatchers.IO) {
            val points = SmoothMoveRepository.readLatLngList()
            val bounds = SmoothMoveRepository.calcLatLngBounds(points)
            val bitmapTexture = SmoothMoveRepository.getPolylineTextureBitmap()
            val movingTrackMarker = SmoothMoveRepository.getPointOverLayMarker()
            setState {
                copy(
                    trackPoints = points,
                    bounds = bounds,
                    bitmapTexture = bitmapTexture,
                    trackMarkerRotate = bdTrackMoveUtils.getAngle(0, points),
                    movingTrackMarker = movingTrackMarker
                )
            }
        }
    }

    fun handleMapLoaded() {
        setState { copy(isMapLoaded = true) }
    }

    fun toggle() {
        setEvent(SmoothMoveContract.Event.PlayPauseEvent)
    }

    /**
     * 自定义估值器，在小车移动过程中更新小车旋转角度
     */
    override fun evaluate(fraction: Float, startPoint: LatLng?, endPoint: LatLng?): LatLng {
        if(startPoint == null || endPoint == null) return LatLng(0.0,0.0)
        val x = startPoint.longitude + fraction * (endPoint.longitude - startPoint.longitude)
        val y = startPoint.latitude + fraction * (endPoint.latitude - startPoint.latitude)

        val angle: Float = bdTrackMoveUtils.getAngle(startPoint, endPoint)
        // 更新小车旋转角度
        if (angle != currentState.trackMarkerRotate) {
            setState { copy(trackMarkerRotate = angle) }
        }

        return LatLng(y, x)
    }
}