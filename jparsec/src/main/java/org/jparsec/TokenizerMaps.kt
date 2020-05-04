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

import org.jparsec.StringLiteralsTranslator.tokenizeDoubleQuote
import org.jparsec.StringLiteralsTranslator.tokenizeSingleQuote
import org.jparsec.Tokens.Fragment
import org.jparsec.Tokens.ScientificNotation
import org.jparsec.Tokens.fragment
import org.jparsec.Tokens.scientificNotation
import java.util.function.Function

/**
 * Common [Function] implementations that maps from [String].
 *
 * @author Ben Yu
 */
internal object TokenizerMaps {
    /** A [Function] that returns a [Tokens.Fragment] tagged as [Tag.RESERVED].  */
    val RESERVED_FRAGMENT = fragment(Tokens.Tag.RESERVED)

    /** A [Function] that returns a [Tokens.Fragment] tagged as [Tag.IDENTIFIER].  */
    @JvmField
    val IDENTIFIER_FRAGMENT = fragment(Tokens.Tag.IDENTIFIER)

    /** A [Function] that returns a [Tokens.Fragment] tagged as [Tag.INTEGER].  */
    @JvmField
    val INTEGER_FRAGMENT = fragment(Tokens.Tag.INTEGER)

    /** A [Function] that returns a [Tokens.Fragment] tagged as [Tag.DECIMAL].  */
    @JvmField
    val DECIMAL_FRAGMENT = fragment(Tokens.Tag.DECIMAL)

    /**
     * A [Function] that recognizes a scientific notation
     * and tokenizes to a [ScientificNotation].
     */
    @JvmField
    val SCIENTIFIC_NOTATION: Function<String, ScientificNotation> = object: Function<String, ScientificNotation> {
        override fun apply(text: String): ScientificNotation {
            var e = text.indexOf('e')
            if (e < 0) {
                e = text.indexOf('E')
            }
            // we know for sure the string is in expected format, so don't bother checking.
            val significand = text.substring(0, e)
            val exponent = text.substring(e + if (text[e + 1] == '+') 2 else 1, text.length)
            return scientificNotation(significand, exponent)
        }

        override fun toString(): String {
            return "SCIENTIFIC_NOTATION"
        }
    }

    /**
     * A [Function] that recognizes a string literal quoted by double quote character
     * (`"`) and tokenizes to a `String`. The backslash character (`\`) is
     * interpreted as escape.
     */
    @JvmField
    val DOUBLE_QUOTE_STRING: Function<String, String> = object: Function<String, String> {
        override fun apply(text: String): String {
            return tokenizeDoubleQuote(text)
        }

        override fun toString(): String {
            return "DOUBLE_QUOTE_STRING"
        }
    }

    /**
     * A [Function] that tokenizes a SQL style string literal quoted by single quote character
     * (`'`) and tokenizes to a `String`. Two adjacent single quote characters
     * (`''`) are escaped as one single quote character.
     */
    @JvmField
    val SINGLE_QUOTE_STRING: Function<String, String> = object: Function<String, String> {
        override fun apply(text: String): String {
            return tokenizeSingleQuote(text)
        }

        override fun toString(): String {
            return "SINGLE_QUOTE_STRING"
        }
    }

    /**
     * A [Function] that recognizes a character literal quoted by single quote characte
     * (`'` and tokenizes to a [Character]. The backslash character (`\`) is
     * interpreted as escape.
     */
    @JvmField
    val SINGLE_QUOTE_CHAR: Function<String, Char> = object: Function<String, Char> {
        override fun apply(text: String): Char {
            val len = text.length
            if (len == 3) return text[1] else if (len == 4) return text[2]
            throw IllegalStateException("illegal char")
        }

        override fun toString(): String {
            return "SINGLE_QUOTE_CHAR"
        }
    }

    /**
     * A [Function] that interprets the recognized character range
     * as a decimal integer and tokenizes to a [Long].
     */
    @JvmField
    val DEC_AS_LONG: Function<String, Long> = object: Function<String, Long> {
        override fun apply(text: String): Long {
            return NumberLiteralsTranslator.tokenizeDecimalAsLong(text)
        }

        override fun toString(): String {
            return "DEC_AS_LONG"
        }
    }

    /**
     * A [Function] that interprets the recognized character range
     * as a octal integer and tokenizes to a [Long].
     */
    @JvmField
    val OCT_AS_LONG: Function<String, Long> = object: Function<String, Long> {
        override fun apply(text: String): Long {
            return NumberLiteralsTranslator.tokenizeOctalAsLong(text)
        }

        override fun toString(): String {
            return "OCT_AS_LONG"
        }
    }

    /**
     * A [Function] that interprets the recognized character range
     * as a hexadecimal integer and tokenizes to a [Long].
     */
    @JvmField
    val HEX_AS_LONG: Function<String, Long> = object: Function<String, Long> {
        override fun apply(text: String): Long {
            return NumberLiteralsTranslator.tokenizeHexAsLong(text)
        }

        override fun toString(): String {
            return "HEX_AS_LONG"
        }
    }

    /**
     * Returns a map that tokenizes the recognized character range to a
     * [Tokens.Fragment] object tagged with `tag`.
     */
    fun fragment(tag: Any): Function<String, Fragment> {
        return object: Function<String, Fragment> {
            override fun apply(text: String): Fragment {
                return fragment(text, tag)
            }

            override fun toString(): String {
                return tag.toString()
            }
        }
    }
}