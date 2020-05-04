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

import org.easymock.EasyMock
import org.jparsec.Asserts.assertFailure
import org.jparsec.Asserts.assertParser
import org.jparsec.TestParsers.areChars
import org.jparsec.TestParsers.isChar
import org.jparsec.easymock.BaseMockTest
import org.jparsec.error.ParserException
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.StringReader
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Unit test for [Parser].
 *
 * @author Ben Yu
 */
@RunWith(Parameterized::class)
class ParserTest(private val mode: Parser.Mode): BaseMockTest() {
    @Test @Throws(Exception::class) fun testParse() {
        assertEquals("foo", FOO.parse("", mode))
        assertFailure(mode, FOO, "a", 1, 1, "EOF expected, a encountered.")
        assertFailure(FOO, "a", 1, 1, "test module", "EOF expected, a encountered.")
        assertEquals(123, INTEGER.parse(StringReader("123")))
        try {
            INTEGER.parse(StringReader("x"), "test module")
            Assert.fail()
        } catch (e: ParserException) {
            assertEquals(1, e.line.toLong())
            assertEquals(1, e.column.toLong())
            Assert.assertTrue(e.message, e.message!!.contains("test module"))
            Assert.assertTrue(e.message, e.message!!.contains("integer expected, x encountered."))
        }
    }

    @Test fun testSource() {
        assertEquals("source", FOO.source().toString())
        assertEquals("", FOO.source().parse("", mode))
        assertParser(mode, COMMA.source(), ", ", ",", " ")
        assertParser(mode, Terminals.IntegerLiteral.TOKENIZER.label("INTEGER").source(), "123 ", "123", " ")
        assertEquals("123",
                            Parsers.tokenType(Integer::class.java, "int")
                                .from(INTEGER, Scanners.WHITESPACES).source()
                                .parse("123", mode))
    }

    @Test fun testToken() {
        assertEquals("foo", FOO.token().toString())
        assertEquals(Token(0, 0, "foo"), FOO.token().parse("", mode))
        assertEquals(Token(0, 3, 123), INTEGER.token().parse("123", mode))
        assertFailure(mode, INTEGER.token(), "a", 1, 1)
    }

    @Test fun testWithSource() {
        assertEquals("foo", FOO.withSource().toString())
        assertEquals(WithSource("foo", ""), FOO.withSource().parse("", mode))
        assertEquals(WithSource(123, "123"), INTEGER.withSource().parse("123", mode))
        assertFailure(mode, INTEGER.withSource(), "a", 1, 1)
    }

    @Mock
    var next: Function<Any, Parser<String>> = Function { error("") }

    @Test fun testNext_withMap() {
        EasyMock.expect(next!!.apply(1)).andReturn(FOO)
        replay()
        assertEquals("foo", INTEGER.next(next).parse("1", mode))
        assertEquals(next.toString(), INTEGER.next(next).toString())
    }

    @Test fun testNext_firstParserFails() {
        replay()
        assertFailure(mode, FAILURE.next(next), "", 1, 1, "failure")
    }

    @Test fun testNext_nextParserFails() {
        EasyMock.expect(next!!.apply(123)).andReturn(FAILURE)
        replay()
        assertFailure(mode, INTEGER.next(next), "123", 1, 4, "failure")
    }

    @Test fun testNext() {
        assertEquals("sequence", COMMA.next(INTEGER).toString())
        assertEquals(123 as Any, COMMA.next(INTEGER).parse(",123", mode))
        assertFailure(mode, FAILURE.next(FOO), "", 1, 1, "failure")
        assertFailure(mode, INTEGER.next(COMMA), "123", 1, 4)
    }

    @Test fun testRetn() {
        assertEquals(1 as Any, COMMA.retn(1).parse(",", mode))
        assertFailure(mode, FAILURE.retn(1), "", 1, 1, "failure")
    }

    @Test fun testUntil() {
        val comma = Scanners.isChar(',').source()
        val dot: Parser<*> = Scanners.isChar('.')
        val parser = INTEGER.cast<Any>().or(comma).until(dot)
        assertParser(mode, parser, "123,456.", listOf<Any>(123, ",", 456), ".")
        assertFailure(mode, parser, "", 1, 1)
        assertParser(mode, parser, ".", listOf<Any>(), ".")
    }

