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

internal class RepeatCharPredicatePattern(private val n: Int, private val predicate: CharPredicate): Pattern() {
    override fun match(src: CharSequence, begin: Int, end: Int): Int {
        return matchRepeat(n, predicate, src, end, begin, 0)
    }

    override fun toString(): String {
        return "$predicate{$n}"
    }

    companion object {
        @JvmStatic fun matchRepeat(n: Int, predicate: CharPredicate, src: CharSequence, length: Int, begin: Int, acc: Int): Int {
            val end = begin + n
            if (end > length) return MISMATCH
            for (i in begin until end) {
                if (!predicate.isChar(src[i])) return MISMATCH
            }
            return n + acc
        }
    }
}