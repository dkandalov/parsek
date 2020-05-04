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
import org.jparsec.Asserts.assertScanner
import org.jparsec.Asserts.assertStringScanner
import org.jparsec.TestParsers.areChars
import org.jparsec.pattern.CharPredicates
import org.jparsec.pattern.Patterns
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

/**
 * Unit test for [Scanners].
 *
 * @author Ben Yu
 */
@RunWith(Parameterized::class)
class ScannersTest(private val mode: Parser.Mode) {
    @Test fun testIdentifier() {
        val scanner = Scanners.IDENTIFIER
        assertStringScanner(mode, scanner, "abc")
        assertStringScanner(mode, scanner, "abc123")
        assertStringScanner(mode, scanner, "abc 123", " 123")
        assertStringScanner(mode, scanner, "_abc_123")
    }

    @Test fun testInteger() {
        val scanner = Scanners.INTEGER
        assertStringScanner(mode, scanner, "123")
        assertStringScanner(mode, scanner, "0")
        assertStringScanner(mode, scanner, "12.3", ".3")
    }

    @Test fun testDecimal() {
        val scanner = Scanners.DECIMAL
        assertStringScanner(mode, scanner, "123")
        assertStringScanner(mode, scanner, "0")
        assertStringScanner(mode, scanner, "12.3")
        assertStringScanner(mode, scanner, ".3")
    }

    @Test fun testDecInteger() {
        val scanner = Scanners.DEC_INTEGER
        assertStringScanner(mode, scanner, "1230")
        assertFailure(mode, scanner, "0", 1, 1, "decimal integer expected, 0 encountered.")
    }

    @Test fun testOctInteger() {
        val scanner = Scanners.OCT_INTEGER
        assertStringScanner(mode, scanner, "01270")
        assertStringScanner(mode, scanner, "0")
        assertFailure(mode, scanner, "12", 1, 1, "octal integer expected, 1 encountered.")
        assertFailure(mode, scanner, "09", 1, 2)
    }

    @Test fun testHexInteger() {
        val scanner = Scanners.HEX_INTEGER
        assertStringScanner(mode, scanner, "0X1AF")
        assertStringScanner(mode, scanner, "0xF0")
        assertFailure(mode, scanner, "1", 1, 1, "hexadecimal integer expected, 1 encountered.")
        assertFailure(mode, scanner, "01", 1, 1)
    }

    @Test fun testScientificNotation() {
        val scanner = Scanners.SCIENTIFIC_NOTATION
        assertStringScanner(mode, scanner, "0e0")
        assertStringScanner(mode, scanner, "1.0E12")
        assertStringScanner(mode, scanner, "1e+12")
        assertStringScanner(mode, scanner, "1e-12")
        assertFailure(mode, scanner, "", 1, 1, "scientific notation expected, EOF encountered.")
        assertFailure(mode, scanner, "12", 1, 1, "scientific notation expected, 1 encountered")
        assertFailure(mode, scanner, "e", 1, 1)
        assertFailure(mode, scanner, "e1", 1, 1)
    }

    @Test fun testMany_withCharPredicate() {
        val scanner = Scanners.many(CharPredicates.IS_ALPHA)
        assertScanner(mode, scanner, "abc123", "123")
        assertScanner(mode, scanner, "123", "123")
    }

    @Test fun testMany1_withCharPredicate() {
        val scanner = Scanners.many1(CharPredicates.IS_ALPHA)
        assertScanner(mode, scanner, "abc123", "123")
        assertFailure(mode, scanner, "123", 1, 1, "[a-zA-Z]+ expected, 1 encountered.")
        assertFailure(mode, scanner, "", 1, 1, "[a-zA-Z]+ expected, EOF encountered.")
    }

    @Test fun testMany_withPattern() {
        val scanner = Patterns.string("ab").many().toScanner("(ab)*")
        assertNull(scanner.parse("abab"))
        assertScanner(mode, scanner, "aba", "a")
        assertScanner(mode, scanner, "abc", "c")
        assertScanner(mode, scanner, "c", "c")
        assertNull(scanner.parse(""))
    }