    @Test fun testFollowedBy() {
        assertEquals(123 as Any, INTEGER.followedBy(COMMA).parse("123,", mode))
        assertFailure(mode, FAILURE.followedBy(FOO), "", 1, 1, "failure")
        assertFailure(mode, INTEGER.followedBy(COMMA), "123", 1, 4, ", expected, EOF encountered.")
    }

    @Test fun testNotFollowedBy() {
        assertEquals(123 as Any, INTEGER.notFollowedBy(COMMA).parse("123", mode))
        assertEquals(123 as Any,
                            INTEGER.notFollowedBy(COMMA.times(2)).followedBy(COMMA).parse("123,", mode))
        assertFailure(mode, FAILURE.notFollowedBy(FOO), "", 1, 1, "failure")
        assertFailure(mode, INTEGER.notFollowedBy(COMMA), "123,", 1, 4, "unexpected ,.")
    }

    @Test fun testSkipTimes() {
        assertEquals(null, isChar('a').skipTimes(3).parse("aaa", mode))
        assertFailure(mode, isChar('a').skipTimes(3), "aa", 1, 3)
        assertEquals(null, areChars("ab").skipTimes(3).parse("ababab", mode))
        assertEquals(null, FOO.skipTimes(3).parse("", mode))
        assertFailure(mode, areChars("ab").skipTimes(3), "aba", 1, 4)
        assertEquals("skipTimes", INTEGER.skipTimes(1).toString())
    }

    @Test fun testTimes() {
        assertListParser(isChar('a').times(3), "aaa", 'a', 'a', 'a')
        assertFailure(mode, isChar('a').times(3), "aa", 1, 3)
        assertListParser(areChars("ab").times(3), "ababab", 'b', 'b', 'b')
        assertListParser(FOO.times(2), "", "foo", "foo")
        assertFailure(mode, areChars("ab").times(3), "aba", 1, 4)
        assertEquals("times", INTEGER.times(1).toString())
    }

    @Test fun skipTimes_range() {
        assertEquals(null, isChar('a').skipTimes(0, 1).parse("", mode))
        assertFailure(mode, isChar('a').skipTimes(1, 2), "", 1, 1)
        assertFailure(mode, areChars("ab").skipTimes(1, 2), "aba", 1, 4)
        assertFailure(mode, areChars("ab").skipTimes(1, 2), "aba", 1, 4)
        assertEquals(null, FOO.skipTimes(0, 1).parse("", mode))
        assertEquals(null, FOO.skipTimes(1, 2).parse("", mode))
        assertParser(mode, isChar('a').asDelimiter().next(isChar('b')).skipTimes(1, 2), "aba", null, "a")
        assertEquals("skipTimes", isChar('a').skipTimes(1, 2).toString())
    }

    @Test fun testTimes_range() {
        assertListParser(isChar('a').times(0, 1), "")
        assertFailure(mode, isChar('a').times(1, 2), "", 1, 1)
        assertFailure(mode, areChars("ab").times(1, 2), "aba", 1, 4)
        assertFailure(mode, areChars("ab").times(1, 2), "aba", 1, 4)
        assertListParser(FOO.times(0, 1), "", "foo")
        assertListParser(FOO.times(2, 3), "", "foo", "foo", "foo")
        assertListParser(isChar('a').asDelimiter().next(isChar('b')).times(1, 2).followedBy(isChar('a')),
                         "aba", 'b')
        assertEquals("times", isChar('a').times(1, 2).toString())
    }

