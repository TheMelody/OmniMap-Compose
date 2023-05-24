package com.melody.bdmap.myapplication.repo

import android.content.Context
import android.util.Log
import com.melody.map.baidu_compose.poperties.MapUiSettings
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * BM3DModelRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 15:23
 */
object BM3DModelRepository {

    private const val TAG = "BM3DModelRepository"

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
            isDoubleClickZoomEnabled = true,
            isZoomEnabled = true
        )
    }

    fun copyFilesAssets(context: Context, oldPath: String, newPath: String) {
        var inputStream: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            val fileNames = context.assets.list(oldPath) // 获取assets目录下的所有文件及目录名
            for (i in fileNames!!.indices) {
                val fileNameStr = fileNames[i]
                Log.e(TAG,"copyFilesFassets: $fileNameStr")
            }
            if (fileNames.isNotEmpty()) { // 如果是目录
                val file = File(newPath)
                file.mkdirs() // 如果文件夹不存在，则递归
                for (fileName in fileNames) {
                    copyFilesAssets(
                        context,
                        oldPath + File.separator + fileName,
                        newPath + File.separator + fileName
                    )
                }
            } else { // 如果是文件
                inputStream = context.assets.open(oldPath)
                fos = FileOutputStream(File(newPath))
                val buffer = ByteArray(1024)
                var byteCount = 0
                while (inputStream.read(buffer).also { byteCount = it } != -1) {
                    fos.write(buffer, 0, byteCount)
                }
                fos.flush() // 刷新缓冲区
                inputStream.close()
                fos.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Copy custom style file failed", e)
        } finally {
            try {
                inputStream?.close()
                fos?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Close stream failed", e)
            }
        }
    }
}