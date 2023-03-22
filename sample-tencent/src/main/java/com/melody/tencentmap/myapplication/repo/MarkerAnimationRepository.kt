package com.melody.tencentmap.myapplication.repo

import android.view.animation.AccelerateDecelerateInterpolator
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.tencent.tencentmap.mapsdk.maps.model.AnimationListener
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.TranslateAnimation

/**
 * MarkerAnimationRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/28 16:32
 */
object MarkerAnimationRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
        )
    }

    fun prepareMarkerAnimation(
        latLng: LatLng,
        onAnimationEnd: () -> Unit
    ): TranslateAnimation {
        return TranslateAnimation(latLng).apply {
            duration = 2500
            interpolator = AccelerateDecelerateInterpolator()
            animationListener = object : AnimationListener {
                override fun onAnimationStart() {
                }
                override fun onAnimationEnd() {
                    onAnimationEnd.invoke()
                }
            }
        }
    }
}