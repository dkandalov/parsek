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

import org.jparsec.internal.util.Strings.join
import java.util.*

/**
 * Represents the syntactical structure of the input being parsed.
 *
 * @since 2.3
 */
class ParseTree internal constructor(
    val name: String,
    val beginIndex: Int,
    val endIndex: Int,
    val value: Any?,
    children: List<ParseTree?>?) {
    /** Returns the node name, which is specified in [Parser.label].  */
    /** Returns the index in source where this node starts.  */
    /** Returns the index in source where this node ends.  */
    /** Returns the parsed value of this node, or `null` if it's a failed node.  */
    /**
     * Returns the immutable list of child nodes that correspond to [labeled][Parser.label]
     * parsers syntactically enclosed inside parent parser.
     */
    val children: List<ParseTree?>

    override fun toString(): String {
        val builder = StringBuilder(name).append(": ")
        if (children.isEmpty()) {
            builder.append(value)
        } else {
            builder.append("{\n")
            join(builder, ",\n", children)
            builder.append("\n}")
        }
        return builder.toString()
    }

    init {
        this.children = Collections.unmodifiableList(children)
    }
}