    @Test fun testSkipMany() {
        assertEquals(null, isChar('a').skipMany().parse("", mode))
        assertEquals(null, isChar('a').skipMany().parse("a", mode))
        assertEquals(null, isChar('a').skipMany().parse("aaa", mode))
        assertFailure(mode, areChars("ab").skipMany(), "aba", 1, 4)
        assertEquals(null, FOO.skipMany().parse("", mode))
        assertEquals(null, isChar('a').skipAtLeast(0).parse("", mode))
        assertFailure(mode, isChar('a').skipAtLeast(1), "", 1, 1)
        assertFailure(mode, areChars("ab").skipAtLeast(1), "aba", 1, 4)
        assertFailure(mode, areChars("ab").skipAtLeast(2), "aba", 1, 4)
        assertEquals(null, FOO.skipAtLeast(0).parse("", mode))
        assertEquals(null, FOO.skipAtLeast(2).parse("", mode))
        assertParser(mode, isChar('a').asDelimiter().next(isChar('b')).skipMany(), "a", null, "a")
        assertEquals("skipAtLeast", isChar('a').skipMany().toString())
        assertEquals("skipAtLeast", isChar('a').skipAtLeast(2).toString())
    }

    @Test fun testSkipMany1() {
        assertFailure(mode, isChar('a').skipMany1(), "", 1, 1)
        assertEquals(null, isChar('a').skipMany1().parse("a", mode))
        assertEquals(null, isChar('a').skipMany1().parse("aaa", mode))
        assertFailure(mode, areChars("ab").skipMany1(), "aba", 1, 4)
        assertEquals(null, FOO.skipMany1().parse("", mode))
        assertParser(mode, isChar('a').asDelimiter().next(isChar('b')).skipMany1(), "aba", null, "a")
        assertEquals("skipAtLeast", isChar('a').skipMany1().toString())
    }

    @Test fun testMany1() {
        assertFailure(mode, isChar('a').many1(), "", 1, 1)
        assertListParser(isChar('a').many1(), "a", 'a')
        assertListParser(isChar('a').many1(), "aaa", 'a', 'a', 'a')
        assertFailure(mode, areChars("ab").many1(), "aba", 1, 4)
        assertListParser(areChars("ab").many1().followedBy(isChar('a')), "aba", 'b')
        assertListParser(FOO.many1(), "", "foo")
        assertListParser(isChar('a').asDelimiter().next(isChar('b')).many1().followedBy(isChar('a')),
                         "aba", 'b')
        assertEquals("atLeast", isChar('a').many1().toString())
    }

    @Test fun testMany() {
        assertListParser(isChar('a').many(), "")
        assertListParser(isChar('a').many(), "a", 'a')
        assertListParser(isChar('a').many(), "aaa", 'a', 'a', 'a')
        assertFailure(mode, areChars("ab").many(), "aba", 1, 4)
        assertListParser(areChars("ab").many().followedBy(isChar('a')), "aba", 'b')
        assertListParser(FOO.many(), "")
        assertListParser(isChar('a').atLeast(0), "")
        assertFailure(mode, isChar('a').atLeast(1), "", 1, 1)
        assertFailure(mode, areChars("ab").atLeast(1), "aba", 1, 4)
        assertFailure(mode, areChars("ab").atLeast(2), "aba", 1, 4)
        assertListParser(FOO.atLeast(0), "")
        assertListParser(FOO.atLeast(2), "", "foo", "foo")
        assertListParser(isChar('a').asDelimiter().next(isChar('b')).many().followedBy(isChar('a')), "a")
        assertEquals("atLeast", isChar('a').many().toString())
        assertEquals("atLeast", isChar('a').atLeast(1).toString())
    }

    @Test fun testOr() {
        assertEquals("or", INTEGER.or(INTEGER).toString())
        assertEquals(123 as Any, INTEGER.or(Parsers.constant(456)).parse("123", mode))
        assertEquals('b' as Any, isChar('a').or(Parsers.constant('b')).parse("", mode))
        assertEquals('a' as Any, areChars("ab").or(isChar('a')).parse("a", mode))
        assertListParser(areChars("ab").or(isChar('a')).many(), "a", 'a')
        assertFailure(mode, areChars("ab").or(isChar('a')), "x", 1, 1)
    }

    @Test fun testOtherwise() {
        assertEquals(123 as Any, INTEGER.otherwise(Parsers.constant(456)).parse("123", mode))
        assertEquals('b' as Any, isChar('a').otherwise(Parsers.constant('b')).parse("", mode))
        assertFailure(mode, areChars("ab").otherwise(isChar('a')), "a", 1, 2)
        assertFailure(mode, areChars("ab").or(isChar('x')).otherwise(isChar('a')), "a", 1, 2)
        assertFailure(mode, areChars("ab").otherwise(isChar('a')), "x", 1, 1)
        assertEquals("otherwise", INTEGER.otherwise(INTEGER).toString())
    }

