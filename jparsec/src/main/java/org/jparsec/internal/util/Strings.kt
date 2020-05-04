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
 * Internal utility for [String] operation.
 *
 * @author Ben Yu
 */
object Strings {
    /** Joins `objects` with `delim` as the delimiter.  */
    @JvmStatic fun join(delim: String?, objects: Array<Any?>): String {
        // Do not use varargs to prevent some silly compiler warnings.
        return if (objects.isEmpty()) "" else join(StringBuilder(), delim, objects).toString()
    }

    /** Joins `objects` with `delim` as the delimiter.  */
    @JvmStatic fun join(builder: StringBuilder, delim: String?, objects: Array<Any?>): StringBuilder {
        return join(builder, delim, Arrays.asList(*objects))
    }

    /** Joins `objects` with `delim` as the delimiter.  */
    @JvmStatic fun join(builder: StringBuilder, delim: String?, objects: Iterable<*>): StringBuilder {
        for ((i, obj) in objects.withIndex()) {
            if (i > 0) builder.append(delim)
            builder.append(obj)
        }
        return builder
    }
}