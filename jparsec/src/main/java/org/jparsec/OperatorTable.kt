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
import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.Lists.arrayList
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Builds [Parser] to parse expressions with operator-precedence grammar. The operators
 * and precedences are declared in this table.
 *
 *
 * Operators have precedences. The higher the precedence number, the higher the precedence. For
 * the same precedence, prefix &gt; postfix &gt; left-associative &gt; non-associative &gt; right-associative.
 *
 *
 * For example:
 * `Unary<Integer> negate = new Unary<Integer>() {... return -n; };
 * Binary<Integer> plus = new Binary<Integer>() {... return a + b; };
 * Binary<Integer> minus = new Binary<Integer>() {... return a - b; };
 * ...
 * Terminals terms = Terminals.operators("+", "-", "*", "/");
 * Parser<Integer> calculator = new OperatorTable()
 * .prefix(terms.token("-").retn(negate), 100)
 * .infixl(terms.token("+").retn(plus), 10)
 * .infixl(terms.token("-").retn(minus), 10)
 * .infixl(terms.token("*").retn(multiply), 20)
 * .infixl(terms.token("/").retn(divide), 20)
 * .build(Terminals.IntegerLiteral.PARSER.map(stringToInteger));
 * Parser<Integer> parser = calculator.from(
 * terms.tokenizer().or(Terminals.IntegerLiteral.TOKENIZER), Scanners.WHITESPACES.optional());
 * return parser.parse(text);
` *
 *
 * @author Ben Yu
 */
class OperatorTable<T> {
    /** Describes operator associativity, in order of precedence.  */
    internal enum class Associativity {
        PREFIX, POSTFIX, LASSOC, NASSOC, RASSOC
    }

    private val ops: MutableList<Operator> = arrayList()

    internal class Operator(val op: Parser<*>, val precedence: Int, val associativity: Associativity): Comparable<Operator> {

        /** Higher precedence first. For tie, compares associativity.  */
        override fun compareTo(that: Operator): Int {
            if (precedence > that.precedence) return -1
            return if (precedence < that.precedence) 1 else associativity.compareTo(that.associativity)
        }

    }

    /**
     * Adds a prefix unary operator.
     *
     * @param parser the parser for the operator.
     * @param precedence the precedence number.
     * @return this.
     */
    fun prefix(
        parser: Parser<out Function<in T, out T>?>, precedence: Int): OperatorTable<T> {
        ops.add(Operator(parser, precedence, Associativity.PREFIX))
        return this
    }

    /**
     * Adds a postfix unary operator.
     *
     * @param parser the parser for the operator.
     * @param precedence the precedence number.
     * @return this.
     */
    fun postfix(
        parser: Parser<out Function<in T, out T>?>, precedence: Int): OperatorTable<T> {
        ops.add(Operator(parser, precedence, Associativity.POSTFIX))
        return this
    }

    /**
     * Adds an infix left-associative binary operator.
     *
     * @param parser the parser for the operator.
     * @param precedence the precedence number.
     * @return this.
     */
    fun infixl(parser: Parser<out BiFunction<in T, in T, out T>?>, precedence: Int): OperatorTable<T> {
        ops.add(Operator(parser, precedence, Associativity.LASSOC))
        return this
    }

    /**
     * Adds an infix right-associative binary operator.
     *
     * @param parser the parser for the operator.
     * @param precedence the precedence number.
     * @return this.
     */
    fun infixr(parser: Parser<out BiFunction<in T, in T, out T>?>, precedence: Int): OperatorTable<T> {
        ops.add(Operator(parser, precedence, Associativity.RASSOC))
        return this
    }

    /**
     * Adds an infix non-associative binary operator.
     *
     * @param parser the parser for the operator.
     * @param precedence the precedence number.
     * @return this.
     */
    fun infixn(parser: Parser<out BiFunction<in T, in T, out T>?>, precedence: Int): OperatorTable<T> {
        ops.add(Operator(parser, precedence, Associativity.NASSOC))
        return this
    }

    /**
     * Builds a [Parser] based on information in this [OperatorTable].
     *
     * @param operand parser for the operands.
     * @return the expression parser.
     */
    fun build(operand: Parser<out T>): Parser<T> {
        return buildExpressionParser(operand, *operators())
    }

    @Private internal fun operators(): Array<Operator> {
        ops.sort()
        return ops.toTypedArray()
    }

    companion object {
        /**
         * Builds a [Parser] based on information described by [OperatorTable].
         *
         * @param term parser for the terminals.
         * @param ops the operators.
         * @return the expression parser.
         */
        internal fun <T> buildExpressionParser(term: Parser<out T>, vararg ops: Operator): Parser<T> {
            if (ops.isEmpty()) return term.cast()
            var begin = 0
            var precedence = ops[0].precedence
            var associativity = ops[0].associativity
            var end = 0
            var ret: Parser<T> = term.cast()
            for (i in 1 until ops.size) {
                val op = ops[i]
                end = i
                if (op.precedence == precedence && op.associativity == associativity) {
                    continue
                }
                end = i
                val p = slice(ops, begin, end)
                ret = build(p, associativity, ret)
                begin = i
                precedence = ops[i].precedence
                associativity = ops[i].associativity
            }
            if (end != ops.size) {
                end = ops.size
                associativity = ops[begin].associativity
                val p = slice(ops, begin, end)
                ret = build(p, associativity, ret)
            }
            return ret
        }

        private fun slice(ops: Array<out Operator>, begin: Int, end: Int): Parser<*> {
            return or(ops.slice(IntRange(begin, end - 1)).map { it.op })
        }

        private fun <T> build(op: Parser<*>, associativity: Associativity, operand: Parser<T>): Parser<T> {
            return when (associativity) {
                Associativity.PREFIX  -> operand.prefix(op as Parser<Function<T, T>>)
                Associativity.POSTFIX -> operand.postfix(op as Parser<Function<T, T>>)
                Associativity.LASSOC  -> operand.infixl(op as Parser<BiFunction<T, T, T>>)
                Associativity.RASSOC  -> operand.infixr(op as Parser<BiFunction<T, T, T>>)
                Associativity.NASSOC  -> operand.infixn(op as Parser<BiFunction<T, T, T>>)
            }
        }
    }
}