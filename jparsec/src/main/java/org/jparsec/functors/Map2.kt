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

import java.util.function.BiFunction

/**
 * Maps two objects of type `A` and `B` respectively to an object of type `T`.
 *
 * @author Ben Yu
 */
@FunctionalInterface
@Deprecated("""Use {@link java.util.function.BiFunction} instead.
  """)
fun interface Map2<A, B, T>: BiFunction<A, B, T> {
    /** Maps `a` and `b` to the target object.  */
    fun map(a: A, b: B): T
    override fun apply(a: A, b: B): T {
        return map(a, b)
    }
}