    @Test fun testMany_withPatternThatConsumesNoInput() {
        val scanner = Patterns.ALWAYS.many().toScanner("*")
        assertNull(scanner.parse(""))
        assertScanner(mode, scanner, "a", "a")
    }

    @Test fun testMany1_withPattern() {
        val scanner = Patterns.string("ab").many1().toScanner("(ab)+")
        assertNull(scanner.parse("abab"))
        assertScanner(mode, scanner, "aba", "a")
        assertScanner(mode, scanner, "abc", "c")
        assertFailure(mode, scanner, "c", 1, 1, "(ab)+ expected, c encountered.")
        assertFailure(mode, scanner, "", 1, 1, "(ab)+ expected, EOF encountered.")
    }

    @Test fun testMany1_withPatternThatConsumesNoInput() {
        val scanner = Patterns.ALWAYS.many1().toScanner("+")
        assertNull(scanner.parse(""))
        assertScanner(mode, scanner, "a", "a")
    }

    @Test fun testString() {
        val scanner = Scanners.string("ab")
        assertNull(scanner.parse("ab"))
        assertScanner(mode, scanner, "abc", "c")
        assertFailure(mode, scanner, "c", 1, 1, "ab expected, c encountered.")
        assertFailure(mode, scanner, "a", 1, 1)
        assertFailure(mode, scanner, "", 1, 1)
    }

