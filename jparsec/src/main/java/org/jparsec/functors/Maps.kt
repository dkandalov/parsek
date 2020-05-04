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

import java.util.*
import java.util.function.Function
import java.util.function.UnaryOperator

/**
 * Provides common implementations of [Map] interface and the variants.
 *
 * @author Ben Yu
 */
object Maps {
    /**
     * The [Map] that maps a [String] to [Integer] by calling
     * [Integer.valueOf].
     *
     */
    @Deprecated("Use {@code Integer::valueOf} directly.")
    val TO_INTEGER = Function { s: String? -> Integer.valueOf(s) }

    /** The [UnaryOperator] that maps a [String] to lower case using [Locale.US].  */
    var TO_LOWER_CASE = toLowerCase(Locale.US)

    /** Returns a [UnaryOperator] that maps a [String] to lower case using `locale`.  */
    fun toLowerCase(locale: Locale?): UnaryOperator<String> {
        return object: UnaryOperator<String> {
            override fun apply(s: String): String {
                return s.toLowerCase(locale!!)
            }

            override fun toString(): String {
                return "toLowerCase"
            }
        }
    }

    /** The [UnaryOperator] that maps a [String] to upper case using [Locale.US].  */
    var TO_UPPER_CASE = toUpperCase(Locale.US)

    /** Returns a [UnaryOperator] that maps a [String] to upper case using `locale`.  */
    fun toUpperCase(locale: Locale?): UnaryOperator<String> {
        return object: UnaryOperator<String> {
            override fun apply(s: String): String {
                return s.toUpperCase(locale!!)
            }

            override fun toString(): String {
                return "toUpperCase"
            }
        }
    }

    @Deprecated("Use {@code String::valueOf} directly.")
    fun <T> mapToString(): Map<T, String> {
        return Map { obj: T -> obj.toString() }
    }

    /**
     * Returns a [Map] that maps the string representation of an enum
     * to the corresponding enum value by calling [Enum.valueOf].
     */
    fun <E: Enum<E>?> toEnum(enumType: Class<E>): Function<String, E> {
        return object: Function<String, E> {
            override fun apply(name: String): E {
                return java.lang.Enum.valueOf(enumType, name)
            }

            override fun toString(): String {
                return "-> " + enumType.name
            }
        }
    }

    /**
     * Returns an identity map that maps parameter to itself.
     *
     */
    @Deprecated("Use {@link Function#identity} instead.") fun <T> identity(): UnaryOperator<T> {
        return UnaryOperator { v: T -> v }
    }

    /**
     * Returns a [Map] that always maps any object to `v`.
     *
     */
    @Deprecated("Use {@code from -> to} directly.")
    fun <F, T> constant(v: T): Function<F, T> {
        return Function { from: F -> v }
    }

    /**
     * Adapts a [java.util.Map] to [Map].
     *
     */
    @Deprecated("Use {@code Map::get} instead.")
    fun <K, V> map(m: kotlin.collections.Map<K, V>): Function<K, V?> {
        return Function { key: K -> m[key] }
    }

    /** A [Map2] object that maps 2 values into a [Pair] object.  */
    fun <A, B> toPair(): Map2<A, B, Pair<A, B>> {
        return Map2 { a: A, b: B -> Pair(a, b) }
    }

    /** A [Map3] object that maps 3 values to a [Tuple3] object.  */
    @Deprecated("") fun <A, B, C> toTuple3(): Map3<A, B, C, Tuple3<A, B, C>> {
        return Map3 { a: A, b: B, c: C -> Tuple3(a, b, c) }
    }

    /** A [Map4] object that maps 4 values to a [Tuple4] object.  */
    @Deprecated("") fun <A, B, C, D> toTuple4(): Map4<A, B, C, D, Tuple4<A, B, C, D>> {
        return Map4 { a: A, b: B, c: C, d: D -> Tuple4(a, b, c, d) }
    }

    /** A [Map5] object that maps 5 values to a [Tuple5] object.  */
    @Deprecated("") fun <A, B, C, D, E> toTuple5(): Map5<A, B, C, D, E, Tuple5<A, B, C, D, E>> {
        return Map5 { a: A, b: B, c: C, d: D, e: E -> Tuple5(a, b, c, d, e) }
    }
}