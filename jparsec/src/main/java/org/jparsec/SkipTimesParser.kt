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

internal class SkipTimesParser(private val parser: Parser<*>, private val min: Int, private val max: Int): Parser<Unit>() {
    override fun apply(ctxt: ParseContext): Boolean {
        if (!ctxt.repeat(parser, min)) return false
        if (repeatAtMost(max - min, ctxt)) {
            ctxt.result = null
            return true
        }
        return false
    }

    override fun toString(): String {
        return "skipTimes"
    }

    private fun repeatAtMost(times: Int, ctxt: ParseContext): Boolean {
        for (i in 0 until times) {
            val physical = ctxt.at
            val logical = ctxt.step
            if (!parser.apply(ctxt)) {
                ctxt.setAt(logical, physical)
                return true
            }
        }
        return true
    }
}