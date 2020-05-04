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

import org.jparsec.TestParsers.areChars
import org.jparsec.internal.util.Lists
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

/**
 * Unit test for error handling of [Parser].
 *
 * @author benyu
 */
@RunWith(Parameterized::class)
class ParserErrorHandlingTest(private val mode: Parser.Mode) {
    @Test fun testNotOverridesNever() {
        assertError(
            Scanners.isChar('a').next(Scanners.isChar('x').not()), Scanners.isChar('a').next(Parsers.never<Any>()),
            "ax", 1, 2, "unexpected x.")
    }

    @Test fun testExpectOverridesNot() {
        assertError(
            areChars("ab"), Scanners.isChar('a').next(Scanners.isChar('x').not()),
            "ax", 1, 2, "b expected, x encountered.")
    }

    @Test fun testFailureOverridesExpect() {
        assertError(
            areChars("ab"), Scanners.isChar('a').next(Parsers.fail<Any>("foo")),
            "ax", 1, 2, "foo")
    }

    @Test fun testFailureOverridesExplicitExpect() {
        assertError(
            Scanners.isChar('a').next(Parsers.fail<Any>("bar")), Scanners.isChar('a').next(Parsers.expect<Any>("foo")),
            "ax", 1, 2, "bar")
    }

    @Test fun testMoreRelevantErrorWins() {
        assertError(areChars("abc"), Scanners.isChar('a').next(Parsers.expect<Any>("foo")), "ab",
                    1, 3, "c expected, EOF encountered.")
    }

    @Test fun testFirstNeverWins() {
        assertError(Parsers.never<Any>(), Parsers.never<Any>(), "x", 1, 1, "")
    }

    @Test fun testFirstNotWins() {
        Asserts.assertFailure(mode,
                              Parsers.or(Scanners.isChar('x').not("xxx"), Scanners.isChar('x').not("X")), "x", 1, 1, "unexpected xxx.")
    }

    @Test fun testFirstFailureWins() {
        Asserts.assertFailure(mode,
                              Parsers.or(Parsers.fail("foo"), Parsers.fail<Any>("bar")), "x", 1, 1, "foo")
    }

    @Test fun testExpectMerged() {
        Asserts.assertFailure(mode,
                              Parsers.or(Parsers.expect("foo"), Parsers.expect<Any>("bar")), "x",
                              1, 1, "foo or bar expected, x encountered.")
        Asserts.assertFailure(mode,
                              Parsers.or(Parsers.expect<Any>("foo").label("foo"), Parsers.expect("foo")), "x",
                              1, 1, "foo expected, x encountered.")
    }

    @Test fun testExpectedMerged() {
        Asserts.assertFailure(mode,
                              Parsers.or(Scanners.isChar('a'), Scanners.isChar('b')), "x",
                              1, 1, "a or b expected, x encountered.")
    }

    @Test fun testErrorSurvivesOr() {
        assertError(
            Parsers.or(areChars("abc"), Scanners.isChar('a')).next(Scanners.isChar('x')), areChars("ax"),
            "abx", 1, 3, "c expected, x encountered.")
    }

    @Test fun testErrorSurvivesLonger() {
        assertError(
            Parsers.longer(areChars("abc"), Scanners.isChar('a')).next(Scanners.isChar('x')), areChars("ax"),
            "abx", 1, 3, "c expected, x encountered.")
    }

    @Test fun testErrorSurvivesShorter() {
        assertError(
            Parsers.shorter(areChars("abc"), Scanners.isChar('a')).next(Scanners.isChar('x')), areChars("ax"),
            "abx", 1, 3, "c expected, x encountered.")
    }

