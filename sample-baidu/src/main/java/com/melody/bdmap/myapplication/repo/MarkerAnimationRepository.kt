package com.melody.bdmap.myapplication.repo

import android.view.animation.AccelerateDecelerateInterpolator
import com.baidu.mapapi.animation.Animation
import com.baidu.mapapi.animation.Transformation
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.overlay.MarkerCustomAnimation
import com.melody.map.baidu_compose.poperties.MapUiSettings

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
    ): MarkerCustomAnimation {
        return MarkerCustomAnimation.create(animation = Transformation(latLng).apply {
            setDuration(2500)
            setInterpolator(AccelerateDecelerateInterpolator())
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart() {
                }
                override fun onAnimationEnd() {
                    onAnimationEnd.invoke()
                }
                override fun onAnimationCancel() {
                    onAnimationEnd.invoke()
                }
                override fun onAnimationRepeat() {
                }
            })
        })
    }
}