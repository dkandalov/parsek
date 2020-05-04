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
 * Provides common token values.
 *
 * @author Ben Yu
 */
object Tokens {
    /**
     * Returns a [Fragment] tagged with `tag`.
     *
     * @param text the fragment text.
     * @param tag the tag representing the fragment's semantics.
     */
    @JvmStatic fun fragment(text: String, tag: Any): Fragment {
        return Fragment(text, tag)
    }

    /**
     * Returns a [Fragment] tagged as [Tag.RESERVED].
     *
     * @param name the reserved word.
     * @return the token value.
     */
    @JvmStatic fun reserved(name: String): Fragment {
        return fragment(name, Tag.RESERVED)
    }

    /**
     * Returns a [Fragment] tagged as [Tag.IDENTIFIER].
     *
     * @param name the identifier.
     * @return the token value.
     */
    fun identifier(name: String): Fragment {
        return fragment(name, Tag.IDENTIFIER)
    }

    /**
     * Returns a [Fragment] tagged as [Tag.DECIMAL].
     *
     * @param s the decimal string representation.
     * @return the token value.
     */
    fun decimalLiteral(s: String): Fragment {
        return fragment(s, Tag.DECIMAL)
    }

    /**
     * Returns a [Fragment] tagged as [Tag.INTEGER].
     *
     * @param s the integer string representation.
     * @return the token value.
     */
    fun integerLiteral(s: String): Fragment {
        return fragment(s, Tag.INTEGER)
    }

    /**
     * Returns a [ScientificNotation] with `significand` before the 'e' or 'E'
     * and `exponent` after.
     */
    @JvmStatic fun scientificNotation(significand: String, exponent: String): ScientificNotation {
        return ScientificNotation(significand, exponent)
    }

    /**
     * Represents a fragment tagged according to its semantics.
     * It's a convenience class so that you don't have to create many classes each for a different
     * token. Instead, you could just use `new fragment(text, "token1")` to uniquely identify
     * a token by the "token1" tag.
     */
    class Fragment @Deprecated("Use {@code Tokens.fragment()} instead. ") constructor(private val text: String, private val tag: Any) {

        /** Returns the text of the token value.  */
        fun text(): String {
            return text
        }

        /** Returns the tag of the token value.  */
        fun tag(): Any {
            return tag
        }

        fun equalFragment(that: Fragment): Boolean {
            return tag == that.tag && text == that.text
        }

        override fun equals(obj: Any?): Boolean {
            return if (obj is Fragment) {
                equalFragment(obj)
            } else false
        }

        override fun hashCode(): Int {
            return tag.hashCode() * 31 + text.hashCode()
        }

        override fun toString(): String {
            return text
        }

    }

    /**
     * Represents a scientific notation with a significand (mantissa) and an exponent. Both are
     * represented with a [String] to avoid number range issue.
     */
    class ScientificNotation @Deprecated("Use {@code Tokens.scientificNotation()} instead. ") constructor(
        /** The significand (mantissa) before the "E".  */
        val significand: String,
        /** The exponent after the "E".  */
        val exponent // we leave the range check to the semantics analysis
        : String) {

        override fun toString(): String {
            return significand + "E" + exponent
        }

        override fun equals(obj: Any?): Boolean {
            if (obj is ScientificNotation) {
                val that = obj
                return significand == that.significand && exponent == that.exponent
            }
            return false
        }

        override fun hashCode(): Int {
            return significand.hashCode() * 31 + exponent.hashCode()
        }

    }

    /** Pre-built [Fragment] token tags.  */
    enum class Tag {
        /** Reserved word  */
        RESERVED,

        /** Regular identifier  */
        IDENTIFIER,

        /** Integral number literal  */
        INTEGER,

        /** Decimal number literal  */
        DECIMAL
    }
}