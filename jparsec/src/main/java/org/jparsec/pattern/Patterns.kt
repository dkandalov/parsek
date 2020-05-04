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

import org.jparsec.internal.util.Checks.checkMax
import org.jparsec.internal.util.Checks.checkMin
import org.jparsec.internal.util.Checks.checkMinMax
import org.jparsec.internal.util.Checks.checkNonNegative
import org.jparsec.pattern.CharPredicates.notAmong
import org.jparsec.pattern.CharPredicates.notChar
import org.jparsec.pattern.RepeatCharPredicatePattern.Companion.matchRepeat

/**
 * Provides common [Pattern] implementations.
 *
 * @author Ben Yu
 */
object Patterns {
    /** A [Pattern] that always returns [Pattern.MISMATCH].  */
    val NEVER: Pattern = object: Pattern() {
        override fun match(src: CharSequence, begin: Int, end: Int): Int {
            return MISMATCH
        }

        override fun toString(): String {
            return "<>"
        }
    }

    /** A [Pattern] that always matches with match length `0`.  */
    val ALWAYS: Pattern = object: Pattern() {
        override fun match(src: CharSequence, begin: Int, end: Int): Int {
            return 0
        }
    }

    /** A [Pattern] that matches any character and only mismatches for an empty string.  */
    @JvmField
    val ANY_CHAR = hasAtLeast(1)

    /** A [Pattern] object that matches if the input has no character left. Match length is `0` if succeed.  */
    val EOF = hasExact(0)

    /**
     * A [Pattern] object that succeeds with match length `2` if there are at least 2 characters in the input
     * and the first character is `'\'`. Mismatch otherwise.
     */
    val ESCAPED: Pattern = object: Pattern() {
        override fun match(src: CharSequence, begin: Int, end: Int): Int {
            return if (begin >= end - 1) MISMATCH else if (src[begin] == '\\') 2 else MISMATCH
        }
    }

    /** A [Pattern] object that matches an integer.  */
    @JvmField
    val INTEGER = many1(CharPredicates.IS_DIGIT)

    /**
     * A [Pattern] object that matches a decimal number that has at least one digit before the decimal point. The
     * decimal point and the numbers to the right are optional.
     *
     *
     *  `0, 11., 2.3` are all good candidates. While `.1, .` are not.
     */
    val STRICT_DECIMAL = INTEGER.next(isChar('.').next(many(CharPredicates.IS_DIGIT)).optional())

    /** A [Pattern] object that matches a decimal point and one or more digits after it.  */
    @JvmField
    val FRACTION = isChar('.').next(INTEGER)

    /** A [Pattern] object that matches a decimal number that could start with a decimal point or a digit.  */
    @JvmField
    val DECIMAL = STRICT_DECIMAL.or(FRACTION)

    /**
     * A [Pattern] object that matches a standard english word, which starts with either an underscore or an alpha
     * character, followed by 0 or more alphanumeric characters.
     */
    @JvmField
    val WORD = isChar(CharPredicates.IS_ALPHA_).next(isChar(CharPredicates.IS_ALPHA_NUMERIC_).many())

    /**
     * A [Pattern] object that matches an octal integer that starts with a `0` and is followed by 0 or more
     * `[0 - 7]` characters.
     */
    @JvmField
    val OCT_INTEGER = isChar('0').next(many(CharPredicates.range('0', '7')))

    /**
     * A [Pattern] object that matches a decimal integer, which starts with a non-zero digit and is followed by 0 or
     * more digits.
     */
    @JvmField
    val DEC_INTEGER = sequence(range('1', '9'), many(CharPredicates.IS_DIGIT))

    /**
     * A [Pattern] object that matches a hex integer, which starts with a `0x` or `0X`, and is followed
     * by one or more hex digits.
     */
    @JvmField
    val HEX_INTEGER = string("0x").or(string("0X")).next(many1(CharPredicates.IS_HEX_DIGIT))

    /** A [Pattern] object that matches a scientific notation, such as `1e12`, `1.2E-1`, etc.  */
    @JvmField
    val SCIENTIFIC_NOTATION = sequence(DECIMAL, among("eE"), among("+-").optional(), INTEGER)

    /**
     * A [Pattern] object that matches any regular expression pattern string in the form of `/some pattern
     * here/`. `'\'` is used as escape character.
     */
    val REGEXP_PATTERN = regularExpressionPattern

    /** A [Pattern] object that matches regular expression modifiers, which is a list of alpha characters.  */
    val REGEXP_MODIFIERS = modifiersPattern

