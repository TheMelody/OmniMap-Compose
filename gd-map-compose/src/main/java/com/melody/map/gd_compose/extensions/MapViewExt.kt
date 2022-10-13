package com.melody.map.gd_compose.extensions

import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal suspend inline fun MapView.awaitMap(): AMap =
    suspendCoroutine { continuation ->
        continuation.resume(map)
    }