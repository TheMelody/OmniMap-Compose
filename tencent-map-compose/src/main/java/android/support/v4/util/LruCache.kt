/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * 处理：腾讯地图[com.tencent.tencentmap.mapsdk.vector.utils.clustering.algo.PreCachingAlgorithmDecorator]方法中需要用到support.v4的LruCache
 */
@Suppress("unused")
class LruCache<K, V>(maxSize: Int) {
    private val map: LinkedHashMap<K, V>

    /** Size of this cache in units. Not necessarily the number of elements.  */
    private var size = 0
    private val maxSize: Int
    private var putCount = 0
    private var createCount = 0
    private var evictionCount = 0
    private var hitCount = 0
    private var missCount = 0

    /**
     * @param maxSize for caches that do not override [.sizeOf], this is
     * the maximum number of entries in the cache. For all other caches,
     * this is the maximum sum of the sizes of the entries in this cache.
     */
    init {
        require(maxSize > 0) { "maxSize <= 0" }
        this.maxSize = maxSize
        map = LinkedHashMap(0, 0.75f, true)
    }

    /**
     * Returns the value for `key` if it exists in the cache or can be
     * created by `#create`. If a value was returned, it is moved to the
     * head of the queue. This returns null if a value is not cached and cannot
     * be created.
     */
    @Synchronized
    operator fun get(key: K?): V? {
        if (key == null) {
            throw NullPointerException("key == null")
        }
        var result = map[key]
        if (result != null) {
            hitCount++
            return result
        }
        missCount++
        // TODO: release the lock while calling this potentially slow user code
        result = create(key)
        if (result != null) {
            createCount++
            size += safeSizeOf(key, result)
            map[key] = result
            trimToSize(maxSize)
        }
        return result
    }

    /**
     * Caches `value` for `key`. The value is moved to the head of
     * the queue.
     *
     * @return the previous value mapped by `key`. Although that entry is
     * no longer cached, it has not been passed to [.entryEvicted].
     */
    @Synchronized
    fun put(key: K?, value: V?): V? {
        if (key == null || value == null) {
            throw NullPointerException("key == null || value == null")
        }
        putCount++
        size += safeSizeOf(key, value)
        val previous = map.put(key, value)
        if (previous != null) {
            size -= safeSizeOf(key, previous)
        }
        trimToSize(maxSize)
        return previous
    }

    private fun trimToSize(maxSize: Int) {
        while (size > maxSize && !map.isEmpty()) {
            val (key, value) = map.entries.iterator().next()
                ?: break // map is empty; if size is not 0 then throw an error below
            map.remove(key)
            size -= safeSizeOf(key, value)
            evictionCount++
            // TODO: release the lock while calling this potentially slow user code
            entryEvicted(key, value)
        }
        check(!(size < 0 || map.isEmpty() && size != 0)) {
            (javaClass.name
                    + ".sizeOf() is reporting inconsistent results!")
        }
    }

    /**
     * Removes the entry for `key` if it exists.
     *
     * @return the previous value mapped by `key`. Although that entry is
     * no longer cached, it has not been passed to [.entryEvicted].
     */
    @Synchronized
    fun remove(key: K?): V? {
        if (key == null) {
            throw NullPointerException("key == null")
        }
        val previous = map.remove(key)
        if (previous != null) {
            size -= safeSizeOf(key, previous)
        }
        return previous
    }

    /**
     * Called for entries that have reached the tail of the least recently used
     * queue and are be removed. The default implementation does nothing.
     */
    protected fun entryEvicted(key: K, value: V) {}

    /**
     * Called after a cache miss to compute a value for the corresponding key.
     * Returns the computed value or null if no value can be computed. The
     * default implementation returns null.
     */
    protected fun create(key: K): V? {
        return null
    }

    private fun safeSizeOf(key: K, value: V): Int {
        val result = sizeOf(key, value)
        check(result >= 0) { "Negative size: $key=$value" }
        return result
    }

    /**
     * Returns the size of the entry for `key` and `value` in
     * user-defined units.  The default implementation returns 1 so that size
     * is the number of entries and max size is the maximum number of entries.
     *
     *
     * An entry's size must not change while it is in the cache.
     */
    protected fun sizeOf(key: K, value: V): Int {
        return 1
    }

    /**
     * Clear the cache, calling [.entryEvicted] on each removed entry.
     */
    @Synchronized
    fun evictAll() {
        trimToSize(-1) // -1 will evict 0-sized elements
    }

    /**
     * For caches that do not override [.sizeOf], this returns the number
     * of entries in the cache. For all other caches, this returns the sum of
     * the sizes of the entries in this cache.
     */
    @Synchronized
    fun size(): Int {
        return size
    }

    /**
     * For caches that do not override [.sizeOf], this returns the maximum
     * number of entries in the cache. For all other caches, this returns the
     * maximum sum of the sizes of the entries in this cache.
     */
    @Synchronized
    fun maxSize(): Int {
        return maxSize
    }

    /**
     * Returns the number of times [.get] returned a value.
     */
    @Synchronized
    fun hitCount(): Int {
        return hitCount
    }

    /**
     * Returns the number of times [.get] returned null or required a new
     * value to be created.
     */
    @Synchronized
    fun missCount(): Int {
        return missCount
    }

    /**
     * Returns the number of times [.create] returned a value.
     */
    @Synchronized
    fun createCount(): Int {
        return createCount
    }

    /**
     * Returns the number of times [.put] was called.
     */
    @Synchronized
    fun putCount(): Int {
        return putCount
    }

    /**
     * Returns the number of values that have been evicted.
     */
    @Synchronized
    fun evictionCount(): Int {
        return evictionCount
    }

    /**
     * Returns a copy of the current contents of the cache, ordered from least
     * recently accessed to most recently accessed.
     */
    @Synchronized
    fun snapshot(): Map<K, V> {
        return LinkedHashMap(map)
    }

    @Synchronized
    override fun toString(): String {
        val accesses = hitCount + missCount
        val hitPercent = if (accesses != 0) 100 * hitCount / accesses else 0
        return String.format(
            "LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]",
            maxSize, hitCount, missCount, hitPercent
        )
    }
}