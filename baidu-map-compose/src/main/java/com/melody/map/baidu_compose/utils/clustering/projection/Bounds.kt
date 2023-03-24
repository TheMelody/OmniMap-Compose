package com.melody.map.baidu_compose.utils.clustering.projection

/**
 * Represents an area in the cartesian plane.
 */
internal class Bounds constructor(val minX: Double, val maxX: Double, val minY: Double, val maxY: Double) {
    val midX: Double
    val midY: Double

    init {
        midX = (minX + maxX) / 2
        midY = (minY + maxY) / 2
    }

    fun contains(x: Double, y: Double): Boolean {
        return (minX <= x) && (x <= maxX) && (minY <= y) && (y <= maxY)
    }

    operator fun contains(point: Point?): Boolean {
        return contains(point!!.x, point.y)
    }

    operator fun contains(bounds: Bounds): Boolean {
        return (bounds.minX >= minX) && (bounds.maxX <= maxX) && (bounds.minY >= minY) && (bounds.maxY <= maxY)
    }

    fun intersects(minX: Double, maxX: Double, minY: Double, maxY: Double): Boolean {
        return (minX < this.maxX) && (this.minX < maxX) && (minY < this.maxY) && (this.minY < maxY)
    }

    fun intersects(bounds: Bounds): Boolean {
        return intersects(bounds.minX, bounds.maxX, bounds.minY, bounds.maxY)
    }
}