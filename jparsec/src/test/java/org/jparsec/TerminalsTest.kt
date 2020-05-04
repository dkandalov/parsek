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

import org.jparsec.Asserts.assertFailure
import org.jparsec.Asserts.assertParser
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Unit test for [Terminals].
 *
 * @author Ben Yu
 */
@RunWith(Parameterized::class)
class TerminalsTest(private val mode: Parser.Mode) {
    @Test fun testSingleQuoteChar() {
        assertEquals('a' as Any, Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER.parse("'a'", mode))
        assertEquals('\'' as Any, Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER.parse("'\\''", mode))
    }

    @Test fun testDoubleQuoteString() {
        assertEquals("a\r\n\t",
                            Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER.parse("\"a\\r\\n\\t\"", mode))
        assertEquals("\"", Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER.parse("\"\\\"\"", mode))
    }

    @Test fun testSingleQuoteString() {
        assertEquals("ab", Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER.parse("'ab'", mode))
        assertEquals("a'b", Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER.parse("'a''b'", mode))
    }

    @Test fun testDecimalLiteralTokenizer() {
        assertEquals(Tokens.decimalLiteral("1"), Terminals.DecimalLiteral.TOKENIZER.parse("1", mode))
        assertEquals(Tokens.decimalLiteral("01"), Terminals.DecimalLiteral.TOKENIZER.parse("01"))
        assertEquals(Tokens.decimalLiteral("09"), Terminals.DecimalLiteral.TOKENIZER.parse("09", mode))
        assertEquals(Tokens.decimalLiteral("12"), Terminals.DecimalLiteral.TOKENIZER.parse("12", mode))
        assertEquals(Tokens.decimalLiteral("12.3"), Terminals.DecimalLiteral.TOKENIZER.parse("12.3", mode))
        assertEquals(Tokens.decimalLiteral("0.1"), Terminals.DecimalLiteral.TOKENIZER.parse("0.1", mode))
        assertEquals(Tokens.decimalLiteral(".2"), Terminals.DecimalLiteral.TOKENIZER.parse(".2", mode))
        assertFailure(mode, Terminals.DecimalLiteral.TOKENIZER, "12x", 1, 3, "EOF expected, x encountered.")
    }

    @Test fun testIntegerLiteralTokenizer() {
        assertEquals(Tokens.fragment("1", Tokens.Tag.INTEGER), Terminals.IntegerLiteral.TOKENIZER.parse("1"))
        assertEquals(Tokens.fragment("12", Tokens.Tag.INTEGER), Terminals.IntegerLiteral.TOKENIZER.parse("12"))
        assertEquals(Tokens.fragment("0", Tokens.Tag.INTEGER), Terminals.IntegerLiteral.TOKENIZER.parse("0"))
        assertEquals(Tokens.fragment("01", Tokens.Tag.INTEGER), Terminals.IntegerLiteral.TOKENIZER.parse("01"))
        assertFailure(mode, Terminals.IntegerLiteral.TOKENIZER, "12x", 1, 3, "EOF expected, x encountered.")
    }

    @Test fun testScientificNumberLiteralTokenizer() {
        assertEquals(Tokens.scientificNotation("1", "2"), Terminals.ScientificNumberLiteral.TOKENIZER.parse("1E2"))
        assertEquals(Tokens.scientificNotation("1", "2"), Terminals.ScientificNumberLiteral.TOKENIZER.parse("1e+2"))
        assertEquals(Tokens.scientificNotation("10", "-2"), Terminals.ScientificNumberLiteral.TOKENIZER.parse("10E-2"))
        assertFailure(mode, Terminals.ScientificNumberLiteral.TOKENIZER,
                              "1e2x", 1, 4, "EOF expected, x encountered.")
    }

    @Test fun testLongLiteralDecTokenizer() {
        assertEquals(1L as Any, Terminals.LongLiteral.DEC_TOKENIZER.parse("1"))
        assertEquals(10L as Any, Terminals.LongLiteral.DEC_TOKENIZER.parse("10"))
        assertFailure(mode, Terminals.LongLiteral.DEC_TOKENIZER, "0", 1, 1)
        assertFailure(mode, Terminals.LongLiteral.DEC_TOKENIZER, "12x", 1, 3, "EOF expected, x encountered.")
    }

