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

/**
 * A parser that always returns an empty mutable list.
 *
 * @author Ben Yu
 */
internal class EmptyListParser<T> private constructor(): Parser<List<T>>() {
    public override fun apply(ctxt: ParseContext): Boolean {
        ctxt.result = ArrayList<T>(0)
        return true
    }

    override fun toString() = "[]"

    companion object {
        private val INSTANCE: Parser<List<Any>> = EmptyListParser()

        @JvmStatic fun <T> instance(): Parser<List<T>> {
            return INSTANCE as Parser<List<T>>
        }
    }
}