    @Test fun testOptional() {
        assertEquals(12 as Any, INTEGER.optional().parse("12", mode))
        assertEquals(null, INTEGER.optional().parse("", mode))
        assertFailure(mode, areChars("ab").optional(), "a", 1, 2)
    }

    @Test fun testAsOptional() {
        assertEquals(Optional.of(12), INTEGER.asOptional().parse("12", mode))
        assertEquals(Optional.empty<Any>(), INTEGER.asOptional().parse("", mode))
        assertFailure(mode, areChars("ab").asOptional(), "a", 1, 2)
    }

    @Test fun testOptional_withDefaultValue() {
        assertEquals(12 as Any, INTEGER.optional(0).parse("12", mode))
        assertEquals(0 as Any, INTEGER.optional(0).parse("", mode))
        assertFailure(mode, areChars("ab").optional('x'), "a", 1, 2)
    }

    @Test fun testNot() {
        assertEquals(null, INTEGER.not().parse("", mode))
        assertFailure(mode, INTEGER.not(), "12", 1, 1)
        assertParser(mode, areChars("ab").not(), "a", null, "a")
        assertEquals(null, INTEGER.not("num").parse("", mode))
        assertFailure(mode, INTEGER.not("num"), "12", 1, 1, "unexpected num")
    }

    @Test fun testPeek() {
        assertParser(mode, INTEGER.peek(), "12", 12, "12")
        assertFailure(mode, INTEGER.peek(), "a", 1, 1)
        assertFailure(mode, areChars("ab").peek(), "a", 1, 2)
        assertParser(mode, Parsers.or(areChars("ab").peek(), isChar('a')), "a", 'a', "")
        assertEquals("peek", INTEGER.peek().toString())
    }

    @Test fun testAtomic() {
        assertEquals("integer", INTEGER.atomic().toString())
        assertEquals('b' as Any, areChars("ab").atomic().parse("ab", mode))
        assertEquals('a' as Any,
                            Parsers.or(areChars("ab").atomic(), isChar('a')).parse("a", mode))
        assertFailure(mode, areChars("ab").atomic(), "a", 1, 2)
    }

    @Test fun testStep() {
        assertEquals(INTEGER.toString(), INTEGER.asDelimiter().toString())
        assertEquals('b' as Any,
                            Parsers.or(areChars("ab").asDelimiter().next(isChar('c')), areChars("ab")).parse("ab", mode))
    }

    @Test fun testSucceeds() {
        assertParser(mode, isChar('a').succeeds(), "ab", true, "b")
        assertParser(mode, isChar('a').succeeds(), "xb", false, "xb")
        assertParser(mode, areChars("ab").succeeds(), "ax", false, "ax")
    }

    @Test fun testFails() {
        assertParser(mode, isChar('a').fails(), "ab", false, "b")
        assertParser(mode, isChar('a').fails(), "xb", true, "xb")
        assertParser(mode, areChars("ab").fails(), "ax", true, "ax")
    }

    @Test fun testIfElse() {
        val parser = areChars("ab").ifelse(INTEGER, Parsers.constant(0))
        assertEquals("ifelse", parser.toString())
        assertEquals(12 as Any, parser.parse("ab12", mode))
        assertEquals(0 as Any, parser.parse("", mode))
        assertParser(mode, parser, "a", 0, "a")
    }

    @Test fun testIfElse_withNext() {
        EasyMock.expect(next!!.apply('b')).andReturn(FOO)
        replay()
        assertEquals("foo", areChars("ab").ifelse<Any>(
            { next?.apply(it!!) }, Parsers.constant("bar")
        ).parse("ab", mode))
    }

    @Test fun testLabel() {
        assertEquals("foo", FOO.label("the foo").parse("", mode))
        assertFailure(mode, INTEGER.label("number"), "", 1, 1, "number")
    }

