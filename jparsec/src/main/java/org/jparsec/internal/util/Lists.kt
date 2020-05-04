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
package org.jparsec.internal.util

import java.util.*

/**
 * Internal utility to work with [java.util.List].
 *
 * @author Ben Yu
 */
object Lists {
    @JvmStatic fun <T> arrayList(): ArrayList<T> {
        return ArrayList()
    }

    /** Returns a new [ArrayList] with enough capacity to hold `expectedElements`.  */
    @JvmStatic fun <T> arrayList(expectedElements: Int): ArrayList<T> {
        return ArrayList(capacity(expectedElements))
    }

    private fun capacity(expectedElements: Int): Int {
        return Math.min(5L + expectedElements + expectedElements / 10, Int.MAX_VALUE.toLong()).toInt()
    }
}