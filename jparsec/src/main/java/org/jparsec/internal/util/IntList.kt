/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 * *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 */
package org.jparsec.internal.util

import org.jparsec.internal.annotations.Private

/**
 * A simple, efficient and dynamic int list.
 *
 *
 *  Not thread-safe.
 *
 * @author Ben Yu.
 */
class IntList @JvmOverloads constructor(capacity: Int = 10) {
    private var buf: IntArray
    private var len = 0

    /** Creates a `int[]` object with all the elements.  */
    fun toArray(): IntArray {
        val ret = IntArray(len)
        for (i in 0 until len) {
            ret[i] = buf[i]
        }
        return ret
    }

    /** Gets the number of int values stored.  */
    fun size(): Int {
        return len
    }

    private fun checkIndex(i: Int) {
        if (i < 0 || i >= len) throw ArrayIndexOutOfBoundsException(i)
    }

    /**
     * Gets the int value at a index `i`.
     * @param i the 0 - based index of the value.
     * @return the int value.
     * @throws ArrayIndexOutOfBoundsException if `i &lt; 0 or i >= size()`.
     */
    operator fun get(i: Int): Int {
        checkIndex(i)
        return buf[i]
    }

    /**
     * Sets the value at index `i` to `val`.
     *
     * @param i the 0 - based index.
     * @param val the new value.
     * @return the old value.
     * @throws ArrayIndexOutOfBoundsException if `i &lt; 0 or i >= size()`.
     */
    operator fun set(i: Int, `val`: Int): Int {
        checkIndex(i)
        val old = buf[i]
        buf[i] = `val`
        return old
    }

    /**
     * Ensures that there is at least `l` capacity.
     *
     * @param capacity the minimal capacity.
     */
    fun ensureCapacity(capacity: Int) {
        if (capacity > buf.size) {
            val factor = buf.size / 2 + 1
            grow(calcSize(capacity - buf.size, factor))
        }
    }

    private fun grow(l: Int) {
        val nbuf = IntArray(buf.size + l)
        System.arraycopy(buf, 0, nbuf, 0, buf.size)
        buf = nbuf
    }

    /**
     * Adds `i` into the array.
     *
     * @param i the int value.
     * @return this object.
     */
    fun add(i: Int): IntList {
        ensureCapacity(len + 1)
        buf[len++] = i
        return this
    }

    companion object {
        @Private fun calcSize(expectedSize: Int, factor: Int): Int {
            val rem = expectedSize % factor
            return expectedSize / factor * factor + if (rem > 0) factor else 0
        }
    }
    /** Creates an [IntList] object with initial capacity equal to `capacity`.  */
    /** Creates an empty [IntList] object.  */
    init {
        buf = IntArray(capacity)
    }
}