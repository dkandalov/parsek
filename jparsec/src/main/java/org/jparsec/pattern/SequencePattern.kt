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
package org.jparsec.pattern

internal class SequencePattern(private vararg val patterns: Pattern): Pattern() {
    override fun match(src: CharSequence, begin: Int, end: Int): Int {
        var current = begin
        for (pattern in patterns) {
            val l = pattern.match(src, current, end)
            if (l == MISMATCH) return l
            current += l
        }
        return current - begin
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (pattern in patterns) {
            sb.append(pattern)
        }
        return sb.toString()
    }
}