    @Test fun testErrorSurvivesRepetition() {
        assertError(
            areChars("abc").many(), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").skipMany(), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").many1(), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").skipMany1(), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").times(0, 1), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").skipTimes(0, 1), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").times(0, 2), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").skipTimes(0, 2), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").times(1), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
        assertError(
            areChars("abc").skipTimes(1), areChars("ax"), "abx", 1, 3, "c expected, x encountered.")
    }

    @Test fun testOuterExpectWins() {
        Asserts.assertFailure(mode, Parsers.expect<Any>("foo").label("bar"), "", 1, 1, "bar expected, EOF encountered.")
    }

    @Test fun testTokenLevelError() {
        val terminals = Terminals
            .operators("+", "-")
            .words(Scanners.IDENTIFIER)
            .keywords("foo", "bar", "baz")
            .build()
        val lexer = terminals.tokenizer()
        val foobar = terminals.token("foo", "bar").times(2).from(lexer, Scanners.WHITESPACES)
        Asserts.assertFailure(mode, foobar, "foo+", 1, 4, "foo or bar expected, + encountered.")
        Asserts.assertFailure(mode, foobar, "foo", 1, 4, "foo or bar expected, EOF encountered.")
        Asserts.assertFailure(mode, Parsers.or(areChars("foo bar"), foobar), "foo baz",
                              1, 5, "foo or bar expected, baz encountered.")
        Asserts.assertFailure(mode, Parsers.or(foobar, areChars("foo bar")), "foo baz",
                              1, 7, "r expected, z encountered.")
        Asserts.assertFailure(mode, Parsers.or(areChars("foox"), foobar), "foo baz",
                              1, 5, "foo or bar expected, baz encountered.")
        val foobar2 = terminals.token("foo", "bar").times(2).from(lexer.next(lexer), Scanners.WHITESPACES)
        assertError(foobar2, areChars("foox"), "+foo -baz",
                    1, 6, "foo or bar expected, baz encountered.")
    }

    @Test fun testEmptyTokenCounts() {
        val keywords = arrayOf("foo", "bar", "baz")
        val terminals = Terminals.operators("+", "-").words(Scanners.IDENTIFIER).keywords(Arrays.asList(*keywords)).build()
        val lexeme = terminals.tokenizer().lexer(Scanners.WHITESPACES)
            .map<List<Token>> { tokens: List<Token> ->
                val result: MutableList<Token> = Lists.arrayList()
                for (token in tokens) {
                    result.add(Token(token.index(), 0, "("))
                    result.add(token)
                    val index = token.index() + token.length()
                    result.add(Token(index, 0, ")"))
                }
                result
            }
        val LPAREN = Parsers.token(InternalFunctors.tokenWithSameValue("("))
        val RPAREN = Parsers.token(InternalFunctors.tokenWithSameValue(")"))
        val parser: Parser<*> = Parsers.or(
            Parsers.sequence(LPAREN, terminals.token("foo"), terminals.token("bar")),
            Parsers.sequence(LPAREN, terminals.token("foo"), RPAREN, terminals.token("bar")))
        Asserts.assertFailure(mode, parser.from(lexeme), "foo baz", 1, 5, "bar expected, ( encountered.")
    }

    private fun assertError(
        a: Parser<*>, b: Parser<*>, source: String, line: Int, column: Int, message: String) {
        Asserts.assertFailure(mode,
                              Parsers.or(a, b), source, line, column, message)
        Asserts.assertFailure(mode,
                              Parsers.or(b, a), source, line, column, message)
        Asserts.assertFailure(mode,
                              Parsers.longer(a, b), source, line, column, message)
        Asserts.assertFailure(mode,
                              Parsers.longer(b, a), source, line, column, message)
        Asserts.assertFailure(mode,
                              Parsers.shorter(a, b), source, line, column, message)
        Asserts.assertFailure(mode,
                              Parsers.shorter(b, a), source, line, column, message)
    }

    companion object {
        @JvmStatic @Parameterized.Parameters fun data(): Collection<Array<Any>> {
            return Arrays.asList(arrayOf(Parser.Mode.PRODUCTION), arrayOf(Parser.Mode.DEBUG))
        }
    }

}