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

import org.jparsec.internal.util.Lists
import org.jparsec.pattern.Pattern
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Unit test for [Indentation].
 *
 * @author benyu
 */
@RunWith(Parameterized::class)
class IndentationTest(private val mode: Parser.Mode) {
    @Test fun testInlineWhitespace() {
        val predicate = Indentation.INLINE_WHITESPACE
        Assert.assertTrue(predicate.isChar(' '))
        Assert.assertTrue(predicate.isChar('\t'))
        Assert.assertTrue(predicate.isChar('\r'))
        Assert.assertFalse(predicate.isChar('\n'))
        Assert.assertEquals("whitespace", predicate.toString())
    }

    @Test fun testLineContinuation() {
        val pattern = Indentation.LINE_CONTINUATION
        Assert.assertEquals(Pattern.MISMATCH.toLong(), pattern.match("", 0, 0).toLong())
        Assert.assertEquals(Pattern.MISMATCH.toLong(), pattern.match("a", 0, 1).toLong())
        Assert.assertEquals(Pattern.MISMATCH.toLong(), pattern.match("\\a", 0, 2).toLong())
        Assert.assertEquals(Pattern.MISMATCH.toLong(), pattern.match("\\ \t", 0, 3).toLong())
        Assert.assertEquals(2, pattern.match("\\\n", 0, 2).toLong())
        Assert.assertEquals(6, pattern.match("\\  \t\r\n", 0, 6).toLong())
    }

    @Test fun testInlineWhitespaces() {
        val pattern = Indentation.INLINE_WHITESPACES
        Assert.assertEquals(Pattern.MISMATCH.toLong(), pattern.match("", 0, 0).toLong())
        Assert.assertEquals(Pattern.MISMATCH.toLong(), pattern.match("a", 0, 1).toLong())
        Assert.assertEquals(Pattern.MISMATCH.toLong(), pattern.match("\n", 0, 1).toLong())
        Assert.assertEquals(1, pattern.match(" ", 0, 1).toLong())
        Assert.assertEquals(2, pattern.match("  ", 0, 2).toLong())
        Assert.assertEquals(4, pattern.match("  \t\r", 0, 4).toLong())
    }

    @Test fun testWhitespaces() {
        val scanner = Indentation.WHITESPACES
        Assert.assertEquals("whitespaces", scanner.toString())
        Assert.assertNull(scanner.parse("  \r\t\\ \t\n \r", mode))
        Asserts.assertFailure(mode, scanner, " \r\n", 1, 3, "EOF expected, \n encountered.")
    }

    @Test fun testAnalyzeIndentations() {
        Assert.assertEquals(tokenList(), analyze())
        Assert.assertEquals(tokenList("foo"), analyze("foo"))
        Assert.assertEquals(tokenList("foo", "bar"), analyze("foo", "bar"))
        Assert.assertEquals(tokenList("foo", 1, "bar"), analyze("foo", "\n", "bar"))
        Assert.assertEquals(tokenList("foo", 4, "bar"), analyze("foo", "\n", 2, "\n", "bar"))
        Assert.assertEquals(tokenList("foo", 2, Indentation.Punctuation.INDENT, "bar", 1, Indentation.Punctuation.OUTDENT, 1, "baz"),
                            analyze("foo", "\n", 1, "bar", "\n", "\n", "baz"))
        Assert.assertEquals(tokenList(2, "foo", "bar"), analyze(2, "foo", "bar"))
        Assert.assertEquals(tokenList(2, "foo", 2, "bar"), analyze(2, "foo", "\n", 1, "bar"))
        Assert.assertEquals(tokenList("foo", 2, Indentation.Punctuation.INDENT, "bar", Indentation.Punctuation.OUTDENT),
                            analyze("foo", "\n", 1, "bar"))
        Assert.assertEquals(tokenList("foo", 2, Indentation.Punctuation.INDENT, "bar", 1, Indentation.Punctuation.OUTDENT, 2, Indentation.Punctuation.INDENT, "baz", Indentation.Punctuation.OUTDENT),
                            analyze("foo", "\n", 1, "bar", "\n", "\n", 1, "baz"))
        Assert.assertEquals(tokenList("foo", 2, Indentation.Punctuation.INDENT, "bar", 2, "bar2", 4, Indentation.Punctuation.INDENT, "baz", Indentation.Punctuation.OUTDENT, Indentation.Punctuation.OUTDENT),
                            analyze("foo", "\n", 1, "bar", "\n", 1, "bar2", "\n", 3, "baz"))
        Assert.assertEquals(tokenList(
            "foo", 2, Indentation.Punctuation.INDENT, "bar", 4, Indentation.Punctuation.INDENT, "baz", 3, Indentation.Punctuation.OUTDENT, Indentation.Punctuation.INDENT, "end", Indentation.Punctuation.OUTDENT, Indentation.Punctuation.OUTDENT),
                            analyze("foo", "\n", 1, "bar", "\n", 3, "baz", "\n", 2, "end"))
    }

    @Test fun testIndent() {
        val parser = Indentation().indent()
        Assert.assertEquals(Token(0, 0, Indentation.Punctuation.INDENT),
                            parser.from(Parsers.constant(tokenList(Indentation.Punctuation.INDENT))).parse("", mode))
        Asserts.assertFailure(mode, parser.from(Parsers.constant(tokenList(Indentation.Punctuation.OUTDENT))), "",
                              1, 1, "INDENT expected, OUTDENT encountered.")
    }

    @Test fun testOutdent() {
        val parser = Indentation().outdent()
        Assert.assertEquals(Token(0, 0, Indentation.Punctuation.OUTDENT),
                            parser.from(Parsers.constant(tokenList(Indentation.Punctuation.OUTDENT))).parse("", mode))
        Asserts.assertFailure(mode, parser.from(Parsers.constant(tokenList(Indentation.Punctuation.INDENT))), "",
                              1, 1, "OUTDENT expected, INDENT encountered.")
    }

    @Test fun testLexer() {
        val parser = Indentation().lexer(Scanners.IDENTIFIER, Indentation.WHITESPACES.optional(Unit))
        Assert.assertEquals(
            tokenList("foo", 7, "bar", 4, Indentation.Punctuation.INDENT, "baz", 4, Indentation.Punctuation.INDENT, "bah", 1, "bah", Indentation.Punctuation.OUTDENT, Indentation.Punctuation.OUTDENT),
            parser.parse("foo \\ \n\\\n bar \n  baz\n   bah bah ", mode))
    }

    companion object {
        @JvmStatic @Parameterized.Parameters fun data(): Collection<Array<Any>> {
            return listOf(arrayOf(Parser.Mode.PRODUCTION), arrayOf(Parser.Mode.DEBUG))
        }

        private fun analyze(vararg values: Any): List<Token> {
            return Indentation().analyzeIndentations(tokenList(*values), "\n")
        }

        private fun tokenList(vararg values: Any): List<Token> {
            val tokenList: MutableList<Token> = Lists.arrayList(values.size)
            var index = 0
            for (value in values) {
                when (value) {
                    is Int        -> {
                        index += value
                    }
                    is String -> {
                        val length = value.toString().length
                        tokenList.add(Token(index, length, value))
                        index += length
                    }
                    else  -> {
                        tokenList.add(Token(index, 0, value))
                    }
                }
            }
            return tokenList
        }
    }

}