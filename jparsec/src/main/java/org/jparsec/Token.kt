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

import org.jparsec.internal.util.Objects

/**
 * Represents any token with a token value and the 0-based index in the source.
 *
 * @author Ben Yu
 */
class Token
/**
 * @param index the starting index.
 * @param length the length of the token.
 * @param value the token value.
 */(private val ind: Int, private val len: Int, private val value: Any?) {

    fun length(): Int {
        return len
    }

    fun index(): Int {
        return ind
    }

    fun value(): Any? {
        return value
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun hashCode(): Int {
        return (ind * 31 + len) * 31 + Objects.hashCode(value)
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is Token) {
            equalToken(obj)
        } else false
    }

    private fun equalToken(that: Token): Boolean {
        return ind == that.ind && len == that.len && Objects.equals(value, that.value)
    }

}