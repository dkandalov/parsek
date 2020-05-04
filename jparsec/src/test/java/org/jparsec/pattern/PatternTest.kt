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

import org.jparsec.pattern.Patterns.ALWAYS
import org.jparsec.pattern.Patterns.INTEGER
import org.jparsec.pattern.Patterns.NEVER
import org.jparsec.pattern.Patterns.hasAtLeast
import org.jparsec.pattern.Patterns.isChar
import org.jparsec.pattern.Patterns.string
import org.junit.Assert.*
import org.junit.Test

class PatternTest {
    @Test fun testMismatch() {
        assertTrue(Pattern.MISMATCH < 0)
    }

    @Test fun testNext() {
        assertEquals(2, hasAtLeast(1).next(hasAtLeast(1)).match("abc", 0, 3).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), ALWAYS.next(NEVER).match("abc", 0, 3).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), NEVER.next(ALWAYS).match("abc", 0, 3).toLong())
    }

    @Test fun testNot() {
        assertEquals(0, NEVER.not().match("abc", 0, 3).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), ALWAYS.not().match("abc", 0, 3).toLong())
    }

    @Test fun testOr() {
        assertEquals(1, hasAtLeast(1).or(hasAtLeast(2)).match("abc", 0, 2).toLong())
        assertEquals(1, hasAtLeast(1).or(hasAtLeast(2)).match("abc", 0, 1).toLong())
        assertEquals(0, NEVER.or(ALWAYS).match("abc", 0, 0).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), NEVER.or(NEVER).match("", 0, 0).toLong())
    }

    @Test fun testOptional() {
        assertEquals(0, NEVER.optional().match("", 0, 0).toLong())
        assertEquals(0, ALWAYS.optional().match("", 0, 0).toLong())
        assertEquals(1, hasAtLeast(1).optional().match("abc", 0, 3).toLong())
    }

    @Test fun testPeek() {
        assertEquals(0, hasAtLeast(1).peek().match("abc", 0, 3).toLong())
        assertEquals(0, ALWAYS.peek().match("abc", 0, 3).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), NEVER.peek().match("abc", 0, 3).toLong())
    }

    @Test fun testRepeat() {
        assertEquals(0, ALWAYS.times(2).match("abc", 0, 3).toLong())
        assertEquals(2, hasAtLeast(1).times(2).match("abc", 0, 3).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), hasAtLeast(1).times(2).match("abc", 0, 1).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), NEVER.times(2).match("abc", 0, 3).toLong())
        assertEquals(0, hasAtLeast(1).times(0).match("abc", 0, 3).toLong())
    }

    @Test fun testRepeat_throwsForNegativeNumber() {
        try {
            ALWAYS.times(-1)
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("n < 0", e.message)
        }
    }

    @Test fun testIfElse() {
        assertEquals(3, isChar('a').ifelse(string("bc"), INTEGER)
            .match("abcd", 0, 4).toLong())
        assertEquals(Pattern.MISMATCH.toLong(),
                     isChar('a').ifelse(string("bd"), INTEGER)
                         .match("abcd", 0, 4).toLong())
        assertEquals(2, isChar('a').ifelse(string("bc"), INTEGER)
            .match("12c", 0, 3).toLong())
        assertEquals(Pattern.MISMATCH.toLong(),
                     isChar('a').ifelse(string("bc"), INTEGER)
                         .match("xxx", 0, 3).toLong())
    }

    @Test fun testMany() {
        assertEquals(0, NEVER.many().match("abc", 0, 3).toLong())
        assertEquals(0, ALWAYS.many().match("abc", 0, 3).toLong())
        assertEquals(3, hasAtLeast(1).many().match("abc", 0, 3).toLong())
        assertEquals(4, hasAtLeast(2).many().match("abcde", 0, 5).toLong())
    }

    @Test fun testMany1() {
        assertEquals(Pattern.MISMATCH.toLong(), NEVER.many1().match("abc", 0, 3).toLong())
        assertEquals(0, ALWAYS.many1().match("abc", 0, 3).toLong())
        assertEquals(3, hasAtLeast(1).many1().match("abc", 0, 3).toLong())
        assertEquals(4, hasAtLeast(2).many1().match("abcde", 0, 5).toLong())
    }

    @Test fun testMany_withMin() {
        assertEquals(0, ALWAYS.atLeast(2).match("abc", 0, 3).toLong())
        assertEquals(0, ALWAYS.atLeast(0).match("abc", 0, 3).toLong())
        assertEquals(0, NEVER.atLeast(0).match("abc", 0, 3).toLong())
        assertEquals(3, hasAtLeast(1).atLeast(2).match("abc", 0, 3).toLong())
        assertEquals(4, hasAtLeast(2).atLeast(2).match("abcde", 0, 5).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), NEVER.atLeast(2).match("abc", 0, 3).toLong())
        assertEquals(Pattern.MISMATCH.toLong(), hasAtLeast(1).atLeast(2).match("abc", 0, 1).toLong())
    }

    @Test fun testMany_throwsForNegativeMin() {
        try {
            ALWAYS.atLeast(-1)
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("min < 0", e.message)
        }
    }

    @Test fun testSome() {
        assertEquals(0, NEVER.atMost(2).match("abc", 0, 3).toLong())
        assertEquals(0, ALWAYS.atMost(2).match("abc", 0, 3).toLong())
        assertEquals(0, NEVER.atMost(0).match("abc", 0, 3).toLong())
        assertEquals(0, ALWAYS.atMost(0).match("abc", 0, 3).toLong())
        assertEquals(2, hasAtLeast(1).atMost(2).match("abc", 0, 3).toLong())
        assertEquals(4, hasAtLeast(2).atMost(2).match("abcde", 0, 5).toLong())
    }

    @Test fun testSome_withMin() {
        assertEquals(Pattern.MISMATCH.toLong(), NEVER.times(1, 2).match("abc", 0, 3).toLong())
        assertEquals(0, ALWAYS.times(1, 2).match("abc", 0, 3).toLong())
        assertEquals(0, NEVER.times(0, 1).match("abc", 0, 3).toLong())
        assertEquals(2, hasAtLeast(1).times(1, 2).match("abc", 0, 3).toLong())
        assertEquals(4, hasAtLeast(2).times(1, 2).match("abcde", 0, 5).toLong())
    }

    @Test fun testSome_throwsForNegativeMax() {
        try {
            ALWAYS.atMost(-1)
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("max < 0", e.message)
        }
    }

    @Test fun testSome_throwsForNegativeMinMax() {
        try {
            ALWAYS.times(-1, 1)
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("min < 0", e.message)
        }
        try {
            ALWAYS.times(1, -1)
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("max < 0", e.message)
        }
    }
}