    @Test fun labelShouldOverrideImplicitErrorMessage() {
        try {
            Scanners.string("foo").label("bar").parse("fo", mode)
            Assert.fail()
        } catch (e: ParserException) {
            Assert.assertTrue(e.message, e.message!!.contains("bar"))
            Assert.assertFalse(e.message, e.message!!.contains("foo"))
        }
    }

    @Test fun labelShouldOverrideLabelMessage() {
        try {
            Scanners.string("foo").label("bar").label("override").parse("fo", mode)
            Assert.fail()
        } catch (e: ParserException) {
            Assert.assertTrue(e.message, e.message!!.contains("override"))
            Assert.assertFalse(e.message, e.message!!.contains("foo"))
            Assert.assertFalse(e.message, e.message!!.contains("bar"))
        }
    }

    @Test fun labelShouldOverrideFromAcrossAtomic() {
        try {
            Scanners.string("foo").label("bar").atomic().label("override").parse("fo", mode)
            Assert.fail()
        } catch (e: ParserException) {
            Assert.assertTrue(e.message, e.message!!.contains("override"))
            Assert.assertFalse(e.message, e.message!!.contains("foo"))
            Assert.assertFalse(e.message, e.message!!.contains("bar"))
        }
    }

    @Test fun labelShouldOverrideFromAcrossCast() {
        try {
            Scanners.string("foo").label("bar").cast<Any>().label("override").parse("fo", mode)
            Assert.fail()
        } catch (e: ParserException) {
            Assert.assertTrue(e.message, e.message!!.contains("override"))
            Assert.assertFalse(e.message, e.message!!.contains("foo"))
            Assert.assertFalse(e.message, e.message!!.contains("bar"))
        }
    }

    @Test fun labelShouldOverrideFromAcrossPeek() {
        try {
            Scanners.string("foo").label("bar").peek().label("override").parse("fo", mode)
            Assert.fail()
        } catch (e: ParserException) {
            Assert.assertTrue(e.message, e.message!!.contains("override"))
            Assert.assertFalse(e.message, e.message!!.contains("foo"))
            Assert.assertFalse(e.message, e.message!!.contains("bar"))
        }
    }

    @Test fun labelShouldOverrideFromAcrossAtomicAndPeek() {
        try {
            Scanners.string("foo").label("bar").atomic().peek().label("override").parse("fo", mode)
            Assert.fail()
        } catch (e: ParserException) {
            Assert.assertTrue(e.message, e.message!!.contains("override"))
            Assert.assertFalse(e.message, e.message!!.contains("foo"))
            Assert.assertFalse(e.message, e.message!!.contains("bar"))
        }
    }

    @Test fun labelShouldOverrideFromAcrossAsDelimiter() {
        try {
            Scanners.string("foo").label("bar").asDelimiter().label("override").parse("fo", mode)
            Assert.fail()
        } catch (e: ParserException) {
            Assert.assertTrue(e.message, e.message!!.contains("override"))
            Assert.assertFalse(e.message, e.message!!.contains("foo"))
            Assert.assertFalse(e.message, e.message!!.contains("bar"))
        }
    }

    @Test fun succeedsShouldNotLeaveErrorBehind() {
        try {
            Scanners.string("foo").succeeds().parse("fo", mode)
            Assert.fail()
        } catch (e: ParserException) {
            Assert.assertTrue(e.message, e.message!!.contains("EOF"))
            Assert.assertFalse(e.message, e.message!!.contains("foo"))
        }
    }

    @Test fun testCast() {
        val parser = Parsers.constant<CharSequence>("chars").cast<String>()
        assertEquals("chars", parser.toString())
        assertEquals("chars", parser.parse("", mode))
    }

    @Test fun testBetween() {
        assertEquals(123 as Any, INTEGER.between(isChar('('), isChar(')')).parse("(123)", mode))
    }

    @Mock
    var map: Function<Int, String> = Function { error("") }

    @Test fun testMap() {
        EasyMock.expect(map!!.apply(12)).andReturn("foo")
        replay()
        assertEquals("foo", INTEGER.map(map).parse("12", mode))
        assertEquals(map.toString(), INTEGER.map(map).toString())
    }

