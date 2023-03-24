package com.melody.tencentmap.myapplication.repo

import android.view.animation.AccelerateDecelerateInterpolator
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.tencent.tencentmap.mapsdk.maps.model.AnimationListener
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.TranslateAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

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

    suspend fun prepareMarkerAnimation(
        latLng: LatLng,
        onPlayAnimation: () -> Unit,
        onAnimationFinish: () -> Unit,
        block: (TranslateAnimation) -> Unit
    ) {
        block.invoke(
            TranslateAnimation(latLng).apply {
                duration = 2500
                interpolator = AccelerateDecelerateInterpolator()
                animationListener = object : AnimationListener {
                    override fun onAnimationStart() {
                    }

                    override fun onAnimationEnd() {
                        onAnimationFinish.invoke()
                    }
                }
            }
        )
        // 不要立马去操作动画播放
        delay(100)
        onPlayAnimation()
    }
}