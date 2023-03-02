/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.support.v4.util

/**
 * 处理：腾讯地图[com.tencent.tencentmap.mapsdk.vector.utils.clustering.algo.GridBasedAlgorithm]方法中需要用到support.v4的LongSparseArray
 */
@Suppress("unused")
class LongSparseArray<E> @JvmOverloads constructor(initialCapacity: Int = 10) : Cloneable {
    private var mGarbage = false
    private var mKeys: LongArray
    private var mValues: Array<Any?>
    private var mSize: Int
    /**
     * Creates a new LongSparseArray containing no mappings that will not
     * require any additional memory allocation to store the specified
     * number of mappings.
     */
    /**
     * Creates a new LongSparseArray containing no mappings.
     */
    init {
        var newInitialCapacity = initialCapacity
        newInitialCapacity = idealLongArraySize(newInitialCapacity)
        mKeys = LongArray(newInitialCapacity)
        mValues = arrayOfNulls(newInitialCapacity)
        mSize = 0
    }

    public override fun clone(): LongSparseArray<E> {
        var clone: LongSparseArray<E>? = null
        try {
            clone = super.clone() as LongSparseArray<E>
            clone.mKeys = mKeys.clone()
            clone.mValues = mValues.clone()
        } catch (ignore: CloneNotSupportedException) {
        }
        return clone!!
    }
    /**
     * Gets the Object mapped from the specified key, or the specified Object
     * if no such mapping has been made.
     */
    /**
     * Gets the Object mapped from the specified key, or `null`
     * if no such mapping has been made.
     */
    @JvmOverloads
    operator fun get(key: Long, valueIfKeyNotFound: E? = null): E? {
        val i = binarySearch(mKeys, 0, mSize, key)
        return if (i < 0 || mValues[i] === DELETED) {
            valueIfKeyNotFound
        } else {
            mValues[i] as E?
        }
    }

    /**
     * Removes the mapping from the specified key, if there was any.
     */
    fun delete(key: Long) {
        val i = binarySearch(mKeys, 0, mSize, key)
        if (i >= 0) {
            if (mValues[i] !== DELETED) {
                mValues[i] = DELETED
                mGarbage = true
            }
        }
    }

    /**
     * Alias for [.delete].
     */
    fun remove(key: Long) {
        delete(key)
    }

    /**
     * Removes the mapping at the specified index.
     */
    fun removeAt(index: Int) {
        if (mValues[index] !== DELETED) {
            mValues[index] = DELETED
            mGarbage = true
        }
    }

    private fun gc() {
        // Log.e("SparseArray", "gc start with " + mSize);
        val n = mSize
        var o = 0
        val keys = mKeys
        val values = mValues
        for (i in 0 until n) {
            val `val` = values[i]
            if (`val` !== DELETED) {
                if (i != o) {
                    keys[o] = keys[i]
                    values[o] = `val`
                    values[i] = null
                }
                o++
            }
        }
        mGarbage = false
        mSize = o
        // Log.e("SparseArray", "gc end with " + mSize);
    }

