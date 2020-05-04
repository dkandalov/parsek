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
package org.jparsec.examples.calculator

import org.jparsec.OperatorTable
import org.jparsec.Parser
import org.jparsec.Parser.Companion.newReference
import org.jparsec.Scanners
import org.jparsec.Scanners.isChar
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.UnaryOperator

/**
 * The main calculator parser.
 *
 * @author Ben Yu
 */
object Calculator {
    /** Parsers `source` and evaluates to an [Integer].  */
    @JvmStatic fun evaluate(source: String?): Int {
        return parser().parse(source)
    }

    val NUMBER = Scanners.INTEGER.map(Function { s: String? -> Integer.valueOf(s) })
    val PLUS = BinaryOperator { a: Int, b: Int -> a + b }
    val MINUS = BinaryOperator { a: Int, b: Int -> a - b }
    val MUL = BinaryOperator { a: Int, b: Int -> a * b }
    val DIV = BinaryOperator { a: Int, b: Int -> a / b }
    val MOD = BinaryOperator { a: Int, b: Int -> a % b }
    val NEG = UnaryOperator { a: Int? -> -a!! }
    private fun <T> op(ch: Char, value: T): Parser<T> {
        return isChar(ch).retn(value)
    }

    fun parser(): Parser<Int> {
        val ref = newReference<Int>()
        val term = ref.lazy().between(isChar('('), isChar(')')).or(NUMBER)
        val parser = OperatorTable<Int>()
            .prefix(op<UnaryOperator<Int>?>('-', NEG), 100)
            .infixl(op<BinaryOperator<Int>?>('+', PLUS), 10)
            .infixl(op<BinaryOperator<Int>?>('-', MINUS), 10)
            .infixl(op<BinaryOperator<Int>?>('*', MUL), 20)
            .infixl(op<BinaryOperator<Int>?>('/', DIV), 20)
            .infixl(op<BinaryOperator<Int>?>('%', MOD), 20)
            .build(term)
        ref.set(parser)
        return parser
    }
}