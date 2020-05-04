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
package org.jparsec.internal.util

import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [IntList].
 *
 * @author Ben Yu
 */
class IntListTest {
    @Test fun testCalcSize() {
        Assert.assertEquals(4, IntList.calcSize(4, 1).toLong())
        Assert.assertEquals(3, IntList.calcSize(1, 3).toLong())
        Assert.assertEquals(3, IntList.calcSize(2, 3).toLong())
        Assert.assertEquals(3, IntList.calcSize(3, 3).toLong())
        Assert.assertEquals(6, IntList.calcSize(4, 3).toLong())
    }

    @Test fun testConstructor() {
        val intList = IntList()
        Assert.assertEquals(0, intList.size().toLong())
    }

    @Test fun testConstructor_withCapacity() {
        val intList = IntList(1)
        Assert.assertEquals(0, intList.size().toLong())
    }

    @Test fun testToArray() {
        val intList = IntList()
        assertEqualArray(intList.toArray())
        Assert.assertSame(intList, intList.add(1))
        Assert.assertSame(intList, intList.add(2))
        assertEqualArray(intList.toArray(), 1, 2)
    }

    @Test fun testGet() {
        val intList = IntList()
        Assert.assertEquals(0, intList.size().toLong())
        intList.add(1)
        Assert.assertEquals(1, intList[0].toLong())
        Assert.assertEquals(1, intList.size().toLong())
        assertEqualArray(intList.toArray(), 1)
    }

    @Test fun testGet_throwsForNegativeIndex() {
        val intList = IntList()
        try {
            intList[-1]
            Assert.fail()
        } catch (e: ArrayIndexOutOfBoundsException) {
        }
    }

    @Test fun testGet_throwsForIndexOutOfBounds() {
        val intList = IntList()
        try {
            intList[0]
            Assert.fail()
        } catch (e: ArrayIndexOutOfBoundsException) {
        }
    }

    @Test fun testSet() {
        val intList = IntList(0)
        intList.add(1)
        intList[0] = 2
        Assert.assertEquals(2, intList[0].toLong())
        assertEqualArray(intList.toArray(), 2)
    }

    @Test fun testEnsureCapacity() {
        val intList = IntList(0)
        intList.add(1)
        intList.ensureCapacity(100)
        Assert.assertEquals(1, intList.size().toLong())
        assertEqualArray(intList.toArray(), 1)
    }

    @Test fun testSet_throwsForNegativeIndex() {
        val intList = IntList()
        try {
            intList[-1] = 0
            Assert.fail()
        } catch (e: ArrayIndexOutOfBoundsException) {
        }
    }

    @Test fun testSet_throwsForIndexOutOfBounds() {
        val intList = IntList()
        try {
            intList[0] = 0
            Assert.fail()
        } catch (e: ArrayIndexOutOfBoundsException) {
        }
    }

    companion object {
        private fun assertEqualArray(array: IntArray, vararg values: Int) {
            Assert.assertEquals(array.size.toLong(), values.size.toLong())
            for (i in array.indices) {
                Assert.assertEquals(values[i], array[i])
            }
        }
    }
}