    @Test fun testLongLiteralHexTokenizer() {
        assertEquals(0L as Any, Terminals.LongLiteral.HEX_TOKENIZER.parse("0x0"))
        assertEquals(0X10L as Any, Terminals.LongLiteral.HEX_TOKENIZER.parse("0X10"))
        assertEquals(0X1AL as Any, Terminals.LongLiteral.HEX_TOKENIZER.parse("0X1A"))
        assertEquals(0XFFL as Any, Terminals.LongLiteral.HEX_TOKENIZER.parse("0XFf"))
        assertFailure(mode, Terminals.LongLiteral.HEX_TOKENIZER, "0", 1, 1)
        assertFailure(mode, Terminals.LongLiteral.HEX_TOKENIZER, "1", 1, 1)
        assertFailure(mode, Terminals.LongLiteral.HEX_TOKENIZER,
                              "0x12x", 1, 5, "EOF expected, x encountered.")
    }

    @Test fun testTokenizeHexAsLong_throwsIfStringIsTooShort() {
        try {
            NumberLiteralsTranslator.tokenizeHexAsLong("0x")
            Assert.fail()
        } catch (e: IllegalStateException) {
        }
    }

    @Test fun testLongLiteralOctTokenizer() {
        assertEquals(0L as Any, Terminals.LongLiteral.OCT_TOKENIZER.parse("0"))
        assertEquals(15L as Any, Terminals.LongLiteral.OCT_TOKENIZER.parse("017"))
        assertFailure(mode, Terminals.LongLiteral.OCT_TOKENIZER, "1", 1, 1)
        assertFailure(mode, Terminals.LongLiteral.OCT_TOKENIZER, "0X1", 1, 2)
        assertFailure(mode, Terminals.LongLiteral.OCT_TOKENIZER, "08", 1, 2)
        assertFailure(mode, Terminals.LongLiteral.OCT_TOKENIZER, "01x", 1, 3, "EOF expected, x encountered.")
    }

    @Test fun testLongLiteralTokenizer() {
        assertEquals(0L as Any, Terminals.LongLiteral.TOKENIZER.parse("0"))
        assertEquals(8L as Any, Terminals.LongLiteral.TOKENIZER.parse("010"))
        assertEquals(12L as Any, Terminals.LongLiteral.TOKENIZER.parse("12"))
        assertEquals(16L as Any, Terminals.LongLiteral.TOKENIZER.parse("0X10"))
        assertEquals(9L as Any, Terminals.LongLiteral.TOKENIZER.parse("9"))
        assertFailure(mode, Terminals.LongLiteral.TOKENIZER, "1x", 1, 2, "EOF expected, x encountered.")
    }

    @Test fun testIdentifierTokenizer() {
        assertEquals(Tokens.identifier("foo"), Terminals.Identifier.TOKENIZER.parse("foo"))
        assertEquals(Tokens.identifier("foo1"), Terminals.Identifier.TOKENIZER.parse("foo1"))
        assertEquals(Tokens.identifier("FOO"), Terminals.Identifier.TOKENIZER.parse("FOO"))
        assertEquals(Tokens.identifier("FOO_2"), Terminals.Identifier.TOKENIZER.parse("FOO_2"))
        assertEquals(Tokens.identifier("_foo"), Terminals.Identifier.TOKENIZER.parse("_foo"))
        assertFailure(mode, Terminals.Identifier.TOKENIZER, "1foo", 1, 1)
    }

    @Test fun testCharLiteralParser() {
        assertEquals('a' as Any, Terminals.CharLiteral.PARSER.from(Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER, Scanners.WHITESPACES).parse("'a'"))
    }

    @Test fun testLongLiteralParser() {
        assertEquals(1L as Any, Terminals.LongLiteral.PARSER.from(Terminals.LongLiteral.DEC_TOKENIZER, Scanners.WHITESPACES).parse("1"))
    }

    @Test fun testStringLiteralParser() {
        assertEquals("abc", Terminals.StringLiteral.PARSER.from(
            Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER, Scanners.WHITESPACES).parse("'abc'"))
    }

    @Test fun testIdentifierParser() {
        assertEquals("foo", Terminals.Identifier.PARSER.from(Terminals.Identifier.TOKENIZER, Scanners.WHITESPACES).parse("foo"))
    }

