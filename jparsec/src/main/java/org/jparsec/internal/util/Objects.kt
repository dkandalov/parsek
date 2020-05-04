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

/**
 * Utility functions for any object.
 *
 * @author Ben Yu
 */
object Objects {
    /** Gets the has hcode for `obj`. 0 is returned if obj is null.  */
    fun hashCode(obj: Any?): Int {
        return obj?.hashCode() ?: 0
    }

    /**
     * Compares `o1` and `o2` for equality. Returns true if both are `null` or
     * `o1.equals(o2)`.
     */
    fun equals(o1: Any?, o2: Any?): Boolean {
        return if (o1 == null) o2 == null else o1 == o2
    }

    /** Checks whether `obj` is one of the elements of `array`.  */
    @JvmStatic fun `in`(obj: Any, vararg array: Any): Boolean {
        for (expected in array) {
            if (obj === expected) {
                return true
            }
        }
        return false
    }
}