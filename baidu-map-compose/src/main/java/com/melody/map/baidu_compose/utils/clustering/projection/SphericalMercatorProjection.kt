/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.projection

import com.baidu.mapapi.model.LatLng
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

internal class SphericalMercatorProjection constructor(private val worldWidth: Double) {
    fun toPoint(latLng: LatLng?): Point {
        val x: Double = (latLng?.longitude?:0.0) / 360 + .5
        val siny: Double = sin(Math.toRadians(latLng?.latitude?:0.0))
        val y: Double = 0.5 * ln((1 + siny) / (1 - siny)) / -(2 * Math.PI) + .5
        return Point(x * worldWidth, y * worldWidth)
    }

    fun toLatLng(point: Point): LatLng {
        val x: Double = point.x / worldWidth - 0.5
        val lng: Double = x * 360
        val y: Double = .5 - (point.y / worldWidth)
        val lat: Double = 90 - Math.toDegrees(atan(exp(-y * 2 * Math.PI)) * 2)
        return LatLng(lat, lng)
    }
}