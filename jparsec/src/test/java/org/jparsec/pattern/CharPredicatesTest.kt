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
package org.jparsec.pattern

import org.jparsec.pattern.CharPredicates.ALWAYS
import org.jparsec.pattern.CharPredicates.IS_ALPHA
import org.jparsec.pattern.CharPredicates.IS_ALPHA_
import org.jparsec.pattern.CharPredicates.IS_ALPHA_NUMERIC
import org.jparsec.pattern.CharPredicates.IS_ALPHA_NUMERIC_
import org.jparsec.pattern.CharPredicates.IS_DIGIT
import org.jparsec.pattern.CharPredicates.IS_HEX_DIGIT
import org.jparsec.pattern.CharPredicates.IS_LETTER
import org.jparsec.pattern.CharPredicates.IS_LOWER_CASE
import org.jparsec.pattern.CharPredicates.IS_UPPER_CASE
import org.jparsec.pattern.CharPredicates.IS_WHITESPACE
import org.jparsec.pattern.CharPredicates.NEVER
import org.jparsec.pattern.CharPredicates.among
import org.jparsec.pattern.CharPredicates.and
import org.jparsec.pattern.CharPredicates.isChar
import org.jparsec.pattern.CharPredicates.not
import org.jparsec.pattern.CharPredicates.notAmong
import org.jparsec.pattern.CharPredicates.notChar
import org.jparsec.pattern.CharPredicates.notRange
import org.jparsec.pattern.CharPredicates.or
import org.jparsec.pattern.CharPredicates.range
import org.junit.Assert.*
import org.junit.Test

class CharPredicatesTest {
    @Test fun testIsChar() {
        val predicate = isChar('a')
        assertTrue(predicate.isChar('a'))
        assertFalse(predicate.isChar('x'))
        assertEquals("a", predicate.toString())
    }

    @Test fun testNotChar() {
        val predicate = notChar('a')
        assertFalse(predicate.isChar('a'))
        assertTrue(predicate.isChar('x'))
        assertEquals("^a", predicate.toString())
    }

    @Test fun testRange() {
        val predicate = range('1', '3')
        assertTrue(predicate.isChar('1'))
        assertTrue(predicate.isChar('2'))
        assertTrue(predicate.isChar('3'))
        assertFalse(predicate.isChar('0'))
        assertFalse(predicate.isChar('4'))
        assertEquals("[1-3]", predicate.toString())
    }

    @Test fun testIsDigit() {
        val predicate = IS_DIGIT
        assertTrue(predicate.isChar('0'))
        assertTrue(predicate.isChar('9'))
        assertFalse(predicate.isChar('a'))
        assertFalse(predicate.isChar(' '))
        assertEquals("[0-9]", predicate.toString())
    }

    @Test fun testNotRange() {
        val predicate = notRange('1', '3')
        assertFalse(predicate.isChar('1'))
        assertFalse(predicate.isChar('2'))
        assertFalse(predicate.isChar('3'))
        assertTrue(predicate.isChar('0'))
        assertTrue(predicate.isChar('4'))
        assertEquals("[^1-3]", predicate.toString())
    }

    @Test fun testAmong() {
        val predicate = among("a1")
        assertTrue(predicate.isChar('a'))
        assertTrue(predicate.isChar('1'))
        assertFalse(predicate.isChar(' '))
        assertEquals("[a1]", predicate.toString())
    }

    @Test fun testNotAmong() {
        val predicate = notAmong("a1")
        assertFalse(predicate.isChar('a'))
        assertFalse(predicate.isChar('1'))
        assertTrue(predicate.isChar(' '))
        assertEquals("^[a1]", predicate.toString())
    }

    @Test fun testIsHexDigit() {
        val predicate = IS_HEX_DIGIT
        assertFalse(predicate.isChar('g'))
        assertFalse(predicate.isChar(' '))
        assertTrue(predicate.isChar('A'))
        assertTrue(predicate.isChar('a'))
        assertTrue(predicate.isChar('F'))
        assertTrue(predicate.isChar('f'))
        assertTrue(predicate.isChar('0'))
        assertTrue(predicate.isChar('9'))
        assertTrue(predicate.isChar('E'))
        assertTrue(predicate.isChar('1'))
        assertEquals("[0-9a-fA-F]", predicate.toString())
    }

    @Test fun testIsUpperCase() {
        val predicate = IS_UPPER_CASE
        assertFalse(predicate.isChar('a'))
        assertFalse(predicate.isChar('1'))
        assertFalse(predicate.isChar(' '))
        assertTrue(predicate.isChar('A'))
        assertTrue(predicate.isChar('Z'))
        assertEquals("uppercase", predicate.toString())
    }

    @Test fun testIsLowerCase() {
        val predicate = IS_LOWER_CASE
        assertFalse(predicate.isChar('A'))
        assertFalse(predicate.isChar('1'))
        assertFalse(predicate.isChar(' '))
        assertTrue(predicate.isChar('a'))
        assertTrue(predicate.isChar('z'))
        assertEquals("lowercase", predicate.toString())
    }

