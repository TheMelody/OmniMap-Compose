/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.projection

internal class Point constructor(val x: Double, val y: Double) {
    override fun toString(): String {
        return ("Point{"
                + "x=" + x
                + ", y=" + y
                + '}')
    }
}