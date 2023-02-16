package com.melody.sample.common.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.melody.sample.common.model.ISensorDegreeListener
import kotlin.math.abs

class SensorEventHelper : SensorEventListener {
    private val mSensorManager: SensorManager = SDKUtils.getApplicationContext()
        .getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val mAccelerometer: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var lastTime: Long = 0
    private var mAngle = 0f
    private var accelermoterValues : FloatArray ?= null  //  FloatArray(3) 不在这里初始化
    private var magneticFieldValues : FloatArray ?= null // FloatArray(3)  不在这里初始化
    private var iSensorDegreeListener:ISensorDegreeListener? = null

    companion object {
        private const val TIME_SENSOR = 100
    }

    fun registerSensorListener(changeDegreeListener: ISensorDegreeListener) {
        iSensorDegreeListener = changeDegreeListener
        mSensorManager.registerListener(
            this, mAccelerometer,
            /*Sensor.TYPE_ACCELEROMETER*/
            SensorManager.SENSOR_DELAY_UI
        )
        mSensorManager.registerListener(
            this, magneticField,
            /*Sensor.TYPE_MAGNETIC_FIELD*/
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun unRegisterSensorListener() {
        iSensorDegreeListener = null
        mSensorManager.unregisterListener(this, mAccelerometer)
        mSensorManager.unregisterListener(this, magneticField)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (System.currentTimeMillis() - lastTime < TIME_SENSOR) {
            return
        }
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelermoterValues = event.values
        }
        if(event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values
        }
        if(null == accelermoterValues || null == magneticFieldValues) return
        val values = FloatArray(3)
        val R = FloatArray(9)
        SensorManager.getRotationMatrix(R, null, accelermoterValues, magneticFieldValues)
        SensorManager.getOrientation(R, values)
        var x = Math.toDegrees(values[0].toDouble()).toFloat()
        x += getScreenRotationOnPhone(SDKUtils.getApplicationContext())
        x %= 360.0F
        if (x > 180.0F) {
            x -= 360.0F
        }else if (x < -180.0F) {
            x += 360.0F
        }
        if (abs(mAngle - x) < 3F) {  // if (abs(mAngle - x) <= 8.0F)
            return
        }
        mAngle = if (java.lang.Float.isNaN(x)) 0F else x
        iSensorDegreeListener?.onSensorDegree(360 - mAngle)
        lastTime = System.currentTimeMillis()
    }

    /**
     * 获取当前屏幕旋转角度
     * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
     */
    private fun getScreenRotationOnPhone(context: Context): Int {
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        }
        when (display?.rotation) {
            Surface.ROTATION_0 -> return 0
            Surface.ROTATION_90 -> return 90
            Surface.ROTATION_180 -> return 180
            Surface.ROTATION_270 -> return -90
        }
        return 0
    }
}