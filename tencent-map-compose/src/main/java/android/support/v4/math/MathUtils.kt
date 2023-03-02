package android.support.v4.math

/**
 * 处理：腾讯地图[com.tencent.tencentmap.mapsdk.vector.utils.animation.MarkerTranslateAnimator.setAnimatorPosition]方法中需要用到support.v4的MathUtils
 */
@Suppress("unused")
object MathUtils {
    /**
     * See [Math.addExact].
     */
    @JvmStatic
    fun addExact(x: Int, y: Int): Int {
        // copied from Math.java
        val r = x + y
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (x xor r and (y xor r) < 0) {
            throw ArithmeticException("integer overflow")
        }
        return r
    }

    /**
     * See [Math.addExact].
     */
    @JvmStatic
    fun addExact(x: Long, y: Long): Long {
        // copied from Math.java
        val r = x + y
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (x xor r and (y xor r) < 0) {
            throw ArithmeticException("long overflow")
        }
        return r
    }

    /**
     * See [Math.subtractExact].
     */
    @JvmStatic
    fun subtractExact(x: Int, y: Int): Int {
        // copied from Math.java
        val r = x - y
        // HD 2-12 Overflow iff the arguments have different signs and
        // the sign of the result is different than the sign of x
        if (x xor y and (x xor r) < 0) {
            throw ArithmeticException("integer overflow")
        }
        return r
    }

    /**
     * See [Math.subtractExact].
     */
    @JvmStatic
    fun subtractExact(x: Long, y: Long): Long {
        // copied from Math.java
        val r = x - y
        // HD 2-12 Overflow iff the arguments have different signs and
        // the sign of the result is different than the sign of x
        if (x xor y and (x xor r) < 0) {
            throw ArithmeticException("long overflow")
        }
        return r
    }

    /**
     * See [Math.multiplyExact].
     */
    @JvmStatic
    fun multiplyExact(x: Int, y: Int): Int {
        // copied from Math.java
        val r = x.toLong() * y.toLong()
        if (r.toInt().toLong() != r) {
            throw ArithmeticException("integer overflow")
        }
        return r.toInt()
    }

    /**
     * See [Math.multiplyExact].
     */
    @JvmStatic
    fun multiplyExact(x: Long, y: Long): Long {
        // copied from Math.java
        val r = x * y
        val ax = Math.abs(x)
        val ay = Math.abs(y)
        if (ax or ay ushr 31 != 0L) {
            // Some bits greater than 2^31 that might cause overflow
            // Check the result using the divide operator
            // and check for the special case of Long.MIN_VALUE * -1
            if ((y != 0L && r / y != x || x == Long.MIN_VALUE) && y == -1L) {
                throw ArithmeticException("long overflow")
            }
        }
        return r
    }

    /**
     * See [Math.incrementExact].
     */
    @JvmStatic
    fun incrementExact(a: Int): Int {
        // copied from Math.java
        if (a == Int.MAX_VALUE) {
            throw ArithmeticException("integer overflow")
        }
        return a + 1
    }

    /**
     * See [Math.incrementExact].
     */
    @JvmStatic
    fun incrementExact(a: Long): Long {
        // copied from Math.java
        if (a == Long.MAX_VALUE) {
            throw ArithmeticException("long overflow")
        }
        return a + 1L
    }

    /**
     * See [Math.decrementExact].
     */
    @JvmStatic
    fun decrementExact(a: Int): Int {
        // copied from Math.java
        if (a == Int.MIN_VALUE) {
            throw ArithmeticException("integer overflow")
        }
        return a - 1
    }

    /**
     * See [Math.decrementExact].
     */
    @JvmStatic
    fun decrementExact(a: Long): Long {
        // copied from Math.java
        if (a == Long.MIN_VALUE) {
            throw ArithmeticException("long overflow")
        }
        return a - 1L
    }

    /**
     * See [Math.negateExact].
     */
    @JvmStatic
    fun negateExact(a: Int): Int {
        // copied from Math.java
        if (a == Int.MIN_VALUE) {
            throw ArithmeticException("integer overflow")
        }
        return -a
    }

    /**
     * See [Math.negateExact].
     */
    @JvmStatic
    fun negateExact(a: Long): Long {
        // copied from Math.java
        if (a == Long.MIN_VALUE) {
            throw ArithmeticException("long overflow")
        }
        return -a
    }

    /**
     * See [Math.toIntExact].
     */
    @JvmStatic
    fun toIntExact(value: Long): Int {
        // copied from Math.java
        if (value.toInt().toLong() != value) {
            throw ArithmeticException("integer overflow")
        }
        return value.toInt()
    }

    /**
     * This method takes a numerical value and ensures it fits in a given numerical range. If the
     * number is smaller than the minimum required by the range, then the minimum of the range will
     * be returned. If the number is higher than the maximum allowed by the range then the maximum
     * of the range will be returned.
     *
     * @param value the value to be clamped.
     * @param min minimum resulting value.
     * @param max maximum resulting value.
     *
     * @return the clamped value.
     */
    @JvmStatic
    fun clamp(value: Float, min: Float, max: Float): Float {
        if (value < min) {
            return min
        } else if (value > max) {
            return max
        }
        return value
    }

    /**
     * This method takes a numerical value and ensures it fits in a given numerical range. If the
     * number is smaller than the minimum required by the range, then the minimum of the range will
     * be returned. If the number is higher than the maximum allowed by the range then the maximum
     * of the range will be returned.
     *
     * @param value the value to be clamped.
     * @param min minimum resulting value.
     * @param max maximum resulting value.
     *
     * @return the clamped value.
     */
    @JvmStatic
    fun clamp(value: Double, min: Double, max: Double): Double {
        if (value < min) {
            return min
        } else if (value > max) {
            return max
        }
        return value
    }

    /**
     * This method takes a numerical value and ensures it fits in a given numerical range. If the
     * number is smaller than the minimum required by the range, then the minimum of the range will
     * be returned. If the number is higher than the maximum allowed by the range then the maximum
     * of the range will be returned.
     *
     * @param value the value to be clamped.
     * @param min minimum resulting value.
     * @param max maximum resulting value.
     *
     * @return the clamped value.
     */
    @JvmStatic
    fun clamp(value: Int, min: Int, max: Int): Int {
        if (value < min) {
            return min
        } else if (value > max) {
            return max
        }
        return value
    }

    /**
     * This method takes a numerical value and ensures it fits in a given numerical range. If the
     * number is smaller than the minimum required by the range, then the minimum of the range will
     * be returned. If the number is higher than the maximum allowed by the range then the maximum
     * of the range will be returned.
     *
     * @param value the value to be clamped.
     * @param min minimum resulting value.
     * @param max maximum resulting value.
     *
     * @return the clamped value.
     */
    @JvmStatic
    fun clamp(value: Long, min: Long, max: Long): Long {
        if (value < min) {
            return min
        } else if (value > max) {
            return max
        }
        return value
    }
}