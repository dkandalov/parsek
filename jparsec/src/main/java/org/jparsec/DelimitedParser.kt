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
 * Parses a list of pattern started with a delimiter, separated and optionally
 * ended by the delimiter.
 *
 * @author Ben Yu
 */
internal class DelimitedParser<T>(
    private val parser: Parser<T>,
    private val delim: Parser<*>,
    private val listFactory: ListFactory<T>
): Parser<List<T>>() {
    override fun apply(ctxt: ParseContext): Boolean {
        val result: MutableList<T> = listFactory.newList()
        while (true) {
            val step0 = ctxt.step
            val at0 = ctxt.at
            var r = ctxt.applyAsDelimiter(delim)
            if (!r) {
                ctxt.result = result
                ctxt.setAt(step0, at0)
                return true
            }
            val step1 = ctxt.step
            val at1 = ctxt.at
            r = parser.apply(ctxt)
            if (!r) {
                ctxt.result = result
                ctxt.setAt(step1, at1)
                return true
            }
            if (at0 == ctxt.at) { // infinite loop
                ctxt.result = result
                return true
            }
            result.add(parser.getReturn(ctxt))
        }
    }

    override fun toString(): String {
        return "delimited"
    }

}