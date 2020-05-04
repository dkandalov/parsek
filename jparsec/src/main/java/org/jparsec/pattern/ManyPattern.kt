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

internal class ManyPattern(private val pattern: Pattern): Pattern() {
    override fun match(src: CharSequence, begin: Int, end: Int): Int {
        return matchMany(pattern, src, end, begin, 0)
    }

    override fun toString(): String {
        return "$pattern*"
    }

    companion object {
        fun matchMany(pattern: Pattern, src: CharSequence, len: Int, from: Int, acc: Int): Int {
            var i = from
            while (true) {
                val l = pattern.match(src, i, len)
                if (MISMATCH == l) return i - from + acc
                //we simply stop the loop when infinity is found. this may make the parser more user-friendly.
                if (l == 0) return i - from + acc
                i += l
            }
        }
    }

}