    @Test fun testIntegerLiteralParser() {
        assertEquals("123", Terminals.IntegerLiteral.PARSER.from(Terminals.IntegerLiteral.TOKENIZER, Scanners.WHITESPACES).parse("123"))
    }

    @Test fun testDecimalLiteralParser() {
        assertEquals("1.23", Terminals.DecimalLiteral.PARSER.from(Terminals.DecimalLiteral.TOKENIZER, Scanners.WHITESPACES).parse("1.23"))
    }

    @Test fun testFromFragment() {
        assertEquals("", Terminals.fromFragment().toString())
        assertEquals("1", Terminals.fromFragment(1).toString())
        val fromToken = Terminals.fromFragment("foo", 1)
        assertEquals("[foo, 1]", fromToken.toString())
        assertEquals("test", fromToken.map(Token(0, 3, Tokens.fragment("test", "foo"))))
        assertEquals("test", fromToken.map(Token(0, 3, Tokens.fragment("test", 1))))
        Assert.assertNull(fromToken.map(Token(0, 3, Tokens.fragment("test", "bar"))))
        Assert.assertNull(fromToken.map(Token(0, 3, Tokens.fragment("test", 2))))
    }

    @Test fun testToken_noTokenName() {
        val terminals = Terminals.operators("+", "-")
        val parser = terminals.token().from(terminals.tokenizer(), Scanners.WHITESPACES)
        assertFailure(mode, parser, "+", 1, 1)
    }

    @Test fun testToken_oneTokenNameOnly() {
        val terminals = Terminals.operators("+", "-")
        val parser = terminals.token("+").from(terminals.tokenizer(), Scanners.WHITESPACES)
        assertEquals(Token(0, 1, Tokens.reserved("+")), parser.parse("+"))
        assertFailure(mode, parser, "-", 1, 1, "+ expected, - encountered.")
    }

    @Test fun testToken_tokenNamesListed() {
        val terminals = Terminals.operators("+", "-")
        val parser = terminals.token("+", "-").from(terminals.tokenizer(), Scanners.WHITESPACES)
        assertEquals(Token(0, 1, Tokens.reserved("+")), parser.parse("+"))
        assertEquals(Token(0, 1, Tokens.reserved("-")), parser.parse("-"))
        assertFailure(mode, parser, "*", 1, 1, "+ or - expected, * encountered.")
    }

    @Test fun testPhrase() {
        val keywords = arrayOf("hello", "world", "hell")
        val terminals = Terminals.operators().words(Scanners.IDENTIFIER).keywords(listOf(*keywords)).build()
        val parser: Parser<String?> = terminals.phrase("hello", "world").from(terminals.tokenizer(), Scanners.WHITESPACES) as Parser<String?>
        parser.parse("hello   world")
        assertFailure(mode, parser, "hello hell", 1, 7, "world")
        assertFailure(mode, parser, "hell", 1, 1, "hello world")
        assertParser(mode, parser.optional(null), "hello hell", null, "hello hell")
    }

    @Test fun testCaseSensitive() {
        val keywords = arrayOf("foo", "bar", "baz")
        val terminals = Terminals.operators("+", "-").words(Scanners.IDENTIFIER).keywords(listOf(*keywords)).build()
        val parser = terminals.token("+", "-", "foo", "bar").from(terminals.tokenizer(), Scanners.WHITESPACES)
        assertEquals(Token(0, 1, Tokens.reserved("+")), parser.parse("+"))
        assertEquals(Token(0, 1, Tokens.reserved("-")), parser.parse("-"))
        assertEquals(Token(0, 3, Tokens.reserved("foo")), parser.parse("foo"))
        assertEquals(Token(0, 3, Tokens.reserved("bar")), parser.parse("bar"))
        assertFailure(mode, parser, "baz", 1, 1, "+, -, foo or bar expected, baz encountered.")
        assertFailure(mode, parser, "Foo", 1, 1, "+, -, foo or bar expected, Foo encountered.")
        assertFailure(mode, parser, "123", 1, 1, "+, -, foo or bar expected, 1 encountered.")
        assertEquals("FOO", Terminals.Identifier.PARSER.from(terminals.tokenizer(), Scanners.WHITESPACES).parse("FOO"))
    }

