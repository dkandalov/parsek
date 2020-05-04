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
 * Immutable data holder for 5 values.
 *
 * @author Ben Yu
 */
@Deprecated("""Prefer to using a lambda expression to convert to your own type.
  """)
class Tuple5<A, B, C, D, E>(a: A, b: B, c: C, d: D, val e: E): Tuple4<A, B, C, D>(a, b, c, d) {
    fun equals(other: Tuple5<*, *, *, *, *>): Boolean {
        return super.equals(other) && Objects.equals(e, other.e)
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is Tuple5<*, *, *, *, *>) {
            equals(obj)
        } else false
    }

    override fun hashCode(): Int {
        return super.hashCode() * 31 + Objects.hashCode(e)
    }

    override fun toString(): String {
        return "($a, $b, $c, $d, $e)"
    }

}