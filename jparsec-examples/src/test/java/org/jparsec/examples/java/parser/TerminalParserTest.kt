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
package org.jparsec.examples.java.parser

import org.jparsec.Parser
import org.jparsec.Token
import org.jparsec.Tokens.reserved
import org.jparsec.error.ParserException
import org.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral
import org.jparsec.examples.java.ast.expression.IntegerLiteral
import org.jparsec.examples.java.ast.expression.IntegerLiteral.Radix
import org.jparsec.examples.java.ast.expression.NumberType
import org.jparsec.examples.java.ast.expression.ScientificNumberLiteral
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit test for [TerminalParser].
 *
 * @author Ben Yu
 */
class TerminalParserTest {
    @Test fun testParse() {
        assertParser(TerminalParser.term("."),
                     "  . /** javadoc */ /* regular doc */ \n // line comment",
                     Token(2, 1, reserved(".")))
    }

    @Test fun testAdjacent() {
        assertOperator(TerminalParser.adjacent(""), "")
        assertOperator(TerminalParser.adjacent("<"), "<")
        assertOperator(TerminalParser.adjacent(">>"), ">>")
        assertOperator(TerminalParser.adjacent(">>>"), ">>>")
        assertOperator(TerminalParser.adjacent("<<"), "<<")
        assertOperator(TerminalParser.adjacent("<+>"), "<+>")
        assertFailure(TerminalParser.adjacent(">>"), "> >", 1, 4)
        assertFailure(TerminalParser.adjacent(">>"), ">+", 1, 2)
        assertParser(TerminalParser.adjacent(">>").optional(), ">+", null, ">+")
        assertOperator(TerminalParser.adjacent(">>").or(TerminalParser.adjacent(">+")), ">+")
    }

    @Test fun testTerm() {
        assertOperator(TerminalParser.term("<<"), "<<")
        assertOperator(TerminalParser.term(">>"), ">>")
        assertOperator(TerminalParser.term(">>>"), ">>>")
        assertOperator(TerminalParser.term("||"), "||")
        assertOperator(TerminalParser.term(">"), ">")
        TerminalParser.parse(TerminalParser.term(">>").followedBy(TerminalParser.term(">")), ">> >")
        assertFailure(TerminalParser.term(">>").followedBy(TerminalParser.term(">")), ">>>", 1, 1)
        try {
            TerminalParser.term("not exist")
            Assert.fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test fun testLexer() {
        val parser = TerminalParser.TOKENIZER
        assertEquals(ScientificNumberLiteral("1e2", NumberType.DOUBLE), parser.parse("1e2"))
        assertEquals(ScientificNumberLiteral("1e2", NumberType.FLOAT), parser.parse("1e2f"))
        assertEquals("foo", parser.parse("\"foo\""))
        assertEquals('a', parser.parse("'a'"))
        assertEquals(reserved("import"), parser.parse("import"))
        assertEquals(DecimalPointNumberLiteral("1.2", NumberType.DOUBLE), parser.parse("1.2"))
        assertEquals(IntegerLiteral(Radix.DEC, "1", NumberType.INT), parser.parse("1"))
        assertEquals(IntegerLiteral(Radix.HEX, "1", NumberType.LONG), parser.parse("0X1L"))
        assertEquals(IntegerLiteral(Radix.OCT, "1", NumberType.DOUBLE), parser.parse("01D"))
    }

    companion object {
        fun assertParser(parser: Parser<*>?, source: String?, value: Any?) {
            assertEquals(value, TerminalParser.parse(parser, source))
        }

        fun assertParser(parser: Parser<*>?, source: String, value: Any?, rest: String) {
            Assert.assertTrue(source.endsWith(rest))
            assertEquals(value,
                                TerminalParser.parse(parser, source.substring(0, source.length - rest.length)))
        }

        fun assertOperator(parser: Parser<*>?, source: String) {
            val actual = TerminalParser.parse(parser, source) as Token
            assertEquals(0, actual.index().toLong())
            assertEquals(source.length.toLong(), actual.length().toLong())
            // TODO: do we make adjacent() call Tokens.reserved()?
            // That seems verbose unless we make Tokenizers public.
            assertEquals(source, actual.value().toString())
        }

        @JvmStatic fun <T> assertResult(
            parser: Parser<T>?, source: String?, expectedType: Class<out T>, expectedResult: String?) {
            assertToString(expectedType, expectedResult, TerminalParser.parse(parser, source))
        }

        @JvmStatic fun <T> assertToString(
            expectedType: Class<out T>, expectedResult: String?, result: T) {
            Assert.assertTrue(expectedType.isInstance(result))
            assertEquals(expectedResult, result.toString())
        }

        @JvmStatic @JvmOverloads fun assertFailure(
            parser: Parser<*>?, source: String?, line: Int, column: Int, errorMessage: String? = "") {
            try {
                TerminalParser.parse(parser, source)
                Assert.fail()
            } catch (e: ParserException) {
                Assert.assertTrue(e.message, e.message!!.contains(errorMessage!!))
                assertEquals(line.toLong(), e.location.line.toLong())
                assertEquals(column.toLong(), e.location.column.toLong())
            }
        }
    }
}