    @Test fun testMap_fails() {
        replay()
        assertFailure(mode, INTEGER.map(map), "", 1, 1, "integer expected, EOF encountered.")
    }

    @Test fun testSepBy1() {
        val parser = INTEGER.sepBy1(isChar(','))
        assertListParser(parser, "1", 1)
        assertListParser(parser, "123,45", 123, 45)
        assertListParser(parser.followedBy(Scanners.isChar(' ')), "1 ", 1)
        assertListParser(parser.followedBy(isChar(',')), "1,", 1)
        assertFailure(mode, parser, "", 1, 1)
        assertFailure(mode, areChars("ab").sepBy1(isChar(',')), "ab,a", 1, 5)
    }

    @Test fun testSepBy() {
        val parser = INTEGER.sepBy(isChar(','))
        assertListParser(parser, "1", 1)
        assertListParser(parser, "123,45", 123, 45)
        assertListParser(parser.followedBy(isChar(' ')), "1 ", 1)
        assertListParser(parser, "")
        assertListParser(parser.followedBy(isChar(',')), "1,", 1)
        assertFailure(mode, areChars("ab").sepBy(isChar(',')), "ab,a", 1, 5)
    }

    @Test fun testEndBy() {
        val parser = INTEGER.endBy(isChar(';'))
        assertListParser(parser, "")
        assertListParser(parser, "1;", 1)
        assertListParser(parser, "12;3;", 12, 3)
        assertListParser(parser.followedBy(isChar(';')), ";")
        assertFailure(mode, parser, "1", 1, 2)
        assertFailure(mode, areChars("ab").endBy(isChar(';')), "ab;a", 1, 5)
    }

    @Test fun testEndBy1() {
        val parser = INTEGER.endBy1(isChar(';'))
        assertListParser(parser, "1;", 1)
        assertListParser(parser, "12;3;", 12, 3)
        assertFailure(mode, parser, "", 1, 1)
        assertFailure(mode, parser, ";", 1, 1)
        assertFailure(mode, parser, "1", 1, 2)
        assertFailure(mode, areChars("ab").endBy1(isChar(';')), "ab;a", 1, 5)
    }

    @Test fun testSepEndBy1() {
        val parser = INTEGER.sepEndBy1(COMMA)
        assertListParser(parser, "1,2", 1, 2)
        assertListParser(parser, "1", 1)
        assertListParser(parser, "1,", 1)
        assertFailure(mode, parser, ",", 1, 1)
        assertFailure(mode, parser, "", 1, 1)
        assertFailure(mode, parser.next(Parsers.EOF), "1,,", 1, 3)
        assertFailure(mode, areChars("ab").sepEndBy1(isChar(';')), "ab;a", 1, 5)

        // atomize on delimiter
        assertListParser(INTEGER.sepEndBy1(COMMA.next(COMMA).atomic()).followedBy(COMMA),
                         "1,", 1)

        // 0 step partial delimiter consumption
        assertListParser(INTEGER.sepEndBy1(COMMA.asDelimiter().next(COMMA)).followedBy(COMMA),
                         "1,", 1)

        // partial delimiter consumption
        assertFailure(mode, INTEGER.sepEndBy1(COMMA.next(COMMA)), "1,", 1, 3, ", expected, EOF encountered.")

        // infinite loop.
        assertListParser(Parsers.always<Any>().sepEndBy1(Parsers.always<Any>()), "", null as Int?)

        // partial consumption on delimited.
        assertFailure(mode, INTEGER.followedBy(COMMA).sepEndBy1(COMMA),
                      "1,,1", 1, 5, ", expected, EOF encountered.")

        // 0 step partial delimited consumption
        assertListParser(INTEGER.asDelimiter().followedBy(COMMA).sepEndBy1(COMMA).followedBy(Scanners.string("1")), "1,,1", 1)
    }

