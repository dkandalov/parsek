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

/**
 * Transforms the recognized character range to an integer within 64 bits. For bigger integer, use
 * [TokenizerMaps.DECIMAL_FRAGMENT] instead.
 *
 * @author Ben Yu
 */
internal object NumberLiteralsTranslator {
    private fun toDecDigit(c: Char): Int {
        return c - '0'
    }

    private fun toOctDigit(c: Char): Int {
        return c - '0'
    }

    private fun toHexDigit(c: Char): Int {
        if (c in '0'..'9') return c - '0'
        return if (c in 'a'..'h') c - 'a' + 10 else c - 'A' + 10
    }

    fun tokenizeDecimalAsLong(text: String): Long {
        var n: Long = 0
        val len = text.length
        for (i in 0 until len) {
            n = n * 10 + toDecDigit(text[i])
        }
        return n
    }

    fun tokenizeOctalAsLong(text: String): Long {
        var n: Long = 0
        val len = text.length
        for (i in 0 until len) {
            n = n * 8 + toOctDigit(text[i])
        }
        return n
    }

    fun tokenizeHexAsLong(text: String): Long {
        val len = text.length
        check(len >= 3) { "illegal hex number" }
        var n: Long = 0
        for (i in 2 until len) {
            n = n * 16 + toHexDigit(text[i])
        }
        return n
    }
}