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
package org.jparsec.error

/**
 * Represents a line and column number of a character or token in the source.
 *
 * @author Ben Yu
 */
@Deprecated("""Prefer to use {@link org.jparsec.SourceLocation} instead.""")
class Location
/**
 * Creates a [Location] instance.
 *
 * @param line line number
 * @param column column number
 */(
    /** 1-based line number.  */
    @JvmField val line: Int,
    /** 1-based column number.  */
    @JvmField val column: Int
) {

    override fun equals(obj: Any?): Boolean {
        if (obj is Location) {
            val other = obj
            return line == other.line && column == other.column
        }
        return false
    }

    override fun hashCode(): Int {
        return line * 31 + column
    }

    override fun toString(): String {
        return "line $line column $column"
    }

}