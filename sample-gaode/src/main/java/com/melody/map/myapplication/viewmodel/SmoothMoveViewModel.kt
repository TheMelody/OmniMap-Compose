package com.melody.map.myapplication.viewmodel

import com.melody.map.myapplication.contract.SmoothMoveContract
import com.melody.map.myapplication.repo.SmoothMoveRepository
import com.melody.sample.common.base.BaseViewModel

/**
 * SmoothMoveViewModel
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/12 14:18
 */
class SmoothMoveViewModel : BaseViewModel<SmoothMoveContract.Event,SmoothMoveContract.State,SmoothMoveContract.Effect>() {
    override fun createInitialState(): SmoothMoveContract.State {
        return SmoothMoveContract.State(
            isStart = false,
            isMapLoaded = false,
            bounds = null,
            trackPoints = null,
            bitmapTexture = null,
            movingTrackMarker = null,
            totalDuration = 40,
            uiSettings = SmoothMoveRepository.initMapUiSettings()
        )
    }

    override fun handleEvents(event: SmoothMoveContract.Event) {
        when(event) {
            is SmoothMoveContract.Event.PlayPauseEvent -> {
                setState { copy(isStart = !isStart) }
            }
        }
    }

    init {
        asyncLaunch {
            val points = SmoothMoveRepository.readLatLngList()
            val bounds = SmoothMoveRepository.calcLatLngBounds(points)
            val bitmapTexture = SmoothMoveRepository.getPolylineTextureBitmap()
            val movingTrackMarker = SmoothMoveRepository.getPointOverLayMarker()
            setState {
                copy(
                    trackPoints = points,
                    bounds = bounds,
                    bitmapTexture = bitmapTexture,
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

    fun pointOverLayClick() {
        setEffect { SmoothMoveContract.Effect.Toast("可以做一些事情，比如：弹一个Dialog显示车的....某些信息，你觉得呢？") }
    }
}