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

import org.fest.assertions.Assertions
import org.jparsec.pattern.Pattern.Companion.MISMATCH
import org.jparsec.pattern.Patterns.ALWAYS
import org.jparsec.pattern.Patterns.ANY_CHAR
import org.jparsec.pattern.Patterns.DECIMAL
import org.jparsec.pattern.Patterns.DEC_INTEGER
import org.jparsec.pattern.Patterns.EOF
import org.jparsec.pattern.Patterns.ESCAPED
import org.jparsec.pattern.Patterns.FRACTION
import org.jparsec.pattern.Patterns.HEX_INTEGER
import org.jparsec.pattern.Patterns.INTEGER
import org.jparsec.pattern.Patterns.NEVER
import org.jparsec.pattern.Patterns.OCT_INTEGER
import org.jparsec.pattern.Patterns.REGEXP_MODIFIERS
import org.jparsec.pattern.Patterns.REGEXP_PATTERN
import org.jparsec.pattern.Patterns.SCIENTIFIC_NOTATION
import org.jparsec.pattern.Patterns.STRICT_DECIMAL
import org.jparsec.pattern.Patterns.WORD
import org.jparsec.pattern.Patterns.among
import org.jparsec.pattern.Patterns.and
import org.jparsec.pattern.Patterns.atLeast
import org.jparsec.pattern.Patterns.atMost
import org.jparsec.pattern.Patterns.hasAtLeast
import org.jparsec.pattern.Patterns.hasExact
import org.jparsec.pattern.Patterns.isChar
import org.jparsec.pattern.Patterns.lineComment
import org.jparsec.pattern.Patterns.longer
import org.jparsec.pattern.Patterns.longest
import org.jparsec.pattern.Patterns.many
import org.jparsec.pattern.Patterns.many1
import org.jparsec.pattern.Patterns.not
import org.jparsec.pattern.Patterns.notString
import org.jparsec.pattern.Patterns.notStringCaseInsensitive
import org.jparsec.pattern.Patterns.or
import org.jparsec.pattern.Patterns.range
import org.jparsec.pattern.Patterns.regex
import org.jparsec.pattern.Patterns.repeat
import org.jparsec.pattern.Patterns.sequence
import org.jparsec.pattern.Patterns.shorter
import org.jparsec.pattern.Patterns.shortest
import org.jparsec.pattern.Patterns.string
import org.jparsec.pattern.Patterns.stringCaseInsensitive
import org.jparsec.pattern.Patterns.times
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class PatternsTest {
    @Test fun testAlways() {
        assertEquals(0, ALWAYS.match("", 0, 0).toLong())
        assertEquals(0, ALWAYS.match("abc", 0, 0).toLong())
        assertEquals(0, ALWAYS.match("abc", 1, 2).toLong())
    }

    @Test fun testNever() {
        assertEquals(MISMATCH.toLong(), NEVER.match("", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), NEVER.match("abc", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), NEVER.match("abc", 1, 2).toLong())
    }

    @Test fun testAnyChar() {
        assertEquals(1, ANY_CHAR.match("a", 0, 1).toLong())
        assertEquals(1, ANY_CHAR.match("abc", 0, 1).toLong())
        assertEquals(1, ANY_CHAR.match("abc", 1, 2).toLong())
        assertEquals(1, ANY_CHAR.match("abc", 0, 2).toLong())
        assertEquals(MISMATCH.toLong(), ANY_CHAR.match("", 0, 0).toLong())
    }

    @Test fun testHasAtLeast() {
        assertEquals(1, hasAtLeast(1).match("a", 0, 1).toLong())
        assertEquals(1, hasAtLeast(1).match("abc", 0, 1).toLong())
        assertEquals(1, hasAtLeast(1).match("abc", 1, 2).toLong())
        assertEquals(2, hasAtLeast(2).match("abc", 0, 2).toLong())
        assertEquals(2, hasAtLeast(2).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), hasAtLeast(2).match("a", 0, 1).toLong())
    }

    @Test fun testHasExact() {
        assertEquals(1, hasExact(1).match("a", 0, 1).toLong())
        assertEquals(1, hasExact(1).match("abc", 0, 1).toLong())
        assertEquals(1, hasExact(1).match("abc", 1, 2).toLong())
        assertEquals(2, hasExact(2).match("abc", 0, 2).toLong())
        assertEquals(MISMATCH.toLong(), hasExact(2).match("abc", 0, 3).toLong())
    }

    @Test fun testHasExactThrowsExceptionWhenNIsNegative() {
        try {
            hasExact(-1)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("n < 0", e.message)
        }
    }

    @Test fun testEof() {
        assertEquals(0, EOF.match("", 0, 0).toLong())
        assertEquals(0, EOF.match("abc", 0, 0).toLong())
        assertEquals(0, EOF.match("abc", 3, 3).toLong())
    }

    @Test fun testIsChar() {
        assertEquals(1, isChar('a').match("a", 0, 1).toLong())
        assertEquals(1, isChar('a').match(" a", 1, 2).toLong())
        assertEquals(MISMATCH.toLong(), isChar('a').match("ba", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), isChar('a').match("a", 0, 0).toLong())
    }

    @Test fun testIsChar_withPredicate() {
        assertEquals(1, isChar(CharPredicates.ALWAYS).match("x", 0, 1).toLong())
        assertEquals(1, isChar(CharPredicates.ALWAYS).match(" x", 1, 2).toLong())
        assertEquals(MISMATCH.toLong(), isChar(CharPredicates.NEVER).match("a", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), isChar(CharPredicates.ALWAYS).match("X", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), isChar(CharPredicates.NEVER).match("X", 0, 0).toLong())
    }

    @Test fun testRange() {
        assertEquals(1, range('a', 'c').match("a", 0, 1).toLong())
        assertEquals(1, range('a', 'c').match("b", 0, 1).toLong())
        assertEquals(1, range('a', 'c').match("c", 0, 1).toLong())
        assertEquals(1, range('a', 'c').match("abc", 0, 1).toLong())
        assertEquals(1, range('a', 'c').match("ba", 1, 2).toLong())
        assertEquals(MISMATCH.toLong(), range('a', 'c').match("d", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), range('a', 'c').match("0", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), range('a', 'c').match("a", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), range('a', 'c').match("a", 0, 0).toLong())
    }

    @Test fun testAmong() {
        val pattern = among("a1")
        assertEquals(1, pattern.match("a", 0, 1).toLong())
        assertEquals(1, pattern.match("10", 0, 1).toLong())
        assertEquals(1, pattern.match("a", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), pattern.match("", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), pattern.match("0", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), pattern.match("1b", 1, 2).toLong())
    }

    @Test fun testEscaped() {
        assertEquals(2, ESCAPED.match("\\0", 0, 2).toLong())
        assertEquals(2, ESCAPED.match("x\\0", 1, 3).toLong())
        assertEquals(MISMATCH.toLong(), ESCAPED.match("\\0", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), ESCAPED.match("012", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), ESCAPED.match("\\0", 0, 0).toLong())
    }

    @Test fun testLineComment() {
        assertEquals(4, lineComment("//").match("//ab", 0, 4).toLong())
        assertEquals(4, lineComment("//").match("//ab\n", 0, 5).toLong())
        assertEquals(5, lineComment("//").match("//ab\r\n", 0, 6).toLong())
        assertEquals(MISMATCH.toLong(), lineComment("//").match("/ab\r\n", 0, 6).toLong())
        assertEquals(MISMATCH.toLong(), lineComment("//").match("//ab\r\n", 0, 0).toLong())
    }

    @Test fun testString() {
        assertEquals(3, string("abc").match("abcd", 0, 4).toLong())
        assertEquals(0, string("").match("abcd", 0, 4).toLong())
        assertEquals(MISMATCH.toLong(), string("abc").match("ABC", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), string("abc").match("abc", 0, 0).toLong())
    }

    @Test fun testStringCaseInsensitive() {
        assertEquals(0, stringCaseInsensitive("").match("a", 0, 0).toLong())
        assertEquals(3, stringCaseInsensitive("abc").match("abcd", 0, 4).toLong())
        assertEquals(3, stringCaseInsensitive("abc").match("ABC", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), stringCaseInsensitive("abc").match("ABx", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), stringCaseInsensitive("abc").match("abc", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), stringCaseInsensitive("abc").match("ab", 0, 0).toLong())
    }

    @Test fun testNotString() {
        assertEquals(MISMATCH.toLong(), notString("abc").match("abcd", 0, 4).toLong())
        assertEquals(MISMATCH.toLong(), notString("").match("abc", 0, 0).toLong())
        assertEquals(1, notString("abc").match("ABC", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), notString("abc").match("abc", 0, 0).toLong())
    }

    @Test fun testNotStringCaseInsensitive() {
        assertEquals(MISMATCH.toLong(), notStringCaseInsensitive("").match("a", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), notStringCaseInsensitive("abc").match("abcd", 0, 4).toLong())
        assertEquals(MISMATCH.toLong(), notStringCaseInsensitive("abc").match("ABC", 0, 3).toLong())
        assertEquals(1, notStringCaseInsensitive("abc").match("ABx", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), notStringCaseInsensitive("abc").match("abc", 0, 0).toLong())
    }

    @Test fun testAnd() {
        assertEquals(2, and(hasAtLeast(1), hasAtLeast(2)).match("abc", 0, 3).toLong())
        assertEquals(2, and(hasAtLeast(2), hasAtLeast(1)).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), and(hasAtLeast(1), NEVER).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), and(NEVER, hasAtLeast(1), ALWAYS).match("abc", 0, 3).toLong())
    }

    @Test fun testOr() {
        assertEquals(1, or(hasAtLeast(1), hasAtLeast(2)).match("abc", 0, 3).toLong())
        assertEquals(2, or(hasAtLeast(2), hasAtLeast(1)).match("abc", 0, 3).toLong())
        assertEquals(1, or(hasAtLeast(1), NEVER).match("abc", 0, 3).toLong())
        assertEquals(1, or(NEVER, hasAtLeast(1), ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), or(NEVER, NEVER).match("abc", 0, 3).toLong())
    }

    @Test fun testSequence() {
        assertEquals(3, sequence(hasAtLeast(1), hasAtLeast(2)).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), sequence(hasAtLeast(1), hasAtLeast(3)).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), sequence(NEVER, hasAtLeast(2)).match("abc", 0, 3).toLong())
    }

    @Test fun testRepeat() {
        assertEquals(3, repeat(3, CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(2, repeat(2, CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), repeat(3, CharPredicates.NEVER).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), repeat(3, CharPredicates.ALWAYS).match("abc", 0, 2).toLong())
    }

    @Test fun testRepeatAnyIsNotEquivalentToHasExact() {
        Assertions.assertThat(repeat(2, CharPredicates.ALWAYS).match("abc", 0, 3)) //
            .isNotEqualTo(hasExact(2).match("abc", 0, 3))
    }

    @Test fun testRepeat_negativeNumberThrows() {
        try {
            repeat(-1, CharPredicates.ALWAYS)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("n < 0", e.message)
        }
    }

    @Test fun testMany() {
        assertEquals(3, many(CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(0, many(CharPredicates.NEVER).match("abc", 0, 3).toLong())
        assertEquals(0, many(CharPredicates.ALWAYS).match("", 0, 0).toLong())
    }

    @Test fun testMany1() {
        assertEquals(3, many1(CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), many1(CharPredicates.ALWAYS).match("abc", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), many1(CharPredicates.NEVER).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), many1(CharPredicates.ALWAYS).match("", 0, 0).toLong())
    }

    @Test fun testMany_withMin() {
        assertEquals(3, atLeast(3, CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), atLeast(4, CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), atLeast(1, CharPredicates.NEVER).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), atLeast(1, CharPredicates.ALWAYS).match("", 0, 0).toLong())
    }

    @Test fun testMany_negativeNumberThrows() {
        try {
            atLeast(-1, CharPredicates.ALWAYS)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("min < 0", e.message)
        }
    }

    @Test fun testSome() {
        assertEquals(2, atMost(2, CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(0, atMost(0, CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(0, atMost(1, CharPredicates.NEVER).match("abc", 0, 3).toLong())
        assertEquals(0, atMost(2, CharPredicates.ALWAYS).match("", 0, 0).toLong())
    }

    @Test fun testSome_withMin() {
        assertEquals(3, times(1, 4, CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), times(4, 5, CharPredicates.ALWAYS).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), times(1, 1, CharPredicates.NEVER).match("abc", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), times(1, 1, CharPredicates.ALWAYS).match("", 0, 0).toLong())
    }

    @Test fun testSome_negativeMaxThrows() {
        try {
            atMost(-1, CharPredicates.ALWAYS)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("max < 0", e.message)
        }
        try {
            times(0, -1, CharPredicates.ALWAYS)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("max < 0", e.message)
        }
    }

    @Test fun testSome_negativeMinThrows() {
        try {
            times(-1, 1, CharPredicates.ALWAYS)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("min < 0", e.message)
        }
    }

    @Test fun testSome_minBiggerThanMaxThrows() {
        try {
            times(1, 0, CharPredicates.ALWAYS)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("min > max", e.message)
        }
    }

    @Test fun testLonger() {
        assertEquals(0, longer(ALWAYS, NEVER).match("", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), longer(NEVER, NEVER).match("", 0, 0).toLong())
        assertEquals(0, longer(ALWAYS, ALWAYS).match("", 0, 0).toLong())
        assertEquals(1, longer(hasAtLeast(1), NEVER).match("a", 0, 1).toLong())
        assertEquals(1, longer(hasAtLeast(1), ALWAYS).match("a", 0, 1).toLong())
        assertEquals(2, longer(hasAtLeast(1), hasExact(2)).match("ab", 0, 2).toLong())
    }

    @Test fun testLongest() {
        assertEquals(0, longest(ALWAYS, NEVER, ALWAYS).match("", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), longest(NEVER, NEVER, NEVER).match("", 0, 0).toLong())
        assertEquals(0, longest(ALWAYS, ALWAYS, ALWAYS).match("", 0, 0).toLong())
        assertEquals(1, longest(hasAtLeast(1), NEVER, ALWAYS).match("a", 0, 1).toLong())
        assertEquals(2, longest(hasAtLeast(1), hasExact(2), NEVER).match("ab", 0, 2).toLong())
    }

    @Test fun testShorter() {
        assertEquals(0, shorter(ALWAYS, NEVER).match("", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), shorter(NEVER, NEVER).match("", 0, 0).toLong())
        assertEquals(0, shorter(ALWAYS, ALWAYS).match("", 0, 0).toLong())
        assertEquals(1, shorter(hasAtLeast(1), NEVER).match("a", 0, 1).toLong())
        assertEquals(0, shorter(hasAtLeast(1), ALWAYS).match("a", 0, 1).toLong())
        assertEquals(1, shorter(hasAtLeast(1), hasExact(2)).match("ab", 0, 2).toLong())
    }

    @Test fun testShortest() {
        assertEquals(0, shortest(ALWAYS, NEVER, ALWAYS).match("", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), shortest(NEVER, NEVER, NEVER).match("", 0, 0).toLong())
        assertEquals(0, shortest(ALWAYS, ALWAYS, ALWAYS).match("", 0, 0).toLong())
        assertEquals(0, shortest(hasAtLeast(1), NEVER, ALWAYS).match("a", 0, 1).toLong())
        assertEquals(1, shortest(hasAtLeast(1), hasExact(2), NEVER).match("ab", 0, 2).toLong())
    }

    @Test fun testDecimalL() {
        assertEquals(2, STRICT_DECIMAL.match("12a", 0, 3).toLong())
        assertEquals(3, STRICT_DECIMAL.match("12.a", 0, 4).toLong())
        assertEquals(2, STRICT_DECIMAL.match("0.", 0, 2).toLong())
        assertEquals(5, STRICT_DECIMAL.match("12.34 ", 0, 6).toLong())
        assertEquals(MISMATCH.toLong(), STRICT_DECIMAL.match(".34 ", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), STRICT_DECIMAL.match("a.34 ", 0, 4).toLong())
        assertEquals(MISMATCH.toLong(), STRICT_DECIMAL.match("", 0, 0).toLong())
    }

    @Test fun testDecimalR() {
        assertEquals(2, FRACTION.match(".1", 0, 2).toLong())
        assertEquals(2, FRACTION.match(".0a", 0, 3).toLong())
        assertEquals(2, FRACTION.match(".1", 0, 2).toLong())
        assertEquals(MISMATCH.toLong(), FRACTION.match("12.34 ", 0, 6).toLong())
        assertEquals(MISMATCH.toLong(), FRACTION.match("a.34 ", 0, 4).toLong())
        assertEquals(MISMATCH.toLong(), FRACTION.match(". ", 0, 2).toLong())
        assertEquals(MISMATCH.toLong(), FRACTION.match("", 0, 0).toLong())
    }

    @Test fun testDecimal() {
        assertEquals(3, DECIMAL.match("1.2", 0, 3).toLong())
        assertEquals(2, DECIMAL.match("12", 0, 2).toLong())
        assertEquals(2, DECIMAL.match(".1", 0, 2).toLong())
        assertEquals(MISMATCH.toLong(), DECIMAL.match(".", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), DECIMAL.match("", 0, 0).toLong())
    }

    @Test fun testWord() {
        assertEquals(1, WORD.match("a", 0, 1).toLong())
        assertEquals(1, WORD.match("A", 0, 1).toLong())
        assertEquals(1, WORD.match("_", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), WORD.match("0", 0, 1).toLong())
        assertEquals(6, WORD.match("abc_01", 0, 6).toLong())
        assertEquals(MISMATCH.toLong(), WORD.match("", 0, 0).toLong())
    }

    @Test fun testInteger() {
        assertEquals(1, INTEGER.match("1", 0, 1).toLong())
        assertEquals(2, INTEGER.match("12a", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), INTEGER.match("a", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), INTEGER.match("", 0, 0).toLong())
    }

    @Test fun testOctInteger() {
        assertEquals(1, OCT_INTEGER.match("0", 0, 1).toLong())
        assertEquals(2, OCT_INTEGER.match("01", 0, 2).toLong())
        assertEquals(3, OCT_INTEGER.match("0078", 0, 4).toLong())
        assertEquals(MISMATCH.toLong(), OCT_INTEGER.match("1", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), OCT_INTEGER.match("", 0, 0).toLong())
    }

    @Test fun testDecInteger() {
        assertEquals(1, DEC_INTEGER.match("1", 0, 1).toLong())
        assertEquals(3, DEC_INTEGER.match("109", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), DEC_INTEGER.match("0", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), DEC_INTEGER.match("", 0, 0).toLong())
    }

    @Test fun testHexInteger() {
        assertEquals(4, HEX_INTEGER.match("0x3F", 0, 4).toLong())
        assertEquals(4, HEX_INTEGER.match("0XAf", 0, 4).toLong())
        assertEquals(3, HEX_INTEGER.match("0X0", 0, 3).toLong())
        assertEquals(MISMATCH.toLong(), HEX_INTEGER.match("0X", 0, 2).toLong())
        assertEquals(MISMATCH.toLong(), HEX_INTEGER.match("0X", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), HEX_INTEGER.match("0X", 0, 0).toLong())
    }

    @Test fun testScientificNumber() {
        val pattern = SCIENTIFIC_NOTATION
        assertEquals(3, pattern.match("0e1", 0, 3).toLong())
        assertEquals(5, pattern.match("12e12", 0, 5).toLong())
        assertEquals(6, pattern.match("0.1E12", 0, 6).toLong())
        assertEquals(7, pattern.match("1.9E+12", 0, 7).toLong())
        assertEquals(5, pattern.match("1E-12", 0, 5).toLong())
        assertEquals(5, pattern.match("1e-12", 0, 5).toLong())
        assertEquals(MISMATCH.toLong(), pattern.match("e", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), pattern.match("0", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), pattern.match("e", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), pattern.match("e1", 0, 0).toLong())
    }

    @Test fun testRegex() {
        assertEquals(3, regex("a*").match("aaab", 0, 4).toLong())
        assertEquals(3, regex("a*").match("aaab", 0, 3).toLong())
        assertEquals(2, regex("a*").match("aaab", 0, 2).toLong())
        assertEquals(0, regex("a*").match("bbbb", 2, 2).toLong())
        assertEquals(MISMATCH.toLong(), regex("a+").match("aaab", 0, 0).toLong())
        assertEquals(MISMATCH.toLong(), regex("a*").match("aaab", 3, 2).toLong())
    }

    @Test fun testRegexpPattern() {
        assertEquals(3, REGEXP_PATTERN.match("/a/", 0, 3).toLong())
        assertEquals(7, REGEXP_PATTERN.match("/ab\\c./", 0, 7).toLong())
        assertEquals(MISMATCH.toLong(), REGEXP_PATTERN.match("/ab\\/", 0, 5).toLong())
        assertEquals(MISMATCH.toLong(), REGEXP_PATTERN.match("A", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), REGEXP_PATTERN.match("/a/", 0, 2).toLong())
        assertEquals(MISMATCH.toLong(), REGEXP_PATTERN.match("/a/", 0, 1).toLong())
        assertEquals(MISMATCH.toLong(), REGEXP_PATTERN.match("/a/", 0, 0).toLong())
    }

    @Test fun testRegexpModifiers() {
        assertEquals(2, REGEXP_MODIFIERS.match("ab", 0, 2).toLong())
        assertEquals(1, REGEXP_MODIFIERS.match("ab", 0, 1).toLong())
        assertEquals(0, REGEXP_MODIFIERS.match("ab", 0, 0).toLong())
    }

    @Test fun testToString() {
        assertEquals("[a-zA-Z]", isChar(CharPredicates.IS_ALPHA).toString())
        assertEquals(".{3,}", hasAtLeast(3).toString())
        assertEquals("(foo & .{2})", and(string("foo"), hasExact(2)).toString())
        assertEquals("(foo | .{2,})", or(string("foo"), hasAtLeast(2)).toString())
        assertEquals("(bar & c{3})", and(string("bar"), isChar('c').times(3)).toString())
        assertEquals("c{3}", repeat(3, CharPredicates.isChar('c')).toString())
        assertEquals("foo{2,}", string("foo").atLeast(2).toString())
        assertEquals("foo+", string("foo").many1().toString())
        assertEquals("!(foo)", notString("foo").toString())
        assertEquals("a+", many1(CharPredicates.isChar('a')).toString())
        assertEquals("foo*", string("foo").many().toString())
        assertEquals("a*", many(CharPredicates.isChar('a')).toString())
        assertEquals("foo?", string("foo").optional().toString())
        assertEquals("foobar", string("foo").next(string("bar")).toString())
        assertEquals("foo{0,2}", string("foo").atMost(2).toString())
        assertEquals("!(FOO)", not(stringCaseInsensitive("foo")).toString())
        assertEquals("(?:foo)", string("foo").peek().toString())
    }
}