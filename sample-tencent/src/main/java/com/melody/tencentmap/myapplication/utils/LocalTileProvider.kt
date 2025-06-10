package com.melody.tencentmap.myapplication.utils

import android.content.Context
import android.util.Log
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.Tile
import com.tencent.tencentmap.mapsdk.maps.model.TileProvider
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

/**
 * 取自腾讯demo
 */
class LocalTileProvider(private val context: Context) : TileProvider {

    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        val latLng = LatLng(39.917505, 116.397657)
        val iZ = 16
        val n = 2.0.pow(iZ.toDouble())
        val iX = (((latLng.getLongitude() + 180) / 360) * n).toInt()
        val iY = ((1 - (ln(
            tan(Math.toRadians(latLng.getLatitude())) +
                    (1 / cos(Math.toRadians(latLng.getLatitude())))
        ) / Math.PI)) / 2 * n).toInt()
        if (iX == x && iY == y && iZ == zoom) {
            Log.e("tile", "zoom:$zoom x:$x y:$y")
            return Tile(256, 256, tileData())
        }
        return TileProvider.NO_TILE

    }

    private fun tileData(): ByteArray? {
        return try {
            context.assets.open("gugong.jpg").use { inputStream ->
                ByteArrayOutputStream().use { byteArrayOutputStream ->
                    val byteBuffer = ByteArray(1024)
                    var count: Int
                    while (inputStream.read(byteBuffer).also { count = it } != -1) {
                        byteArrayOutputStream.write(byteBuffer, 0, count)
                    }
                    byteArrayOutputStream.toByteArray()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}