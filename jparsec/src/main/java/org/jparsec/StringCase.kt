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

import java.util.*
import java.util.function.Function

internal enum class StringCase: Comparator<String> {
    CASE_SENSITIVE {
        override fun compare(a: String, b: String) = a.compareTo(b)
        override fun toKey(k: String) = k
    },
    CASE_INSENSITIVE {
        override fun compare(a: String, b: String) = a.compareTo(b, ignoreCase = true)
        override fun toKey(k: String) = k.toLowerCase(Locale.ENGLISH)
    };

    abstract fun toKey(k: String): String

    fun <T> byKey(function: Function<String, T>): Function<String, T> {
        return Function { k: String -> function.apply(toKey(k)) }
    }
}