    @Test fun testSepEndBy() {
        val parser = INTEGER.sepEndBy(COMMA)
        assertListParser(parser, "1,2", 1, 2)
        assertListParser(parser, "1", 1)
        assertListParser(parser, "1,", 1)
        assertListParser(parser.followedBy(isChar(',')), ",")
        assertListParser(parser, "")
        assertFailure(mode, parser.next(Parsers.EOF), "1,,", 1, 3)
        assertFailure(mode, areChars("ab").sepEndBy(isChar(';')), "ab;a", 1, 5)

        // atomize on delimiter
        assertListParser(INTEGER.sepEndBy(COMMA.next(COMMA).atomic()).followedBy(COMMA),
                         "1,", 1)

        // 0 step partial delimiter consumption
        assertListParser(INTEGER.sepEndBy(COMMA.asDelimiter().next(COMMA)).followedBy(COMMA),
                         "1,", 1)

        // partial delimiter consumption
        assertFailure(mode, INTEGER.sepEndBy(COMMA.next(COMMA)), "1,", 1, 3, ", expected, EOF encountered.")

        // infinite loop.
        assertListParser(Parsers.always<Any>().sepEndBy(Parsers.always<Any>()), "", (null as Int?))

        // partial consumption on delimited.
        assertFailure(mode, INTEGER.followedBy(COMMA).sepEndBy(COMMA),
                      "1,,1", 1, 5, ", expected, EOF encountered.")

        // 0 step partial delimited consumption
        assertListParser(INTEGER.asDelimiter().followedBy(COMMA).sepEndBy(COMMA).followedBy(Scanners.string("1")), "1,,1", 1)
    }

    @Test fun testEmptyListParser_toString() {
        assertEquals("[]", EmptyListParser.instance<Any>().toString())
    }

    @Mock
    var unaryOp: Function<Int, Int> = Function { error("") }

    @Test fun testPrefix_noOperator() {
        replay()
        val parser = INTEGER.prefix(isChar('-').retn(unaryOp))
        assertEquals(123 as Any, parser.parse("123", mode))
    }

    @Test fun testPrefix() {
        EasyMock.expect(unaryOp!!.apply(1)).andReturn(-1)
        EasyMock.expect(unaryOp!!.apply(-1)).andReturn(1)
        EasyMock.expect(unaryOp!!.apply(1)).andReturn(-1)
        replay()
        val parser = INTEGER.prefix(isChar('-').retn(unaryOp))
        assertEquals(Integer.valueOf(-1), parser.parse("---1", mode))
    }

    @Test fun testPostfix_noOperator() {
        replay()
        val parser = INTEGER.postfix(isChar('^').retn(unaryOp))
        assertEquals(123 as Any, parser.parse("123", mode))
    }

    @Test fun testPostfix() {
        EasyMock.expect(unaryOp!!.apply(2)).andReturn(4)
        EasyMock.expect(unaryOp!!.apply(4)).andReturn(256)
        replay()
        val parser = INTEGER.postfix(isChar('^').retn(unaryOp))
        assertEquals(256 as Any, parser.parse("2^^", mode))
    }

    @Mock
    var binaryOp: BiFunction<Int, Int, Int> = BiFunction { _:Int, _:Int -> error("") }

    @Test fun testInfixn_noOperator() {
        replay()
        val parser = INTEGER.infixn(isChar('+').retn(binaryOp))
        assertEquals(1 as Any, parser.parse("1", mode))
    }

    @Test fun testInfixn() {
        EasyMock.expect(binaryOp!!.apply(1, 2)).andReturn(3)
        replay()
        val parser = INTEGER.infixn(isChar('+').retn(binaryOp))
        assertParser(mode, parser, "1+2+3", 3, "+3")
    }

    @Test fun testInfixl_noOperator() {
        replay()
        val parser = INTEGER.infixl(isChar('+').retn(binaryOp))
        assertEquals(1 as Any, parser.parse("1", mode))
    }

    @Test fun testInfixl() {
        EasyMock.expect(binaryOp!!.apply(4, 1)).andReturn(3)
        EasyMock.expect(binaryOp!!.apply(3, 2)).andReturn(1)
        replay()
        val parser = INTEGER.infixl(isChar('-').retn(binaryOp))
        assertEquals(1 as Any, parser.parse("4-1-2", mode))
    }

    @Test fun testInfixl_fails() {
        assertFailure(mode, INTEGER.infixl(isChar('-').retn(binaryOp)), "4-1-", 1, 5)
    }

