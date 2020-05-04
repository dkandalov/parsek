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

internal class BestParser<T>(private val parsers: Array<Parser<out T>>, private val order: IntOrder): Parser<T>() {
    override fun apply(ctxt: ParseContext): Boolean {
        val result = ctxt.result
        val step = ctxt.step
        val at = ctxt.at
        val latestChild = ctxt.trace.latestChild
        for (i in parsers.indices) {
            val parser = parsers[i]
            if (parser.apply(ctxt)) {
                applyForBestFit(i + 1, ctxt, result, step, at, latestChild)
                return true
            }
            // in alternate, we do not care partial match.
            ctxt[step, at] = result
        }
        return false
    }

    override fun toString(): String {
        return order.toString()
    }

    private fun applyForBestFit(
        from: Int, ctxt: ParseContext,
        originalResult: Any?, originalStep: Int, originalAt: Int, originalLatestChild: TreeNode?) {
        var bestAt = ctxt.at
        var bestStep = ctxt.step
        var bestResult = ctxt.result
        var bestChild = ctxt.trace.latestChild
        for (i in from until parsers.size) {
            ctxt[originalStep, originalAt] = originalResult
            ctxt.trace.latestChild = originalLatestChild
            val parser: Parser<*> = parsers[i]
            val ok = parser.apply(ctxt)
            if (!ok) continue
            val at2 = ctxt.at
            if (order.compare(at2, bestAt)) {
                bestAt = at2
                bestStep = ctxt.step
                bestResult = ctxt.result
                bestChild = ctxt.trace.latestChild
            }
        }
        ctxt[bestStep, bestAt] = bestResult
        ctxt.trace.latestChild = bestChild
    }

}