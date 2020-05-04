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
import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.IntList

/**
 * Locates the line and column number of a 0-based index in the source.
 *
 *
 *  This class internally keeps a cache of the indices of all the line break characters scanned
 * so far, therefore repeated location lookup can be done in amortized log(n) time.
 *
 *
 *  It is <EM>not</EM> multi-thread safe.
 *
 * @author Ben Yu
 */
class SourceLocator
/**
 * Creates a [SourceLocator] object.
 *
 * @param source the source.
 * @param lineNumber the starting line number.
 * @param columnNumber the starting column number.
 */ @Private constructor(private val source: CharSequence,
                         /** The first line number.  */
                         private val startLineNumber: Int,
                         /** The first column number.  */
                         private val startColumnNumber: Int) {

    /** The 0-based indices of the line break characters scanned so far.  */
    @Private
    val lineBreakIndices = IntList(20)

    /** The 0-based index of the next character to be scanned.  */
    @Private
    var nextIndex = 0

    /** The 0-based index of the column of the next character to be scanned.  */
    @Private
    var nextColumnIndex = 0

    /**
     * Creates a [SourceLocator] object.
     *
     * @param source the source.
     */
    constructor(source: CharSequence): this(source, 1, 1) {}

    fun locate(index: Int): Location {
        return if (index < nextIndex) lookup(index) else scanTo(index)
    }

    /**
     * Looks up the location identified by `ind` using the cached indices of line break
     * characters. This assumes that all line-break characters before `ind` are already scanned.
     */
    @Private fun lookup(index: Int): Location {
        val size = lineBreakIndices.size()
        if (size == 0) return location(0, index)
        val lineNumber = binarySearch(lineBreakIndices, index)
        if (lineNumber == 0) return location(0, index)
        val previousBreak = lineBreakIndices[lineNumber - 1]
        return location(lineNumber, index - previousBreak - 1)
    }

    /**
     * Scans from `nextIndex` to `ind` and saves all indices of line break characters
     * into `lineBreakIndices` and adjusts the current column number as it goes. The location of
     * the character on `ind` is returned.
     *
     *
     *  After this method returns, `nextIndex` and `nextColumnIndex` will point to the
     * next character to be scanned or the EOF if the end of input is encountered.
     */
    @Private fun scanTo(index: Int): Location {
        var index = index
        var eof = false
        if (index == source.length) { // The eof has index size() + 1
            eof = true
            index--
        }
        var columnIndex = nextColumnIndex
        for (i in nextIndex..index) {
            val c = source[i]
            if (c == LINE_BREAK) {
                lineBreakIndices.add(i)
                columnIndex = 0
            } else columnIndex++
        }
        nextIndex = index + 1
        nextColumnIndex = columnIndex
        val lines = lineBreakIndices.size()
        if (eof) return location(lines, columnIndex)
        return if (columnIndex == 0) getLineBreakLocation(lines - 1) else location(lines, columnIndex - 1)
    }

    /**
     * Gets the 0-based column number of the line break character for line identified by
     * `lineIndex`.
     */
    private fun getLineBreakColumnIndex(lineIndex: Int): Int {
        val lineBreakIndex = lineBreakIndices[lineIndex]
        return if (lineIndex == 0) lineBreakIndex else lineBreakIndex - lineBreakIndices[lineIndex - 1] - 1
    }

    private fun getLineBreakLocation(lineIndex: Int): Location {
        return location(lineIndex, getLineBreakColumnIndex(lineIndex))
    }

    private fun location(l: Int, c: Int): Location {
        return Location(startLineNumber + l, (if (l == 0) startColumnNumber else 1) + c)
    }

    companion object {
        /** The line break character.  */
        private const val LINE_BREAK = '\n'

        /**
         * Uses binary search to look up the index of the first element in `ascendingInts` that's
         * greater than or equal to `value`. If all elements are smaller than `value`,
         * `ascendingInts.size()` is returned.
         */
        @Private fun binarySearch(ascendingInts: IntList, value: Int): Int {
            var begin = 0
            var to = ascendingInts.size()
            while (true) {
                if (begin == to) return begin
                val i = (begin + to) / 2
                val x = ascendingInts[i]
                if (x == value) return i else if (x > value) to = i else begin = i + 1
            }
        }
    }

}