    /**
     * Adds a mapping from the specified key to the specified value,
     * replacing the previous mapping from the specified key if there
     * was one.
     */
    fun put(key: Long, value: E) {
        var i = binarySearch(mKeys, 0, mSize, key)
        if (i >= 0) {
            mValues[i] = value
        } else {
            i = i.inv()
            if (i < mSize && mValues[i] === DELETED) {
                mKeys[i] = key
                mValues[i] = value
                return
            }
            if (mGarbage && mSize >= mKeys.size) {
                gc()
                // Search again because indices may have changed.
                i = binarySearch(mKeys, 0, mSize, key).inv()
            }
            if (mSize >= mKeys.size) {
                val n = idealLongArraySize(mSize + 1)
                val nkeys = LongArray(n)
                val nvalues = arrayOfNulls<Any>(n)
                // Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
                System.arraycopy(mKeys, 0, nkeys, 0, mKeys.size)
                System.arraycopy(mValues, 0, nvalues, 0, mValues.size)
                mKeys = nkeys
                mValues = nvalues
            }
            if (mSize - i != 0) {
                // Log.e("SparseArray", "move " + (mSize - i));
                System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i)
                System.arraycopy(mValues, i, mValues, i + 1, mSize - i)
            }
            mKeys[i] = key
            mValues[i] = value
            mSize++
        }
    }

    /**
     * Returns the number of key-value mappings that this LongSparseArray
     * currently stores.
     */
    fun size(): Int {
        if (mGarbage) {
            gc()
        }
        return mSize
    }

    /**
     * Given an index in the range `0...size()-1`, returns
     * the key from the `index`th key-value mapping that this
     * LongSparseArray stores.
     */
    fun keyAt(index: Int): Long {
        if (mGarbage) {
            gc()
        }
        return mKeys[index]
    }

    /**
     * Given an index in the range `0...size()-1`, returns
     * the value from the `index`th key-value mapping that this
     * LongSparseArray stores.
     */
    fun valueAt(index: Int): E? {
        if (mGarbage) {
            gc()
        }
        return mValues[index] as E?
    }

    /**
     * Given an index in the range `0...size()-1`, sets a new
     * value for the `index`th key-value mapping that this
     * LongSparseArray stores.
     */
    fun setValueAt(index: Int, value: E) {
        if (mGarbage) {
            gc()
        }
        mValues[index] = value
    }

    /**
     * Returns the index for which [.keyAt] would return the
     * specified key, or a negative number if the specified
     * key is not mapped.
     */
    fun indexOfKey(key: Long): Int {
        if (mGarbage) {
            gc()
        }
        return binarySearch(mKeys, 0, mSize, key)
    }

    /**
     * Returns an index for which [.valueAt] would return the
     * specified key, or a negative number if no keys map to the
     * specified value.
     * Beware that this is a linear search, unlike lookups by key,
     * and that multiple keys can map to the same value and this will
     * find only one of them.
     */
    fun indexOfValue(value: E): Int {
        if (mGarbage) {
            gc()
        }
        for (i in 0 until mSize) if (mValues[i] === value) return i
        return -1
    }

    /**
     * Removes all key-value mappings from this LongSparseArray.
     */
    fun clear() {
        val n = mSize
        val values = mValues
        for (i in 0 until n) {
            values[i] = null
        }
        mSize = 0
        mGarbage = false
    }

    /**
     * Puts a key/value pair into the array, optimizing for the case where
     * the key is greater than all existing keys in the array.
     */
    fun append(key: Long, value: E) {
        if (mSize != 0 && key <= mKeys[mSize - 1]) {
            put(key, value)
            return
        }
        if (mGarbage && mSize >= mKeys.size) {
            gc()
        }
        val pos = mSize
        if (pos >= mKeys.size) {
            val n = idealLongArraySize(pos + 1)
            val nkeys = LongArray(n)
            val nvalues = arrayOfNulls<Any>(n)
            // Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
            System.arraycopy(mKeys, 0, nkeys, 0, mKeys.size)
            System.arraycopy(mValues, 0, nvalues, 0, mValues.size)
            mKeys = nkeys
            mValues = nvalues
        }
        mKeys[pos] = key
        mValues[pos] = value
        mSize = pos + 1
    }

    companion object {
        private val DELETED = Any()
        private fun binarySearch(a: LongArray, start: Int, len: Int, key: Long): Int {
            var high = start + len
            var low = start - 1
            var guess: Int
            while (high - low > 1) {
                guess = (high + low) / 2
                if (a[guess] < key) low = guess else high = guess
            }
            return if (high == start + len) (start + len).inv() else if (a[high] == key) high else high.inv()
        }

        @JvmStatic
        fun idealByteArraySize(need: Int): Int {
            for (i in 4..31) if (need <= (1 shl i) - 12) return (1 shl i) - 12
            return need
        }

        @JvmStatic
        fun idealLongArraySize(need: Int): Int {
            return idealByteArraySize(need * 8) / 8
        }
    }
}