    @Test fun testCaseInsensitive() {
        val keywords = arrayOf("foo", "bar", "baz")
        val terminals = Terminals.operators("+", "-").words(Scanners.IDENTIFIER).caseInsensitiveKeywords(listOf(*keywords)).build()
        val parser = terminals.token("+", "-", "foo", "bar").from(terminals.tokenizer(), Scanners.WHITESPACES)
        assertEquals(Token(0, 1, Tokens.reserved("+")), parser.parse("+"))
        assertEquals(Token(0, 1, Tokens.reserved("-")), parser.parse("-"))
        assertEquals(Token(0, 3, Tokens.reserved("foo")), parser.parse("foo"))
        assertEquals(Token(0, 3, Tokens.reserved("foo")), parser.parse("Foo"))
        assertEquals(Token(0, 3, Tokens.reserved("bar")), parser.parse("bar"))
        assertFailure(mode, parser, "baz", 1, 1, "+, -, foo or bar expected, baz encountered.")
        assertFailure(mode, parser, "123", 1, 1, "+, -, foo or bar expected, 1 encountered.")
        assertEquals("xxx", Terminals.Identifier.PARSER.from(terminals.tokenizer(), Scanners.WHITESPACES).parse("xxx"))
    }

    @Test fun testCaseSensitive_withScanner() {
        val terminals = Terminals
            .operators("+", "-")
            .words(Scanners.INTEGER)
            .keywords("12", "34")
            .build()
        val parser = terminals.token("+", "-", "12", "34").from(terminals.tokenizer(), Scanners.WHITESPACES)
        assertEquals(Token(0, 1, Tokens.reserved("+")), parser.parse("+"))
        assertEquals(Token(0, 1, Tokens.reserved("-")), parser.parse("-"))
        assertEquals(Token(0, 2, Tokens.reserved("12")), parser.parse("12"))
        assertEquals(Token(0, 2, Tokens.reserved("34")), parser.parse("34"))
        assertFailure(mode, parser, "foo", 1, 1, "+, -, 12 or 34 expected, f encountered.")
        assertFailure(mode, parser, "123", 1, 1, "+, -, 12 or 34 expected, 123 encountered.")
        assertEquals("123", Terminals.Identifier.PARSER.from(terminals.tokenizer(), Scanners.WHITESPACES).parse("123"))
    }

    @Test fun testCaseInsensitive_withScanner() {
        val terminals = Terminals
            .operators("+", "-")
            .words(Scanners.INTEGER)
            .caseInsensitiveKeywords("12", "34")
            .build()
        val parser = terminals.token("+", "-", "12", "34").from(terminals.tokenizer(), Scanners.WHITESPACES)
        assertEquals(Token(0, 1, Tokens.reserved("+")), parser.parse("+"))
        assertEquals(Token(0, 1, Tokens.reserved("-")), parser.parse("-"))
        assertEquals(Token(0, 2, Tokens.reserved("12")), parser.parse("12"))
        assertEquals(Token(0, 2, Tokens.reserved("34")), parser.parse("34"))
        assertFailure(mode, parser, "foo", 1, 1, "+, -, 12 or 34 expected, f encountered.")
        assertFailure(mode, parser, "123", 1, 1, "+, -, 12 or 34 expected, 123 encountered.")
        assertEquals("123", Terminals.Identifier.PARSER.from(terminals.tokenizer(), Scanners.WHITESPACES).parse("123"))
    }

    @Test fun testCheckDup() {
        Terminals.checkDup(listOf("a", "b"), listOf("+", "-"))
        Terminals.checkDup(listOf("a", "b"), listOf("A", "B"))
        assertDup(listOf("a", "b"), listOf("x", "b"))
    }

    companion object {
        @JvmStatic @Parameterized.Parameters fun data(): Collection<Array<Any>> {
            return listOf(arrayOf(Parser.Mode.PRODUCTION), arrayOf(Parser.Mode.DEBUG))
        }

        private fun assertDup(a: Iterable<String>, b: Iterable<String>) {
            try {
                Terminals.checkDup(a, b)
                Assert.fail()
            } catch (e: IllegalArgumentException) {
            }
        }
    }

}