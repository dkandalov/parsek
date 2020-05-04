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

import org.jparsec.Parsers.or
import org.jparsec.Tokens.reserved
import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.Lists.arrayList
import java.util.*
import java.util.function.Function
import kotlin.collections.ArrayList

/**
 * Helper class for creating parsers and lexers for operators.
 *
 * @author Ben Yu
 */
internal object Operators {
    /**
     * Gets a [Lexicon] instance with [Tokens.reserved] as each operator's value
     * and a lexer that strives to try the shortest operator first.
     *
     *
     *  Safely speaking, we can always start from the longest operator and falls back to shorter
     * ones. Yet shorter operators are more often used than longer ones and the scanning of them is
     * faster. However, scanning shorter operators first has the chance that a "==" is mistakenly
     * scanned as "=" followed by another "=". In order to avoid this, we analyze the prefix
     * relationship and make sure that prefixes are scanned after prefixes.
     */
    @JvmStatic fun lexicon(operatorNames: Collection<String>): Lexicon {
        val operators: MutableMap<String, Any> = HashMap()
        val ops = sort(*operatorNames.toTypedArray())
        val lexers: ArrayList<Parser<*>> = ArrayList(ops.size)
        for (i in ops.indices) {
            val s = ops[i]
            val scanner: Parser<*> = if (s.length == 1) Scanners.isChar(s[0]) else Scanners.string(s)
            val value: Any = reserved(s)
            operators[s] = value
            lexers.add(scanner.retn(value))
        }
        return Lexicon(Function { key: String -> operators[key] }, or(lexers))
    }

    private val LONGER_STRING_FIRST = Comparator<String> { a, b -> b.length - a.length }

    /**
     * Sorts `names` into a new array by putting short string first, unless a shorter string is
     * a prefix of a longer string, in which case, the longer string is before the prefix string.
     */
    @Private fun sort(vararg names: String): Array<String> {
        //short name first, unless it is fully contained in a longer name
        val copy: Array<out String> = names.clone()
        Arrays.sort(copy, LONGER_STRING_FIRST)
        val suites = Suites()
        for (name in copy) {
            suites.add(name)
        }
        return suites.toArray()
    }

    /**
     * A suite is a list of overlapping operators, where some operators are prefixes of other
     * operators. If operator foo is a prefix of operator bar, it is listed after bar.
     *
     *
     *  For example ["==", "="]. Empty strings are ignored.
     *
     *
     *  Upon a new string is added, We scan from the end of the list until a string is found
     * to contain it, in which case, the new string is added right after the position.
     *
     *
     *  With the critical requirement that longer strings are added before shorter ones, prefixes
     * are always inserted later than prefixees.
     */
    private class Suite(s: String) {
        //containees are behined containers.
        val list = arrayList<String>()
        init {
            if (s.isNotEmpty()) list.add(s)
        }

        fun add(v: String): Boolean {
            if (v.isEmpty()) return true
            for (i in list.indices.reversed()) {
                val s = list[i]
                if (s.startsWith(v)) {
                    if (s.length == v.length) return true // ignore duplicates
                    list.add(i + 1, v)
                    return true
                }
            }
            return false
        }
    }

    /**
     * A list of suites in the reverse order of the suites. Suite a is defined to be bigger than
     * suite b if the first element of a is longer than that of b.
     */
    private class Suites {
        private val list = arrayList<Suite>()

        /**
         * Scans the list of suites by adding `v` to the first suite that claims it as a prefix.
         * If no suite claims it as prefix, it is added as a standalone [Suite] at the end of the
         * list.
         */
        fun add(v: String) {
            for (suite in list) {
                if (suite.add(v)) return
            }
            list.add(Suite(v))
        }

        /**
         * Collapses the names in each suite by traversing the suites in reverse order, so that smaller
         * suites are collapsed first and generally shorter operators will be placed before longer ones
         * unless it is contained by a longer operator.
         */
        fun toArray(): Array<String> {
            val result = arrayList<String>()
            for (i in list.indices.reversed()) {
                val suite = list[i]
                for (name in suite.list) {
                    result.add(name)
                }
            }
            return result.toTypedArray()
        }
    }
}