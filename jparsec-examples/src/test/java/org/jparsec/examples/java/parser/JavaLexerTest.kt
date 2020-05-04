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
import org.jparsec.error.ParserException
import org.jparsec.examples.java.ast.expression.DecimalPointNumberLiteral
import org.jparsec.examples.java.ast.expression.IntegerLiteral
import org.jparsec.examples.java.ast.expression.IntegerLiteral.Radix
import org.jparsec.examples.java.ast.expression.NumberType
import org.jparsec.examples.java.ast.expression.ScientificNumberLiteral
import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [JavaLexer].
 *
 * @author Ben Yu
 */
class JavaLexerTest {
    @Test fun testIdentifier() {
        val scanner = JavaLexer.IDENTIFIER
        Assert.assertEquals("foo", scanner.parse("foo"))
        Assert.assertEquals("foo_123_", scanner.parse("foo_123_"))
    }

    @Test fun testDecimalPointScanner() {
        val scanner = JavaLexer.DECIMAL_POINT_SCANNER
        scanner.parse("0.1")
        scanner.parse("1.23")
        scanner.parse(".12")
        assertFailure(scanner, "1", 1, 1)
    }

    @Test fun testDecimalPointNumber() {
        val scanner = JavaLexer.DECIMAL_POINT_NUMBER
        Assert.assertEquals(decimal("1.0", NumberType.DOUBLE), scanner.parse("1.0"))
        Assert.assertEquals(decimal("1.0", NumberType.FLOAT), scanner.parse("1.0f"))
        Assert.assertEquals(decimal("1.23", NumberType.FLOAT), scanner.parse("1.23F"))
        Assert.assertEquals(decimal(".0", NumberType.DOUBLE), scanner.parse(".0D"))
        Assert.assertEquals(decimal("10.0", NumberType.DOUBLE), scanner.parse("10.0d"))
    }

    @Test fun testScientificNumberLiteral() {
        val scanner = JavaLexer.SCIENTIFIC_NUMBER_LITERAL
        Assert.assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2"))
        Assert.assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2d"))
        Assert.assertEquals(scientific("1e2", NumberType.DOUBLE), scanner.parse("1e2D"))
        Assert.assertEquals(scientific("1e2", NumberType.FLOAT), scanner.parse("1e2f"))
        Assert.assertEquals(scientific("1e2", NumberType.FLOAT), scanner.parse("1e2F"))
    }

    @Test fun testInteger() {
        val scanner = JavaLexer.INTEGER
        Assert.assertEquals(integer(Radix.DEC, "123", NumberType.INT), scanner.parse("123"))
        Assert.assertEquals(integer(Radix.DEC, "10", NumberType.LONG), scanner.parse("10L"))
        Assert.assertEquals(integer(Radix.DEC, "10", NumberType.LONG), scanner.parse("10l"))
        Assert.assertEquals(integer(Radix.DEC, "1", NumberType.FLOAT), scanner.parse("1F"))
        Assert.assertEquals(integer(Radix.DEC, "1", NumberType.FLOAT), scanner.parse("1f"))
        Assert.assertEquals(integer(Radix.DEC, "1", NumberType.DOUBLE), scanner.parse("1d"))
        Assert.assertEquals(integer(Radix.DEC, "1", NumberType.DOUBLE), scanner.parse("1D"))
        Assert.assertEquals(integer(Radix.OCT, "1", NumberType.FLOAT), scanner.parse("01f"))
    }

    @Test fun testZero() {
        val scanner = JavaLexer.INTEGER
        Assert.assertEquals(integer(Radix.HEX, "0D", NumberType.INT), scanner.parse("0X0D"))
        Assert.assertEquals(integer(Radix.HEX, "0D", NumberType.LONG), scanner.parse("0X0DL"))
        Assert.assertEquals(integer(Radix.DEC, "0", NumberType.INT), scanner.parse("0"))
        Assert.assertEquals(integer(Radix.DEC, "0", NumberType.DOUBLE), scanner.parse("0d"))
        Assert.assertEquals(integer(Radix.OCT, "0", NumberType.INT), scanner.parse("00"))
    }

    companion object {
        private fun decimal(number: String, type: NumberType): DecimalPointNumberLiteral {
            return DecimalPointNumberLiteral(number, type)
        }

        private fun integer(radix: Radix, number: String, type: NumberType): IntegerLiteral {
            return IntegerLiteral(radix, number, type)
        }

        private fun scientific(number: String, type: NumberType): ScientificNumberLiteral {
            return ScientificNumberLiteral(number, type)
        }

        private fun assertFailure(parser: Parser<*>, source: String, line: Int, column: Int) {
            try {
                parser.parse(source)
                Assert.fail()
            } catch (e: ParserException) {
                Assert.assertEquals(e.message, line.toLong(), e.location.line.toLong())
                Assert.assertEquals(e.message, column.toLong(), e.location.column.toLong())
            }
        }
    }
}