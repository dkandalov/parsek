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
 * Parses any nestable comment pattern.
 *
 * @author Ben Yu
 */
internal class NestableBlockCommentScanner(private val openQuote: Parser<*>, private val closeQuote: Parser<*>, private val commented: Parser<*>): Parser<Unit>() {
    public override fun apply(ctxt: ParseContext): Boolean {
        if (!openQuote.apply(ctxt)) return false
        var level = 1
        while (level > 0) {
            val step = ctxt.step
            val at = ctxt.at
            if (closeQuote.apply(ctxt)) {
                check(at != ctxt.at) { "closing comment scanner not consuming input." }
                level--
                continue
            }
            if (openQuote.apply(ctxt)) {
                check(at != ctxt.at) { "opening comment scanner not consuming input." }
                level++
                continue
            }
            if (!ctxt.stillThere(at, step)) return false
            if (commented.apply(ctxt)) {
                check(at != ctxt.at) { "commented scanner not consuming input." }
                continue
            }
            return false
        }
        ctxt.result = null
        return true
    }

    override fun toString(): String {
        return "nestable block comment"
    }

}