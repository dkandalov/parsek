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
package org.jparsec.error

import org.jparsec.internal.annotations.Private
import java.util.*

/**
 * Reports parser errors in human-readable format.
 *
 * @author Ben Yu
 */
internal object ErrorReporter {
    @JvmStatic fun toString(details: ParseErrorDetails?, location: Location?): String {
        val buf = StringBuilder()
        if (location != null) {
            buf.append("line " + location.line + ", column " + location.column)
        }
        if (details != null) {
            buf.append(":\n")
            if (details.failureMessage != null) {
                buf.append(details.failureMessage)
            } else if (details.expected.isNotEmpty()) {
                reportList(buf, details.expected)
                buf.append(" expected, ")
                buf.append(details.encountered).append(" encountered.")
            } else if (details.unexpected != null) {
                buf.append("unexpected ").append(details.unexpected).append('.')
            }
        }
        return buf.toString()
    }

    @Private
    fun reportList(builder: StringBuilder, messages: List<String?>) {
        if (messages.isEmpty()) return
        val set = LinkedHashSet(messages)
        val size = set.size
        var i = 0
        for (message in set) {
            if (i++ > 0) {
                if (i == size) { // last one
                    builder.append(" or ")
                } else {
                    builder.append(", ")
                }
            }
            builder.append(message)
        }
    }
}