    /**
     * Returns a [Pattern] object that matches if the input has at least `n` characters left. Match length is
     * `n` if succeed.
     */
    fun hasAtLeast(n: Int): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                return if (begin + n > end) MISMATCH else n
            }

            override fun toString(): String {
                return ".{$n,}"
            }
        }
    }

    /**
     * Returns a [Pattern] object that matches if the input has exactly `n` characters left. Match length is
     * `n` if succeed.
     */
    fun hasExact(n: Int): Pattern {
        checkNonNegative(n, "n < 0")
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                return if (begin + n != end) MISMATCH else n
            }

            override fun toString(): String {
                return ".{$n}"
            }
        }
    }

    /**
     * Returns a [Pattern] object that matches if the current character in the input is equal to character `c`, in which case `1` is returned as match length. Mismatches otherwise.
     */
    @JvmStatic fun isChar(c: Char): Pattern {
        return isChar(CharPredicates.isChar(c))
    }

    /**
     * Returns a [Pattern] object that matches if the current character in the input is between character `c1`
     * and `c2`, in which case `1` is returned as match length.
     */
    fun range(c1: Char, c2: Char): Pattern {
        return isChar(CharPredicates.range(c1, c2))
    }

    /**
     * Returns a [Pattern] object that matches if the current character in the input is equal to any character in
     * `chars`, in which case `1` is returned as match length.
     */
    fun among(chars: String?): Pattern {
        return isChar(CharPredicates.among(chars!!))
    }

    /**
     * Returns a [Pattern] object that matches if the current character in the input satisfies `predicate`, in
     * which case `1` is returned as match length.
     */
    @JvmStatic fun isChar(predicate: CharPredicate): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                return if (begin >= end) MISMATCH else if (predicate.isChar(src[begin])) 1 else MISMATCH
            }

            override fun toString(): String {
                return predicate.toString()
            }
        }
    }

    /**
     * Returns a [Pattern] object that matches a line comment started by `begin` and ended by `EOF` or
     * `LF` (the line feed character).
     */
    @JvmStatic fun lineComment(begin: String): Pattern {
        return string(begin).next(many(notChar('\n')))
    }

    /** Returns a [Pattern] object that matches `string` literally.  */
    @JvmStatic fun string(string: String): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                return if (end - begin < string.length) MISMATCH else matchString(string, src, begin, end)
            }

            override fun toString(): String {
                return string
            }
        }
    }

    /** Returns a [Pattern] object that matches `string` case insensitively.  */
    @JvmStatic fun stringCaseInsensitive(string: String): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                return matchStringCaseInsensitive(string, src, begin, end)
            }

            override fun toString(): String {
                return string.toUpperCase()
            }
        }
    }

    /**
     * Returns a [Pattern] object that matches if the input has at least 1 character and doesn't match `string`. `1` is returned as match length if succeeds.
     */
    @JvmStatic fun notString(string: String): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                if (begin >= end) return MISMATCH
                val matchedLength = matchString(string, src, begin, end)
                return if (matchedLength == MISMATCH || matchedLength < string.length) 1 else MISMATCH
            }

            override fun toString(): String {
                return "!($string)"
            }
        }
    }

    /**
     * Returns a [Pattern] object that matches if the input has at least 1 character and doesn't match `string` case insensitively. `1` is returned as match length if succeeds.
     */
    fun notStringCaseInsensitive(string: String): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                if (begin >= end) return MISMATCH
                return if (matchStringCaseInsensitive(string, src, begin, end) == MISMATCH) 1 else MISMATCH
            }

            override fun toString(): String {
                return "!(" + string.toUpperCase() + ")"
            }
        }
    }

    /**
     *
     * @param pattern
     * @return a [Pattern] that matches iff the input does not match nested `pattern`.
     */
    @JvmStatic fun not(pattern: Pattern): Pattern {
        return pattern.not()
    }

    /**
     * Returns a [Pattern] that matches if all of `patterns` matches, in which case, the maximum match length
     * is returned. Mismatch if any one mismatches.
     */
    fun and(vararg patterns: Pattern): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                var ret = 0
                for (pattern in patterns) {
                    val l = pattern.match(src, begin, end)
                    if (l == MISMATCH) return MISMATCH
                    if (l > ret) ret = l
                }
                return ret
            }

            override fun toString(): String {
                val sb = StringBuilder()
                sb.append('(')
                for (pattern in patterns) {
                    sb.append(pattern).append(" & ")
                }
                if (sb.length > 1) {
                    sb.delete(sb.length - 3, sb.length)
                }
                return sb.append(')').toString()
            }
        }
    }

    /**
     * Returns a [Pattern] that matches if any of `patterns` matches, in which case, the first match length is
     * returned. Mismatch if any one mismatches.
     */
    @JvmStatic fun or(vararg patterns: Pattern): Pattern {
        return OrPattern(*patterns)
    }

    fun orWithoutEmpty(left: Pattern, right: Pattern): Pattern {
        if (right === NEVER) return left
        return if (left === NEVER) right else left.or(right)
    }

    fun nextWithEmpty(left: Pattern, right: Pattern): Pattern {
        if (right === NEVER) return NEVER
        return if (left === NEVER) NEVER else left.next(right)
    }

    /**
     * Returns a [Pattern] object that matches the input against `patterns` sequentially. Te total match
     * length is returned if all succeed.
     */
    @JvmStatic fun sequence(vararg patterns: Pattern): Pattern {
        return SequencePattern(*patterns)
    }

    /**
     * Returns a [Pattern] object that matches if the input has at least `n` characters and the first `n` characters all satisfy `predicate`.
     */
    fun repeat(n: Int, predicate: CharPredicate?): Pattern {
        checkNonNegative(n, "n < 0")
        return RepeatCharPredicatePattern(n, predicate!!)
    }

    /**
     * Returns a [Pattern] object that matches if the input starts with `min` or more characters and all
     * satisfy `predicate`.
     */
    @Deprecated("Use {@link #atLeast(int, CharPredicate)} instead.")
    fun many(min: Int, predicate: CharPredicate): Pattern {
        return atLeast(min, predicate)
    }

    /**
     * Returns a [Pattern] object that matches if the input starts with `min` or more characters and all
     * satisfy `predicate`.
     * @since 2.2
     */
    fun atLeast(min: Int, predicate: CharPredicate): Pattern {
        checkMin(min)
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                val minLen = matchRepeat(min, predicate, src, end, begin, 0)
                return if (minLen == MISMATCH) MISMATCH else matchMany(predicate, src, end, begin + minLen, minLen)
            }

            override fun toString(): String {
                return if (min > 1) "$predicate{$min,}" else "$predicate+"
            }
        }
    }

    /** Returns a [Pattern] that matches 0 or more characters satisfying `predicate`.  */
    @JvmStatic fun many(predicate: CharPredicate): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                return matchMany(predicate, src, end, begin, 0)
            }

            override fun toString(): String {
                return "$predicate*"
            }
        }
    }

    /**
     * Returns a [Pattern] that matches at least `min` and up to `max` number of characters satisfying
     * `predicate`,
     */
    @Deprecated("Use {@link #times(int, int, CharPredicate)} instead.")
    fun some(min: Int, max: Int, predicate: CharPredicate): Pattern {
        return times(min, max, predicate)
    }

    /**
     * Returns a [Pattern] that matches at least `min` and up to `max` number of characters satisfying
     * `predicate`,
     *
     * @since 2.2
     */
    fun times(min: Int, max: Int, predicate: CharPredicate): Pattern {
        checkMinMax(min, max)
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                val minLen = matchRepeat(min, predicate, src, end, begin, 0)
                return if (minLen == MISMATCH) MISMATCH else matchSome(max - min, predicate, src, end, begin + minLen, minLen)
            }
        }
    }

    /**
     * Returns a [Pattern] that matches up to `max` number of characters satisfying
     * `predicate`.
     */
    @Deprecated("Use {@link #atMost(int, CharPredicate)} instead.")
    fun some(max: Int, predicate: CharPredicate): Pattern {
        return atMost(max, predicate)
    }

    /**
     * Returns a [Pattern] that matches up to `max` number of characters satisfying
     * `predicate`.
     * @since 2.2
     */
    fun atMost(max: Int, predicate: CharPredicate): Pattern {
        checkMax(max)
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                return matchSome(max, predicate, src, end, begin, 0)
            }
        }
    }

    /**
     * Returns a [Pattern] that tries both `p1` and `p2`,
     * and picks the one with the longer match length.
     * If both have the same length, `p1` is favored.
     */
    fun longer(p1: Pattern?, p2: Pattern?): Pattern {
        return longest(p1!!, p2!!)
    }

    /**
     * Returns a [Pattern] that tries all of `patterns`, and picks the one with the
     * longest match length. If two patterns have the same length, the first one is favored.
     */
    fun longest(vararg patterns: Pattern): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                var r = MISMATCH
                for (pattern in patterns) {
                    val l = pattern.match(src, begin, end)
                    if (l > r) r = l
                }
                return r
            }
        }
    }

    /**
     * Returns a [Pattern] that tries both `p1` and `p2`, and picks the one with the shorter match
     * length. If both have the same length, `p1` is favored.
     */
    fun shorter(p1: Pattern?, p2: Pattern?): Pattern {
        return shortest(p1!!, p2!!)
    }

    /**
     * Returns a [Pattern] that tries all of `patterns`, and picks the one with the shortest match length. If
     * two patterns have the same length, the first one is favored.
     */
    fun shortest(vararg patterns: Pattern): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                var r = MISMATCH
                for (pattern in patterns) {
                    val l = pattern.match(src, begin, end)
                    if (l != MISMATCH) {
                        if (r == MISMATCH || l < r) r = l
                    }
                }
                return r
            }
        }
    }

    /** Returns a [Pattern] that matches 1 or more characters satisfying `predicate`.  */
    @JvmStatic fun many1(predicate: CharPredicate): Pattern {
        return atLeast(1, predicate)
    }

    /**
     * Adapts a regular expression pattern to a [Pattern].
     *
     *
     * *WARNING*: in addition to regular expression cost, the returned `Pattern` object needs
     * to make a substring copy every time it's evaluated. This can incur excessive copying and memory overhead
     * when parsing large strings. Consider implementing `Pattern` manually for large input.
     */
    fun regex(p: java.util.regex.Pattern): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                if (begin > end) return MISMATCH
                val matcher = p.matcher(src.subSequence(begin, end))
                return if (matcher.lookingAt()) matcher.end() else MISMATCH
            }
        }
    }

    /**
     * Adapts a regular expression pattern string to a [Pattern].
     *
     *
     * *WARNING*: in addition to regular expression cost, the returned `Pattern` object needs
     * to make a substring copy every time it's evaluated. This can incur excessive copying and memory overhead
     * when parsing large strings. Consider implementing `Pattern` manually for large input.
     */
    fun regex(s: String?): Pattern {
        return regex(java.util.regex.Pattern.compile(s))
    }

    fun optional(pp: Pattern?): Pattern {
        return OptionalPattern(pp!!)
    }

    private fun matchSome(max: Int, predicate: CharPredicate, src: CharSequence, len: Int, from: Int, acc: Int): Int {
        val k = Math.min(max + from, len)
        for (i in from until k) {
            if (!predicate.isChar(src[i])) return i - from + acc
        }
        return k - from + acc
    }

    private val regularExpressionPattern: Pattern
        get() {
            val quote = isChar('/')
            val escape = isChar('\\').next(hasAtLeast(1))
            val content = or(escape, isChar(notAmong("/\r\n\\")))
            return quote.next(content.many()).next(quote)
        }

    private val modifiersPattern: Pattern
        get() = isChar(CharPredicates.IS_ALPHA).many()

    private fun matchMany(
        predicate: CharPredicate, src: CharSequence, len: Int, from: Int, acc: Int): Int {
        for (i in from until len) {
            if (!predicate.isChar(src[i])) return i - from + acc
        }
        return len - from + acc
    }

    private fun matchStringCaseInsensitive(str: String, src: CharSequence, begin: Int, end: Int): Int {
        val patternLength = str.length
        if (end - begin < patternLength) return Pattern.MISMATCH
        for (i in 0 until patternLength) {
            val exp = str[i]
            val enc = src[begin + i]
            if (Character.toLowerCase(exp) != Character.toLowerCase(enc)) return Pattern.MISMATCH
        }
        return patternLength
    }

    /**
     * Matches (part of) a character sequence against a pattern string.
     *
     * @param  str   the pattern string.
     * @param  src   the input sequence. Must not be null.
     * @param  begin start of index to scan characters from `src`.
     * @param  end   end of index to scan characters from `src`.
     *
     * @return the number of characters matched, or [Pattern.MISMATCH] if an unexpected character is encountered.
     */
    private fun matchString(str: String, src: CharSequence, begin: Int, end: Int): Int {
        val patternLength = str.length
        var i = 0
        while (i < patternLength && begin + i < end) {
            val exp = str[i]
            val enc = src[begin + i]
            if (exp != enc) return Pattern.MISMATCH
            i++
        }
        return i
    }
}