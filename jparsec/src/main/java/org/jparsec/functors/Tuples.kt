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
package org.jparsec.functors

/**
 * Creates [Pair] and tuple instances.
 *
 *
 *  These data holders can be used to hold temporary results during parsing so you don't have to
 * create your own data types.
 *
 * @author Ben Yu
 */
object Tuples {
    /** Returns a [Pair] of 2 objects. Is equivalent to [.tuple].  */
    @JvmStatic fun <A, B> pair(a: A, b: B): Pair<A, B> {
        return Pair(a, b)
    }

    /** Returns a [Pair] of 2 objects. Is equivalent to [.pair].  */
    fun <A, B> tuple(a: A, b: B): Pair<A, B> {
        return pair(a, b)
    }

    /** Returns a [Tuple3] of 3 objects.  */
    fun <A, B, C> tuple(a: A, b: B, c: C): Tuple3<A, B, C> {
        return Tuple3(a, b, c)
    }

    /** Returns a [Tuple4] of 4 objects.  */
    fun <A, B, C, D> tuple(a: A, b: B, c: C, d: D): Tuple4<A, B, C, D> {
        return Tuple4(a, b, c, d)
    }

    /** Returns a [Tuple5] of 5 objects.  */
    fun <A, B, C, D, E> tuple(a: A, b: B, c: C, d: D, e: E): Tuple5<A, B, C, D, E> {
        return Tuple5(a, b, c, d, e)
    }
}