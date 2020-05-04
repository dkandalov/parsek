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
package org.jparsec

import org.jparsec.functors.Map3
import org.jparsec.functors.Map4
import org.jparsec.functors.Map5
import java.util.function.BiFunction

/**
 * Functors used only internally by this package.
 *
 * @author Ben Yu
 */
internal object InternalFunctors {
    @JvmStatic fun tokenWithSameValue(value: Any): TokenMap<Token?> {
        return object: TokenMap<Token?> {
            override fun map(token: Token): Token? {
                return (if (value === token.value()) token else null)
            }

            override fun toString(): String {
                return value.toString()
            }
        }
    }

    val FIRST_OF_TWO: BiFunction<*, *, *> = object: BiFunction<Any?, Any?, Any?> {
        override fun apply(first: Any?, b: Any?): Any? {
            return first
        }

        override fun toString(): String {
            return "followedBy"
        }
    }
    private val LAST_OF_TWO: BiFunction<*, *, *> = object: BiFunction<Any?, Any?, Any?> {
        override fun apply(a: Any?, last: Any?): Any? {
            return last
        }

        override fun toString(): String {
            return "sequence"
        }
    }
    private val LAST_OF_THREE: Map3<*, *, *, *> = object: Map3<Any?, Any?, Any?, Any?> {
        override fun map(a: Any?, b: Any?, last: Any?): Any? {
            return last
        }

        override fun toString(): String {
            return "sequence"
        }
    }
    private val LAST_OF_FOUR: Map4<*, *, *, *, *> = object: Map4<Any?, Any?, Any?, Any?, Any?> {
        override fun map(a: Any?, b: Any?, c: Any?, last: Any?): Any? {
            return last
        }

        override fun toString(): String {
            return "sequence"
        }
    }
    private val LAST_OF_FIVE: Map5<*, *, *, *, *, *> = object: Map5<Any?, Any?, Any?, Any?, Any?, Any?> {
        override fun map(a: Any?, b: Any?, c: Any?, d: Any?, last: Any?): Any? {
            return last
        }

        override fun toString(): String {
            return "sequence"
        }
    }

    @JvmStatic fun <T, B> firstOfTwo(): BiFunction<T, B, T> {
        return FIRST_OF_TWO as BiFunction<T, B, T>
    }

    fun <A, T> lastOfTwo(): BiFunction<A, T, T> {
        return LAST_OF_TWO as BiFunction<A, T, T>
    }

    fun <A, B, T> lastOfThree(): Map3<A, B, T, T> {
        return LAST_OF_THREE as Map3<A, B, T, T>
    }

    fun <A, B, C, T> lastOfFour(): Map4<A, B, C, T, T> {
        return LAST_OF_FOUR as Map4<A, B, C, T, T>
    }

    fun <A, B, C, D, T> lastOfFive(): Map5<A, B, C, D, T, T> {
        return LAST_OF_FIVE as Map5<A, B, C, D, T, T>
    }
}