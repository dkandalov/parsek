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

import org.jparsec.error.Location
import org.jparsec.internal.util.IntList
import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [SourceLocator].
 *
 * @author Ben Yu
 */
class DefaultSourceLocatorTest {
    @Test fun testLocate_onlyOneLineBreakCharacter() {
        val locator = SourceLocator("\n")
        val location = locator.locate(0)
        Assert.assertEquals(Location(1, 1), location)
        Assert.assertEquals(location, locator.locate(0))
        Assert.assertEquals(Location(2, 1), locator.locate(1))
    }

    @Test fun testLocate_emptySource() {
        val locator = SourceLocator("")
        val location = locator.locate(0)
        Assert.assertEquals(Location(1, 1), location)
        Assert.assertEquals(location, locator.locate(0))
    }

    @Test fun testBinarySearch_firstElementIsEqual() {
        Assert.assertEquals(0, SourceLocator.binarySearch(intList(1, 2, 3), 1).toLong())
    }

    @Test fun testBinarySearch_firstElementIsBigger() {
        Assert.assertEquals(0, SourceLocator.binarySearch(intList(1, 2, 3), 0).toLong())
    }

    @Test fun testBinarySearch_secondElementIsEqual() {
        Assert.assertEquals(1, SourceLocator.binarySearch(intList(1, 2, 3), 2).toLong())
    }

    @Test fun testBinarySearch_secondElementIsBigger() {
        Assert.assertEquals(1, SourceLocator.binarySearch(intList(1, 3, 5), 2).toLong())
    }

    @Test fun testBinarySearch_lastElementIsEqual() {
        Assert.assertEquals(2, SourceLocator.binarySearch(intList(1, 3, 5), 5).toLong())
    }

    @Test fun testBinarySearch_lastElementIsBigger() {
        Assert.assertEquals(2, SourceLocator.binarySearch(intList(1, 3, 5), 4).toLong())
    }

    @Test fun testBinarySearch_allSmaller() {
        Assert.assertEquals(3, SourceLocator.binarySearch(intList(1, 3, 5), 10).toLong())
    }

    @Test fun testBinarySearch_oneEqualElement() {
        Assert.assertEquals(0, SourceLocator.binarySearch(intList(1), 1).toLong())
    }

    @Test fun testBinarySearch_oneBiggerElement() {
        Assert.assertEquals(0, SourceLocator.binarySearch(intList(2), 1).toLong())
    }

    @Test fun testBinarySearch_oneSmallerElement() {
        Assert.assertEquals(1, SourceLocator.binarySearch(intList(0), 1).toLong())
    }

    @Test fun testBinarySearch_noElement() {
        Assert.assertEquals(0, SourceLocator.binarySearch(intList(), 1).toLong())
    }

    @Test fun testLookup_noLineBreaksScanned() {
        val locator = SourceLocator("whatever", 2, 3)
        Assert.assertEquals(Location(2, 4), locator.lookup(1))
    }

    @Test fun testLookup_inFirstLine() {
        val locator = SourceLocator("whatever", 2, 3)
        addLineBreaks(locator, 3, 5, 7)
        Assert.assertEquals(Location(2, 4), locator.lookup(1))
    }

    @Test fun testLookup_firstLineBreak() {
        val locator = SourceLocator("whatever", 2, 3)
        addLineBreaks(locator, 3, 5, 7)
        Assert.assertEquals(Location(2, 6), locator.lookup(3))
    }

    @Test fun testLookup_firstCharInSecondLine() {
        val locator = SourceLocator("whatever", 2, 3)
        addLineBreaks(locator, 3, 5, 7)
        Assert.assertEquals(Location(3, 1), locator.lookup(4))
    }

    @Test fun testLookup_lastCharInSecondLine() {
        val locator = SourceLocator("whatever", 2, 3)
        addLineBreaks(locator, 3, 5, 7)
        Assert.assertEquals(Location(3, 2), locator.lookup(5))
    }

    @Test fun testLookup_firstCharInThirdLine() {
        val locator = SourceLocator("whatever", 2, 3)
        addLineBreaks(locator, 3, 5, 7)
        Assert.assertEquals(Location(4, 1), locator.lookup(6))
    }

    @Test fun testLookup_lastCharInThirdLine() {
        val locator = SourceLocator("whatever", 2, 3)
        addLineBreaks(locator, 3, 5, 7)
        Assert.assertEquals(Location(4, 2), locator.lookup(7))
    }

    @Test fun testLookup_firstCharInLastLine() {
        val locator = SourceLocator("whatever", 2, 3)
        addLineBreaks(locator, 3, 5, 7)
        Assert.assertEquals(Location(5, 1), locator.lookup(8))
    }

    @Test fun testLookup_secondCharInLastLine() {
        val locator = SourceLocator("whatever", 2, 3)
        addLineBreaks(locator, 3, 5, 7)
        Assert.assertEquals(Location(5, 2), locator.lookup(9))
    }

    @Test fun testScanTo_indexOutOfBounds() {
        val locator = SourceLocator("whatever", 2, 3)
        try {
            locator.scanTo(100)
            Assert.fail()
        } catch (e: StringIndexOutOfBoundsException) {
        }
    }

    @Test fun testScanTo_indexOnEof() {
        val locator = SourceLocator("foo", 2, 3)
        Assert.assertEquals(Location(2, 6), locator.scanTo(3))
        Assert.assertEquals(3, locator.nextIndex.toLong())
        Assert.assertEquals(3, locator.nextColumnIndex.toLong())
    }

    @Test fun testScanTo_spansLines() {
        val locator = SourceLocator("foo\nbar\n", 2, 3)
        Assert.assertEquals(Location(3, 1), locator.scanTo(4))
        Assert.assertEquals(5, locator.nextIndex.toLong())
        Assert.assertEquals(1, locator.nextColumnIndex.toLong())
    }

    @Test fun testScanTo_lastCharOfLine() {
        val locator = SourceLocator("foo\nbar\n", 2, 3)
        Assert.assertEquals(Location(3, 4), locator.scanTo(7))
        Assert.assertEquals(8, locator.nextIndex.toLong())
        Assert.assertEquals(0, locator.nextColumnIndex.toLong())
    }

    @Test fun testLocate() {
        val locator = SourceLocator("foo\nbar\n", 2, 3)
        Assert.assertEquals(Location(3, 4), locator.locate(7))
        Assert.assertEquals(Location(2, 5), locator.locate(2)) // this will call lookup()
    }

    companion object {
        private fun addLineBreaks(locator: SourceLocator, vararg indices: Int) {
            for (i in indices) {
                locator.lineBreakIndices.add(i)
            }
        }

        private fun intList(vararg ints: Int): IntList {
            val intList = IntList()
            for (i in ints) {
                intList.add(i)
            }
            return intList
        }
    }
}