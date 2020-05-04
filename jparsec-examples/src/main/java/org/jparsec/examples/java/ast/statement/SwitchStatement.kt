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
package org.jparsec.examples.java.ast.statement

import org.jparsec.examples.common.ValueObject
import org.jparsec.examples.java.ast.expression.Expression
import org.jparsec.functors.Pair

/**
 * Represents the "switch case" expression.
 *
 * @author Ben Yu
 */
class SwitchStatement(
    val condition: Expression, val cases: List<Pair<Expression?, Statement?>>, val defaultCase: Statement?): ValueObject(), Statement {
    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("switch (").append(condition).append(") {")
        for (c in cases) {
            builder.append(" case ").append(c.a).append(":")
            if (c.b != null) {
                builder.append(" ").append(c.b)
            }
        }
        if (defaultCase != null) {
            builder.append(" default: ").append(defaultCase)
        }
        builder.append("}")
        return builder.toString()
    }

}