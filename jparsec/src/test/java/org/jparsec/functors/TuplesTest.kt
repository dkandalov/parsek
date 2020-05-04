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
package org.jparsec.functors

import org.jparsec.util.ObjectTester
import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [Tuples].
 *
 * @author Ben Yu
 */
class TuplesTest {
    @Test fun testPair() {
        val pair = Tuples.pair("one", 1)
        Assert.assertEquals("(one, 1)", pair.toString())
        Assert.assertEquals("one", pair.a)
        Assert.assertEquals(1, pair.b.toInt().toLong())
        ObjectTester.assertEqual(pair, pair, Tuples.pair("one", 1))
        ObjectTester.assertNotEqual(pair, Tuples.pair("one", 2), Tuples.pair("two", 1), "abc")
    }

    @Test fun testTuple2() {
        val pair = Tuples.tuple("one", 1)
        Assert.assertEquals("(one, 1)", pair.toString())
        Assert.assertEquals("one", pair.a)
        Assert.assertEquals(1, pair.b.toInt().toLong())
        ObjectTester.assertEqual(pair, pair, Tuples.pair("one", 1))
        ObjectTester.assertNotEqual(pair, Tuples.pair("one", 2), Tuples.pair("two", 1), "abc")
    }

    @Test fun testTuple3() {
        val tuple = Tuples.tuple("12", 1, 2)
        Assert.assertEquals("(12, 1, 2)", tuple.toString())
        Assert.assertEquals("12", tuple.a)
        Assert.assertEquals(1, tuple.b.toInt().toLong())
        Assert.assertEquals(2, tuple.c.toInt().toLong())
        ObjectTester.assertEqual(tuple, tuple, Tuples.tuple("12", 1, 2))
        ObjectTester.assertNotEqual(tuple,
                                    Tuples.tuple("21", 1, 2), Tuples.tuple("12", 2, 2), Tuples.tuple("12", 1, 1), "abc")
    }

    @Test fun testTuple4() {
        val tuple = Tuples.tuple("123", 1, 2, 3)
        Assert.assertEquals("(123, 1, 2, 3)", tuple.toString())
        Assert.assertEquals("123", tuple.a)
        Assert.assertEquals(1, tuple.b.toInt().toLong())
        Assert.assertEquals(2, tuple.c.toInt().toLong())
        Assert.assertEquals(3, tuple.d.toInt().toLong())
        ObjectTester.assertEqual(tuple, tuple, Tuples.tuple("123", 1, 2, 3))
        ObjectTester.assertNotEqual(tuple,
                                    Tuples.tuple("21", 1, 2, 3), Tuples.tuple("123", 2, 2, 3),
                                    Tuples.tuple("123", 1, 1, 3), Tuples.tuple("123", 1, 2, 2), "abc")
    }

    @Test fun testTuple5() {
        val tuple = Tuples.tuple("1234", 1, 2, 3, 4)
        Assert.assertEquals("(1234, 1, 2, 3, 4)", tuple.toString())
        Assert.assertEquals("1234", tuple.a)
        Assert.assertEquals(1, tuple.b.toInt().toLong())
        Assert.assertEquals(2, tuple.c.toInt().toLong())
        Assert.assertEquals(3, tuple.d.toInt().toLong())
        Assert.assertEquals(4, tuple.e.toInt().toLong())
        ObjectTester.assertEqual(tuple, tuple, Tuples.tuple("1234", 1, 2, 3, 4))
        ObjectTester.assertNotEqual(tuple,
                                    Tuples.tuple("21", 1, 2, 3, 4), Tuples.tuple("1234", 2, 2, 3, 4),
                                    Tuples.tuple("1234", 1, 1, 3, 4), Tuples.tuple("1234", 1, 2, 2, 4),
                                    Tuples.tuple("1234", 1, 2, 3, 3), "abc")
    }
}