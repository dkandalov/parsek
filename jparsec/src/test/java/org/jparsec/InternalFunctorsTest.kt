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

import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [InternalFunctors].
 *
 * @author Ben Yu
 */
class InternalFunctorsTest {
    @Test fun testTokenWithSameValue() {
        val i = 10
        val fromToken = InternalFunctors.tokenWithSameValue(i)
        Assert.assertEquals("10", fromToken.toString())
        Assert.assertNull(fromToken.map(Token(1, 1, "foo")))
        Assert.assertNull(fromToken.map(Token(1, 1, 2)))
        Assert.assertNull(fromToken.map(Token(1, 1, null)))
        val token = Token(1, 1, i)
        Assert.assertSame(token, fromToken.map(token))
    }

    @Test fun testFirstOfTwo() {
        val map = InternalFunctors.firstOfTwo<String, Int>()
        Assert.assertEquals("followedBy", map.toString())
        Assert.assertEquals("one", map.apply("one", 2))
    }

    @Test fun testLastOfTwo() {
        val map = InternalFunctors.lastOfTwo<Int, String>()
        Assert.assertEquals("sequence", map.toString())
        Assert.assertEquals("two", map.apply(1, "two"))
    }

    @Test fun testLastOfThree() {
        val map = InternalFunctors.lastOfThree<Int, String, String>()
        Assert.assertEquals("sequence", map.toString())
        Assert.assertEquals("three", map.map(1, "two", "three"))
    }

    @Test fun testLastOfFour() {
        val map = InternalFunctors.lastOfFour<Int, String, String, String>()
        Assert.assertEquals("sequence", map.toString())
        Assert.assertEquals("four", map.map(1, "two", "three", "four"))
    }

    @Test fun testLastOfFive() {
        val map = InternalFunctors.lastOfFive<Int, String, String, String, String>()
        Assert.assertEquals("sequence", map.toString())
        Assert.assertEquals("five", map.map(1, "two", "three", "four", "five"))
    }
}