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

import org.jparsec.internal.util.Checks.checkNotNull
import org.jparsec.internal.util.Objects

/**
 * Parsed result with the matched source text.
 *
 * @author Stepan Koltsov
 */
class WithSource<T>(val value: T, source: String) {
    /** Returns the parsed result.  */
    /** Returns the underlying source text. Never null.  */
    val source: String = source

    /** Returns the underlying source text.  */
    override fun toString(): String {
        return source
    }

    override fun equals(o: Any?): Boolean {
        if (o is WithSource<*>) {
            val that = o
            return (Objects.equals(value, that.value)
                && source == that.source)
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value) * 31 + source.hashCode()
    }

}