    @Test fun testWhitespaces() {
        val scanner = Scanners.WHITESPACES
        Assert.assertEquals("whitespaces", scanner.toString())
        assertNull(scanner.parse(" \r\n"))
        assertScanner(mode, scanner, " \r\na", "a")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "a", 1, 1)
    }

    @Test fun testPattern() {
        val scanner = Patterns.INTEGER.toScanner("integer")
        assertNull(scanner.parse("123"))
        assertScanner(mode, scanner, "12a", "a")
        assertFailure(mode, scanner, "", 1, 1, "integer expected, EOF encountered.")
        assertFailure(mode, scanner, "a", 1, 1)
    }

    @Test fun testStringCaseInsensitive() {
        val scanner = Scanners.stringCaseInsensitive("ab")
        assertNull(scanner.parse("ab"))
        assertNull(scanner.parse("AB"))
        assertNull(scanner.parse("aB"))
        assertFailure(mode, scanner, "", 1, 1, "ab expected, EOF encountered.")
        assertFailure(mode, scanner, "a", 1, 1)
    }

    @Test fun testAnyChar() {
        val scanner = Scanners.ANY_CHAR
        assertNull(scanner.parse("a"))
        assertNull(scanner.parse("1"))
        assertNull(scanner.parse(" "))
        assertNull(scanner.parse("\n"))
        assertScanner(mode, scanner, "ab", "b")
        assertFailure(mode, scanner, "", 1, 1, "any character expected, EOF encountered.")
        Assert.assertEquals("any character", scanner.toString())
    }

    @Test fun testIsChar() {
        val scanner = Scanners.isChar('a')
        assertNull(scanner.parse("a"))
        assertScanner(mode, scanner, "abc", "bc")
        assertFailure(mode, scanner, "bc", 1, 1, "a expected, b encountered.")
        assertFailure(mode, scanner, "", 1, 1)
    }

    @Test fun testNotChar() {
        val scanner = Scanners.notChar('a')
        assertNull(scanner.parse("b"))
        assertScanner(mode, scanner, "bcd", "cd")
        assertFailure(mode, scanner, "abc", 1, 1, "^a expected, a encountered.")
        assertFailure(mode, scanner, "", 1, 1)
    }

    @Test fun testAmong() {
        val scanner = Scanners.among("ab")
        assertNull(scanner.parse("a"))
        assertNull(scanner.parse("b"))
        assertScanner(mode, scanner, "ab", "b")
        assertFailure(mode, scanner, "c", 1, 1, "[ab] expected, c encountered.")
        assertFailure(mode, scanner, "", 1, 1, "[ab] expected, EOF encountered.")
    }

    @Test fun testAmong_noChars() {
        val scanner = Scanners.among("")
        assertFailure(mode, scanner, "a", 1, 1, "none expected, a encountered.")
        assertFailure(mode, scanner, "", 1, 1, "none expected, EOF encountered.")
    }

    @Test fun testAmong_oneChar() {
        val scanner = Scanners.among("a")
        assertNull(scanner.parse("a"))
        assertScanner(mode, scanner, "ab", "b")
        assertFailure(mode, scanner, "b", 1, 1, "a expected, b encountered.")
        assertFailure(mode, scanner, "", 1, 1)
    }

    @Test fun testNotAmong() {
        val scanner = Scanners.notAmong("ab")
        assertNull(scanner.parse("0"))
        assertScanner(mode, scanner, "0a", "a")
        assertFailure(mode, scanner, "a", 1, 1, "^[ab] expected, a encountered.")
        assertFailure(mode, scanner, "b", 1, 1)
        assertFailure(mode, scanner, "", 1, 1)
    }

    @Test fun testNotAmong_noChars() {
        val scanner = Scanners.notAmong("")
        assertNull(scanner.parse("0"))
        assertScanner(mode, scanner, "ab", "b")
        assertFailure(mode, scanner, "", 1, 1, "any character expected, EOF encountered.")
    }

    @Test fun testNotAmong_oneChar() {
        val scanner = Scanners.notAmong("a")
        assertNull(scanner.parse("0"))
        assertScanner(mode, scanner, "0a", "a")
        assertFailure(mode, scanner, "a", 1, 1)
        assertFailure(mode, scanner, "", 1, 1)
    }

    @Test fun testLineComment() {
        val scanner = Scanners.lineComment("#")
        assertNull(scanner.parse("#hello world"))
        assertScanner(mode, scanner, "#hello world\n", "\n")
        assertScanner(mode, scanner, "#hello world\r\n", "\n")
        assertScanner(mode, scanner, "#\n", "\n")
        assertNull(scanner.parse("#"))
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "\n", 1, 1)
        assertFailure(mode, scanner, "a", 1, 1)
    }

    @Test fun testJavaLineComment() {
        val scanner = Scanners.JAVA_LINE_COMMENT
        assertNull(scanner.parse("//hello"))
    }

    @Test fun testSqlLineComment() {
        val scanner = Scanners.SQL_LINE_COMMENT
        assertNull(scanner.parse("--hello"))
    }

    @Test fun testHaskellLineComment() {
        val scanner = Scanners.HASKELL_LINE_COMMENT
        assertNull(scanner.parse("--hello"))
    }

    @Test fun testDoubleQuoteString() {
        val scanner = Scanners.DOUBLE_QUOTE_STRING
        assertStringScanner(mode, scanner, "\"\"")
        assertStringScanner(mode, scanner, "\"a b'c\"")
        assertStringScanner(mode, scanner, "\"a\\\\\\\"1\"")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "ab", 1, 1)
        assertFailure(mode, scanner, "\"ab", 1, 4)
        assertFailure(mode, scanner, "\"\\\"", 1, 4)
    }

    @Test fun testSingleQuoteString() {
        val scanner = Scanners.SINGLE_QUOTE_STRING
        assertStringScanner(mode, scanner, "''")
        assertStringScanner(mode, scanner, "'a'")
        assertStringScanner(mode, scanner, "'foo'")
        assertStringScanner(mode, scanner, "'foo''s day'")
    }

    @Test fun testSingleQuoteChar() {
        val scanner = Scanners.SINGLE_QUOTE_CHAR
        assertStringScanner(mode, scanner, "'a'")
        assertStringScanner(mode, scanner, "'\\a'")
        assertStringScanner(mode, scanner, "'\\\\'")
        assertStringScanner(mode, scanner, "'\\\"'")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "ab", 1, 1)
        assertFailure(mode, scanner, "''", 1, 2)
        assertFailure(mode, scanner, "'\\'", 1, 4)
    }

    @Test fun testJavaDelimiter() {
        val scanner = Scanners.JAVA_DELIMITER
        assertNull(scanner.parse(""))
        assertNull(scanner.parse(" "))
        assertNull(scanner.parse("//comment"))
        assertNull(scanner.parse("/*comment*/"))
        assertNull(scanner.parse("  //line comment\n\t/*block comment*/ "))
        assertScanner(mode, scanner, "a", "a")
    }

    @Test fun testSqlDelimiter() {
        val scanner = Scanners.SQL_DELIMITER
        assertNull(scanner.parse(""))
        assertNull(scanner.parse(" "))
        assertNull(scanner.parse("--comment"))
        assertNull(scanner.parse("/*comment*/"))
        assertNull(scanner.parse("  --line comment\n\t/*block comment*/ "))
        assertScanner(mode, scanner, "a", "a")
    }

    @Test fun testHaskellDelimiter() {
        val scanner = Scanners.HASKELL_DELIMITER
        assertNull(scanner.parse(""))
        assertNull(scanner.parse(" "))
        assertNull(scanner.parse("--comment"))
        assertNull(scanner.parse("{-comment-}"))
        assertNull(scanner.parse("  --line comment\n\t{-block comment-} "))
        assertScanner(mode, scanner, "a", "a")
    }

    @Test fun testJavaBlockComment() {
        val scanner = Scanners.JAVA_BLOCK_COMMENT
        assertNull(scanner.parse("/* this is a comment */"))
        assertNull(scanner.parse("/** another comment */"))
        assertNull(scanner.parse("/** \"comment\" again **/"))
        assertScanner(mode, scanner, "/*comment*/not comment*/", "not comment*/")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "/*a *", 1, 6)
    }

    @Test fun testSqlBlockComment() {
        val scanner = Scanners.SQL_BLOCK_COMMENT
        assertNull(scanner.parse("/* this is a comment */"))
        assertNull(scanner.parse("/** another comment */"))
        assertNull(scanner.parse("/** \"comment\" again **/"))
        assertScanner(mode, scanner, "/*comment*/not comment*/", "not comment*/")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "/*a *", 1, 6)
    }

    @Test fun testHaskellBlockComment() {
        val scanner = Scanners.HASKELL_BLOCK_COMMENT
        assertNull(scanner.parse("{- this is a comment -}"))
        assertNull(scanner.parse("{-- another comment -}"))
        assertNull(scanner.parse("{-- \"comment\" again --}"))
        assertScanner(mode, scanner, "{-comment-}not comment-}", "not comment-}")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "{-a -", 1, 6)
    }

    @Test fun testBlockComment() {
        val scanner = Scanners.blockComment("<<", ">>")
        assertNull(scanner.parse("<< this is a comment >>"))
        assertNull(scanner.parse("<<< another comment >>"))
        assertNull(scanner.parse("<<< \"comment\" again >>"))
        assertScanner(mode, scanner, "<<comment>>not comment>>", "not comment>>")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "<<a >", 1, 6)
    }

    @Test fun testBlockComment_emptyQuotes() {
        val scanner = Scanners.blockComment("", "")
        assertScanner(mode, scanner, "abc", "abc")
        assertNull(scanner.parse(""))
    }

    @Test fun testBlockComment_withQuotedPattern() {
        val scanner = Scanners.blockComment("<<", ">>", Patterns.hasAtLeast(1))
        assertNull(scanner.parse("<<abc>>"))
        assertNull(scanner.parse("<<>>"))
        assertNull(scanner.parse("<<<>>"))
        assertScanner(mode, scanner, "<<a>>>\n", ">\n")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "a", 1, 1)
    }

    @Test fun testBlockComment_withEmptyQuotedPattern() {
        val scanner = Scanners.blockComment("<<", ">>", Patterns.ALWAYS)
        assertNull(scanner.parse("<<>>"))
        assertFailure(mode, scanner, "<<a>>", 1, 3)
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "a", 1, 1)
    }

    @Test fun testBlockComment_withQuotedPatternThatMismatches() {
        val scanner = Scanners.blockComment("<<", ">>", Patterns.NEVER)
        assertNull(scanner.parse("<<>>"))
        assertFailure(mode, scanner, "<<a>>", 1, 3)
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "a", 1, 1)
    }

    @Test fun testBlockComment_withParsers() {
        val scanner = Scanners.blockComment(
            Scanners.string("<!--"), Scanners.string("-->"), Scanners.ANY_CHAR)
        assertNull(scanner.parse("<!--abc-->"))
        assertNull(scanner.parse("<!---->"))
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "a", 1, 1)
    }

    @Test fun testBlockComment_withQuotedParserThatMatchesEmpty() {
        val scanner = Scanners.blockComment(
            Scanners.string("<!--"), Scanners.string("-->"),
            Patterns.ALWAYS.toScanner("nothing"))
        assertNull(scanner.parse("<!---->"))
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "<!-", 1, 1)
    }

    @Test fun testBlockComment_withQuotedParserThatMismatches() {
        val scanner = Scanners.blockComment(
            Scanners.string("<!--"), Scanners.string("-->"),
            Patterns.NEVER.toScanner("nothing"))
        assertNull(scanner.parse("<!---->"))
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "<!-", 1, 1)
    }

    @Test fun testNestableBlockComment() {
        val scanner = Scanners.nestableBlockComment("/*", "*/")
        Assert.assertEquals("nestable block comment", scanner.toString())
        assertNull(scanner.parse("/* not nested */"))
        assertNull(scanner.parse("/* this is /*nested*/ */"))
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "/*", 1, 3)
        assertFailure(mode, scanner, "/* /**/", 1, 8)
        assertFailure(mode, scanner, "/* /**/*", 1, 9)
    }

    @Test fun testNestableBlockComment_withQuotedPattern() {
        val scanner = Scanners.nestableBlockComment("<!--", "-->", Patterns.ANY_CHAR)
        assertNull(scanner.parse("<!-- not nested -->"))
        assertNull(scanner.parse("<!-- this is <!--nested--> -->"))
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "<!--", 1, 5)
        assertFailure(mode, scanner, "<!-- <!---->", 1, 13)
        assertFailure(mode, scanner, "<!-- <!---->-", 1, 14)
    }

    @Test fun testNestableBlockComment_withQuotedParser() {
        val scanner = Scanners.nestableBlockComment(
            Scanners.string("<!--"), Scanners.string("-->"),
            Scanners.isChar(CharPredicates.not(CharPredicates.IS_DIGIT)))
        assertNull(scanner.parse("<!-- not nested -->"))
        assertNull(scanner.parse("<!-- this is <!--nested--> -->"))
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "<!-- 1-->", 1, 6)
        assertFailure(mode, scanner, "<!--", 1, 5)
        assertFailure(mode, scanner, "<!-- <!---->", 1, 13)
        assertFailure(mode, scanner, "<!-- <!---->-", 1, 14)
    }

    @Test fun testNestedBlockComment_partialMatch() {
        val scanner = Scanners.nestableBlockComment(
            areChars("/*"), areChars("*/"), Scanners.isChar('a').many())
        assertNull(scanner.parse("/*aaa*/"))
        assertNull(scanner.parse("/*a/*aa*/a*/"))
        assertFailure(mode, scanner, "/**a", 1, 4, "/ expected, a encountered.")
        assertFailure(mode, scanner, "/*/a", 1, 4, "* expected, a encountered.")
        assertFailure(mode, scanner, "/**a", 1, 4, "/ expected, a encountered.")
    }

    @Test fun testNestedBlockComment_notLogicalPartialMatch() {
        val scanner = Scanners.nestableBlockComment(
            Scanners.isChar('/').asDelimiter().next(Scanners.isChar('*')),
            Scanners.isChar('*').asDelimiter().next(Scanners.isChar('/')),
            Scanners.among("*/"))
        assertNull(scanner.parse("/*****/"))
        assertNull(scanner.parse("/*//****/*/"))
        assertFailure(mode, scanner, "/***//*/", 1, 6)
    }

    @Test fun testNestableBlockComment_quotedConsumesNoChar() {
        val scanner = Scanners.nestableBlockComment("<!--", "-->", Patterns.ALWAYS)
        assertFailure(mode, scanner, "<!-- -->", 1, 5, IllegalStateException::class.java)
    }

    @Test fun testNestableBlockComment_openQuoteConsumesNoChar() {
        val scanner = Scanners.nestableBlockComment(
            Parsers.always<Any>(), Scanners.string("*/"), Scanners.ANY_CHAR)
        assertFailure(mode, scanner, "/**/", 1, 1, IllegalStateException::class.java)
    }

    @Test fun testNestableBlockComment_closeQuoteConsumesNoChar() {
        val scanner = Scanners.nestableBlockComment(
            Scanners.string("/*"), Parsers.always<Any>(), Scanners.ANY_CHAR)
        assertFailure(mode, scanner, "/* */", 1, 3, IllegalStateException::class.java)
    }

    @Test fun testQuoted_byChar() {
        val scanner = Scanners.quoted('<', '>')
        assertStringScanner(mode, scanner, "<abc123>")
        assertFailure(mode, scanner, "<a", 1, 3)
    }

    @Test fun testQuoted() {
        val scanner = Scanners.quoted(Scanners.isChar('<'), Scanners.isChar('>'), Patterns.INTEGER.toScanner("number"))
        assertStringScanner(mode, scanner, "<>")
        assertStringScanner(mode, scanner, "<123>")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "<12", 1, 4)
        assertFailure(mode, scanner, "<a>", 1, 2)
    }

    @Test fun testQuoted_quotedParserConsumeNoChar() {
        val scanner = Scanners.quoted(Scanners.isChar('<'), Scanners.isChar('>'), Parsers.always<Any>())
        assertStringScanner(mode, scanner, "<>")
        assertFailure(mode, scanner, "", 1, 1)
        assertFailure(mode, scanner, "<a>", 1, 2)
    }

    @Test fun testNestedScanner() {
        val scanner = Scanners.nestedScanner(
            Scanners.isChar(CharPredicates.IS_ALPHA).skipMany1(), Scanners.isChar('a').skipTimes(2))
        Assert.assertEquals("nested scanner", scanner.toString())
        assertNull(scanner.parse("aa"))
        assertNull(scanner.parse("aabb"))
        assertFailure(mode, scanner, "ab", 1, 2)
        assertFailure(mode, scanner, "01", 1, 1)
        assertNull(Scanners.isChar(' ').next(scanner).parse(" aa"))
        assertNull(Scanners.isChar(' ').next(scanner).parse(" aab"))
        assertScanner(mode, Scanners.isChar(' ').next(scanner), " aab1", "1")
        assertScanner(mode, Scanners.isChar(' ').next(scanner), " aa1", "1")
    }

    @Test fun veryLongDoublyQuotedStringWithEscapedDoubleQuotes() {
        val quoted = "\"" + replicate(1000, "\n\\\"dsvtrbdfvbgf\\\"") + "\""
        Assert.assertEquals(quoted, Scanners.DOUBLE_QUOTE_STRING.parse(quoted))
    }

    @Test fun veryLongStringWithEscapedSingleQuotes() {
        val quoted = replicate(1000, "a''bc")
        Assert.assertEquals("'$quoted'", Scanners.SINGLE_QUOTE_STRING.parse("'$quoted'"))
    }

    companion object {
        @JvmStatic @Parameterized.Parameters fun data(): Collection<Array<Any>> {
            return listOf(arrayOf(Parser.Mode.PRODUCTION), arrayOf(Parser.Mode.DEBUG))
        }

        private fun replicate(times: Int, s: String): String {
            val builder = StringBuilder()
            for (i in 0 until times) {
                builder.append(s)
            }
            return builder.toString()
        }
    }

}