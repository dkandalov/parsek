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
 * Translates the recognized string literal to a [String].
 *
 * @author Ben Yu
 */
internal object StringLiteralsTranslator {
    private fun escapedChar(c: Char): Char {
        return when (c) {
            'r' -> '\r'
            'n' -> '\n'
            't' -> '\t'
            else -> c
        }
    }

    @JvmStatic fun tokenizeDoubleQuote(text: String): String {
        val end = text.length - 1
        val buf = StringBuilder()
        var i = 1
        while (i < end) {
            val c = text[i]
            if (c != '\\') {
                buf.append(c)
            } else {
                val c1 = text[++i]
                buf.append(escapedChar(c1))
            }
            i++
        }
        return buf.toString()
    }

    @JvmStatic fun tokenizeSingleQuote(text: String): String {
        val end = text.length - 1
        val buf = StringBuilder()
        var i = 1
        while (i < end) {
            val c = text[i]
            if (c != '\'') {
                buf.append(c)
            } else {
                buf.append('\'')
                i++
            }
            i++
        }
        return buf.toString()
    }
}