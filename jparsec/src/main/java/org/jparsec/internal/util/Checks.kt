/*****************************************************************************
 * Copyright 2013 (C) jparsec.org                                                *
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
 * Common facilities to check precondition, postcondition and invariants.
 *
 * @author Ben Yu
 */
object Checks {
    /** Checks that `value` cannot be null.  */
    @JvmStatic @Throws(NullPointerException::class) fun <T> checkNotNull(value: T?): T {
        if (value == null) {
            throw NullPointerException()
        }
        return value
    }

    /**
     * Checks that an argument satisfies requirement.
     *
     * @param  condition the condition that has to be true
     * @param  message   the error message if `condition` is false
     * @param  args      the arguments to the error message
     *
     * @throws IllegalArgumentException if `condition` is false
     */
    @Throws(IllegalArgumentException::class)
    @JvmStatic
    fun checkArgument(condition: Boolean, message: String) {
        require(condition) { message }
    }

    /**
     * Checks that an argument satisfies requirement.
     *
     * @param  condition the condition that has to be true
     * @param  message   the error message if `condition` is false
     * @param  args      the arguments to the error message
     *
     * @throws IllegalArgumentException if `condition` is false
     */
    @JvmStatic @Throws(IllegalArgumentException::class)
    fun checkArgument(condition: Boolean, message: String?, vararg args: Any?) {
        require(condition) { String.format(message!!, *args) }
    }

    /**
     * Checks a certain state.
     *
     * @param  condition the condition of the state that has to be true
     * @param  message   the error message if `condition` is false
     * @param  args      the arguments to the error message
     *
     * @throws IllegalStateException if `condition` is false
     */
    @JvmStatic fun checkState(condition: Boolean, message: String) {
        check(condition) { message }
    }

    /**
     * Checks a certain state.
     *
     * @param  condition the condition of the state that has to be true
     * @param  message   the error message if `condition` is false
     * @param  args      the arguments to the error message
     *
     * @throws IllegalStateException if `condition` is false
     */
    @JvmStatic @Throws(IllegalStateException::class)
    fun checkState(condition: Boolean, message: String?, vararg args: Any?) {
        check(condition) { String.format(message!!, *args) }
    }

    /**
     * Checks that `object` is not null.
     *
     * @param  object  the object that cannot be null
     * @param  message the error message if `condition` is false
     * @param  args    the arguments to the error message
     *
     * @throws IllegalStateException if `object` is null
     */
    @JvmStatic fun checkNotNullState(`object`: Any?, message: String?) {
        checkState(`object` != null, message)
    }

    /**
     * Checks that `object` is not null.
     *
     * @param  object  the object that cannot be null
     * @param  message the error message if `condition` is false
     * @param  args    the arguments to the error message
     *
     * @throws IllegalStateException if `object` is null
     */
    @JvmStatic fun checkNotNullState(`object`: Any?, message: String?, vararg args: Any?) {
        checkState(`object` != null, message, *args)
    }

    /** Checks that neither `min` or `max` is negative and `min &lt;= max`.  */
    @JvmStatic fun checkMinMax(min: Int, max: Int) {
        checkMin(min)
        checkMax(max)
        checkArgument(min <= max, "min > max")
    }

    /** Checks that `min` isn't negative.  */
    @JvmStatic fun checkMin(min: Int): Int {
        checkNonNegative(min, "min < 0")
        return min
    }

    /** Checks that `max` isn't negative.  */
    @JvmStatic fun checkMax(max: Int): Int {
        checkNonNegative(max, "max < 0")
        return max
    }

    /** Checks that `n` isn't negative. Or throws an [IllegalArgumentException] with `message`.  */
    @JvmStatic fun checkNonNegative(n: Int, message: String?): Int {
        checkArgument(n >= 0, message)
        return n
    }
}