    @Test fun testIsWhitespace() {
        val predicate = IS_WHITESPACE
        assertFalse(predicate.isChar('A'))
        assertFalse(predicate.isChar('1'))
        assertFalse(predicate.isChar('a'))
        assertTrue(predicate.isChar(' '))
        assertTrue(predicate.isChar('\t'))
        assertTrue(predicate.isChar('\n'))
        assertEquals("whitespace", predicate.toString())
    }

    @Test fun testIsAlpha() {
        val predicate = IS_ALPHA
        assertFalse(predicate.isChar('-'))
        assertFalse(predicate.isChar('1'))
        assertFalse(predicate.isChar('_'))
        assertTrue(predicate.isChar('a'))
        assertTrue(predicate.isChar('Z'))
        assertEquals("[a-zA-Z]", predicate.toString())
    }

    @Test fun testIsAlpha_() {
        val predicate = IS_ALPHA_
        assertFalse(predicate.isChar('-'))
        assertFalse(predicate.isChar('1'))
        assertTrue(predicate.isChar('_'))
        assertTrue(predicate.isChar('a'))
        assertTrue(predicate.isChar('Z'))
        assertEquals("[a-zA-Z_]", predicate.toString())
    }

    @Test fun testIsAlphaNumeric() {
        val predicate = IS_ALPHA_NUMERIC
        assertFalse(predicate.isChar('-'))
        assertFalse(predicate.isChar('_'))
        assertTrue(predicate.isChar('1'))
        assertTrue(predicate.isChar('a'))
        assertTrue(predicate.isChar('Z'))
        assertEquals("[0-9a-zA-Z]", predicate.toString())
    }

    @Test fun testIsAlphaNumeric_() {
        val predicate = IS_ALPHA_NUMERIC_
        assertFalse(predicate.isChar('-'))
        assertTrue(predicate.isChar('1'))
        assertTrue(predicate.isChar('_'))
        assertTrue(predicate.isChar('a'))
        assertTrue(predicate.isChar('Z'))
        assertEquals("[0-9a-zA-Z_]", predicate.toString())
    }

    @Test fun testIsLetter() {
        val predicate = IS_LETTER
        assertFalse(predicate.isChar('-'))
        assertFalse(predicate.isChar('1'))
        assertFalse(predicate.isChar('_'))
        assertTrue(predicate.isChar('a'))
        assertTrue(predicate.isChar('Z'))
        assertEquals("letter", predicate.toString())
    }

    @Test fun testAlways() {
        assertTrue(ALWAYS.isChar('a'))
        assertTrue(ALWAYS.isChar('>'))
        assertTrue(ALWAYS.isChar('0'))
        assertEquals("any character", ALWAYS.toString())
    }

    @Test fun testNever() {
        assertFalse(NEVER.isChar('a'))
        assertFalse(NEVER.isChar('>'))
        assertFalse(NEVER.isChar('0'))
        assertEquals("none", NEVER.toString())
    }

    @Test fun testNot() {
        assertFalse(not(ALWAYS).isChar('a'))
        assertTrue(not(NEVER).isChar('a'))
        assertEquals("^any character", not(ALWAYS).toString())
    }

    @Test fun testAnd() {
        assertSame(ALWAYS, and())
        assertSame(IS_ALPHA, and(IS_ALPHA))
        assertFalse(and(ALWAYS, NEVER).isChar('a'))
        assertFalse(and(NEVER, ALWAYS).isChar('a'))
        assertFalse(and(NEVER, NEVER).isChar('a'))
        assertTrue(and(ALWAYS, ALWAYS).isChar('a'))
        assertFalse(and(ALWAYS, NEVER, ALWAYS).isChar('a'))
        assertFalse(and(NEVER, ALWAYS, ALWAYS).isChar('a'))
        assertFalse(and(NEVER, NEVER, NEVER).isChar('a'))
        assertTrue(and(ALWAYS, ALWAYS, ALWAYS).isChar('a'))
        assertEquals("any character and none", and(ALWAYS, NEVER).toString())
        assertEquals("any character and none and any character", and(ALWAYS, NEVER, ALWAYS).toString())
    }

    @Test fun testOr() {
        assertSame(NEVER, or())
        assertSame(IS_ALPHA, or(IS_ALPHA))
        assertTrue(or(ALWAYS, NEVER).isChar('a'))
        assertTrue(or(NEVER, ALWAYS).isChar('a'))
        assertTrue(or(ALWAYS, ALWAYS).isChar('a'))
        assertFalse(or(NEVER, NEVER).isChar('a'))
        assertTrue(or(ALWAYS, NEVER, NEVER).isChar('a'))
        assertTrue(or(NEVER, NEVER, ALWAYS).isChar('a'))
        assertTrue(or(ALWAYS, ALWAYS, ALWAYS).isChar('a'))
        assertFalse(or(NEVER, NEVER, NEVER).isChar('a'))
        assertEquals("any character or none", or(ALWAYS, NEVER).toString())
        assertEquals("any character or none or any character", or(ALWAYS, NEVER, ALWAYS).toString())
    }
}