    @Test fun testInfixr_noOperator() {
        replay()
        val parser = INTEGER.infixr(isChar('+').retn(binaryOp))
        assertEquals(1 as Any, parser.parse("1", mode))
    }

    @Test fun testInfixr() {
        EasyMock.expect(binaryOp!!.apply(1, 2)).andReturn(12)
        EasyMock.expect(binaryOp!!.apply(4, 12)).andReturn(412)
        replay()
        val parser = INTEGER.infixr(Scanners.string("->").retn(binaryOp))
        assertEquals(412 as Any, parser.parse("4->1->2", mode))
    }

    @Test fun testInfixr_fails() {
        assertFailure(mode, INTEGER.infixr(isChar('-').retn(binaryOp)), "4-1-", 1, 5)
    }

    @Test fun testFrom() {
        val tokenList = listOf(Token(0, 2, 'a'), Token(2, 3, 4L))
        val parser = Terminals.CharLiteral.PARSER.next(Terminals.LongLiteral.PARSER)
        val lexeme = Parsers.constant(tokenList)
        assertEquals(4L as Any, parser.from(lexeme).parse("", mode))
        assertFailure(mode, Terminals.CharLiteral.PARSER.from(Parsers.constant(listOf())), "", 1, 1, "character literal expected, EOF encountered.")
        assertListParser(Parsers.ANY_TOKEN.many().from(lexeme), "", 'a', 4L)
        assertFailure(mode, Parsers.ANY_TOKEN.from(lexeme), "abcde", 1, 3)
        assertFailure(mode, Parsers.always<Any>().from(Parsers.fail<List<Token>>("foo")), "", 1, 1)
        val badParser = Terminals.CharLiteral.PARSER.next(Terminals.Identifier.PARSER)
        assertFailure(mode, badParser.from(lexeme), "aabbb", 1, 3)
    }

    @Test fun testFrom_throwsOnScanners() {
        assertFailure(mode, Scanners.string("foo").from(Parsers.constant(listOf(Token(0, 3, "foo")))),
                      "foo", 1, 1, "Cannot scan characters on tokens.")
        assertFailure(mode, isChar('f').from(Parsers.constant(listOf(Token(0, 1, 'f')))),
                      "f", 1, 1, "Cannot scan characters on tokens.")
    }

    @Test fun testFrom_withDelimiter() {
        val integers = Terminals.IntegerLiteral.PARSER.many()
        val parser = integers.from(Terminals.IntegerLiteral.TOKENIZER, Scanners.WHITESPACES)
        assertEquals("followedBy", parser.toString())
        assertListParser(parser, "12 34   5 ", "12", "34", "5")
    }

    @Test fun testLexer() {
        val parser = Terminals.LongLiteral.DEC_TOKENIZER.lexer(Scanners.WHITESPACES)
        assertEquals(listOf<Token>(), parser.parse("", mode))
        assertEquals(listOf<Token>(), parser.parse("  ", mode))
        assertEquals(listOf(Token(1, 2, 12L)), parser.parse(" 12  ", mode))
        assertEquals(listOf(Token(0, 2, 12L), Token(3, 1, 3L)),
                            parser.parse("12 3  ", mode))
    }

    @Test @Throws(Exception::class) fun testCopy() {
        val content = "foo bar and baz"
        val to = Parser.read(StringReader(content))
        assertEquals(content, to.toString())
    }

    private fun assertListParser(parser: Parser<out List<*>>, source: String, vararg expected: Any?) {
        assertList(parser.parse(source, mode), *expected)
    }

    companion object {
        private val INTEGER = Scanners.INTEGER.source().map { s: String? -> Integer.valueOf(s) }.label("integer")
        private val FOO = Parsers.constant("foo")
        private val FAILURE = Parsers.fail<String>("failure")
        private val COMMA = Scanners.isChar(',')

        @JvmStatic @Parameterized.Parameters fun data(): Collection<Array<Any>> {
            return listOf(arrayOf(Parser.Mode.PRODUCTION), arrayOf(Parser.Mode.DEBUG))
        }

        private fun assertList(actual: Any?, vararg expected: Any?) {
            assertEquals(listOf(*expected), actual)
        }
    }

}