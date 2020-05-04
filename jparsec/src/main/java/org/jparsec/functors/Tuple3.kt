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

import org.jparsec.internal.util.Objects

/**
 * Immutable data holder for 3 values.
 *
 * @author Ben Yu
 */
@Deprecated("""Prefer to using a lambda expression to convert to your own type.""")
open class Tuple3<A, B, C>(a: A, b: B, val c: C): Pair<A, B>(a, b) {
    fun equals(other: Tuple3<*, *, *>): Boolean {
        return super.equals(other) && Objects.equals(c, other.c)
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is Tuple3<*, *, *>) {
            equals(obj)
        } else false
    }

    override fun hashCode(): Int {
        return super.hashCode() * 31 + Objects.hashCode(c)
    }

    override fun toString(): String {
        return "($a, $b, $c)"
    }

}