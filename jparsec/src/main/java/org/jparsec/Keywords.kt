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

import org.jparsec.Tokens.reserved
import org.jparsec.internal.annotations.Private
import java.util.*
import java.util.function.Function

/**
 * Helper class for creating lexers and parsers for keywords.
 *
 * @author Ben Yu
 */
internal object Keywords {
    @Private
    fun unique(c: Comparator<String>?, vararg names: String?): Array<String> {
        val set = TreeSet(c)
        set.addAll(listOf(*names))
        return set.toTypedArray()
    }

    @JvmStatic fun lexicon(
        wordScanner: Parser<String>, keywordNames: Collection<String>,
        stringCase: StringCase, defaultMap: Function<String, *>
    ): Lexicon {
        val map = HashMap<String, Any>()
        for (n in unique(stringCase, *keywordNames.toTypedArray())) {
            val value: Any = reserved(n)
            map[stringCase.toKey(n)] = value
        }
        val keywordMap = stringCase.byKey { key: String -> map[key] }
        return Lexicon(keywordMap, wordScanner.map(Lexicon.fallback(keywordMap, defaultMap)))
    }
}