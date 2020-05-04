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
import org.jparsec.TestParsers.areChars
import org.jparsec.TestParsers.isChar
import org.jparsec.easymock.BaseMockTest
import org.jparsec.functors.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import java.util.function.BiFunction

/**
 * Unit test for [Parsers].
 *
 * @author Ben Yu
 */
@RunWith(Parameterized::class)
class ParsersTest(private val mode: Parser.Mode): BaseMockTest() {
    @Test fun testAlways() {
        assertEquals(null, Parsers.always<Any>().parse("", mode))
    }

    @Test fun testNever() {
        assertFailure(mode, Parsers.never<Any>(), "", 1, 1)
        assertEquals("never", Parsers.never<Any>().toString())
    }

    @Test fun testBetween() {
        assertEquals(null,
                            Parsers.between(isChar('('), Scanners.string("foo"), isChar(')')).parse("(foo)", mode))
    }

    @Test fun testEof() {
        Parsers.EOF.parse("", mode)
        assertFailure(mode, Parsers.EOF, "a", 1, 1, "EOF")
        Parsers.eof("END").parse("", mode)
        assertFailure(mode, Parsers.eof("END"), "a", 1, 1, "END")
        assertEquals("EOF", Parsers.EOF.toString())
    }

    @Test fun testConstant() {
        assertEquals("foo",
                            Parsers.constant("foo").followedBy(Scanners.string("bar")).parse("bar", mode))
        assertEquals("foo", Parsers.constant("foo").toString())
    }

    @Test fun testRunnable() {
        val runnable = mock(Runnable::class.java)
        runnable.run()
        replay()
        assertEquals(null, Parsers.runnable(runnable).parse("", mode))
        assertEquals(runnable.toString(), Parsers.runnable(runnable).toString())
    }

    @Test fun testSequence_2Parsers() {
        val parser = Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'))
        assertEquals("sequence", parser.toString())
        assertEquals(null, parser.parse("ab", mode))
        assertFailure(mode, parser, "xb", 1, 1)
        assertFailure(mode, parser, "ax", 1, 2)
    }

    @Test fun testSequence_3Parsers() {
        val parser = Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'))
        assertEquals("sequence", parser.toString())
        assertEquals(null, parser.parse("abc", mode))
        assertFailure(mode, parser, "xbc", 1, 1)
        assertFailure(mode, parser, "axc", 1, 2)
        assertFailure(mode, parser, "abx", 1, 3)
    }

    @Test fun testSequence_4Parsers() {
        val parser = Parsers.sequence(
            Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'), Scanners.isChar('d'))
        assertEquals("sequence", parser.toString())
        assertEquals(null, parser.parse("abcd", mode))
        assertFailure(mode, parser, "xbcd", 1, 1)
        assertFailure(mode, parser, "axcd", 1, 2)
        assertFailure(mode, parser, "abxd", 1, 3)
        assertFailure(mode, parser, "abcx", 1, 4)
    }

    @Test fun testSequence_5Parsers() {
        val parser = Parsers.sequence(
            Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'),
            Scanners.isChar('d'), Scanners.isChar('e'))
        assertEquals("sequence", parser.toString())
        assertEquals(null, parser.parse("abcde", mode))
        assertFailure(mode, parser, "bbcde", 1, 1)
        assertFailure(mode, parser, "aacde", 1, 2)
        assertFailure(mode, parser, "abbde", 1, 3)
        assertFailure(mode, parser, "abcce", 1, 4)
        assertFailure(mode, parser, "abcdd", 1, 5)
    }

    @Test fun testSequence() {
        val parser: Parser<*> = Parsers.sequence(Scanners.isChar('a'), Scanners.isChar('b'), Scanners.isChar('c'))
        assertEquals("sequence", parser.toString())
        assertEquals(null, parser.parse("abc", mode))
        assertFailure(mode, parser, "xbc", 1, 1)
        assertFailure(mode, parser, "axc", 1, 2)
        assertFailure(mode, parser, "abx", 1, 3)
    }

    @Test fun testSequence_withIterable() {
        val parser: Parser<*> = Parsers.sequence(Arrays.asList(Scanners.isChar('a'), Scanners.isChar('b')))
        assertEquals("sequence", parser.toString())
        assertEquals(null, parser.parse("ab", mode))
        assertFailure(mode, parser, "xb", 1, 1)
        assertFailure(mode, parser, "ax", 1, 2)
    }

    @Test fun testSequence_0Parser() {
        val parser: Parser<*> = Parsers.sequence()
        assertEquals("sequence", parser.toString())
        assertEquals(null, parser.parse("", mode))
    }

    @Test fun testSequence_1Parser() {
        val parser: Parser<*> = Parsers.sequence(Scanners.isChar('a'))
        assertEquals("sequence", parser.toString())
        assertEquals(null, parser.parse("a", mode))
    }

    @Test fun testPair() {
        val parser: Parser<*> = Parsers.pair(isChar('a'), isChar('b'))
        assertEquals(Tuples.pair('a', 'b'), parser.parse("ab", mode))
        assertFailure(mode, parser, "xb", 1, 1)
        assertFailure(mode, parser, "ax", 1, 2)
    }

    @Test fun testTuple_2Parsers() {
        val parser: Parser<*> = Parsers.tuple(isChar('a'), isChar('b'))
        assertEquals(Tuples.pair('a', 'b'), parser.parse("ab", mode))
        assertFailure(mode, parser, "xb", 1, 1)
        assertFailure(mode, parser, "ax", 1, 2)
    }

    @Test fun testTuple_3Parsers() {
        val parser: Parser<*> = Parsers.tuple(isChar('a'), isChar('b'), isChar('c'))
        assertEquals(Tuples.tuple('a', 'b', 'c'), parser.parse("abc", mode))
        assertFailure(mode, parser, "xbc", 1, 1)
        assertFailure(mode, parser, "axc", 1, 2)
        assertFailure(mode, parser, "abx", 1, 3)
    }

    @Test fun testTuple_4Parsers() {
        val parser: Parser<*> = Parsers.tuple(isChar('a'), isChar('b'), isChar('c'), isChar('d'))
        assertEquals(Tuples.tuple('a', 'b', 'c', 'd'), parser.parse("abcd", mode))
        assertFailure(mode, parser, "xbcd", 1, 1)
        assertFailure(mode, parser, "axcd", 1, 2)
        assertFailure(mode, parser, "abxd", 1, 3)
        assertFailure(mode, parser, "abcx", 1, 4)
    }

    @Test fun testTuple_5Parsers() {
        val parser: Parser<*> = Parsers.tuple(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'))
        assertEquals(Tuples.tuple('a', 'b', 'c', 'd', 'e'), parser.parse("abcde", mode))
        assertFailure(mode, parser, "xbcde", 1, 1)
        assertFailure(mode, parser, "axcde", 1, 2)
        assertFailure(mode, parser, "abxde", 1, 3)
        assertFailure(mode, parser, "abcxe", 1, 4)
        assertFailure(mode, parser, "abcdx", 1, 5)
    }

    @Test fun testArray() {
        val parser = Parsers.array(isChar('a'), isChar('b'))
        assertEquals("array", parser.toString())
        assertEquals(Arrays.asList('a', 'b'), Arrays.asList(*parser.parse("ab", mode)))
        assertFailure(mode, parser, "xb", 1, 1)
        assertFailure(mode, parser, "ax", 1, 2)
    }

    @Test fun testList() {
        val parser = Parsers.list(Arrays.asList(isChar('a'), isChar('b')))
        assertEquals("list", parser.toString())
        assertEquals(Arrays.asList('a', 'b'), parser.parse("ab", mode))
        assertFailure(mode, parser, "xb", 1, 1)
        assertFailure(mode, parser, "ax", 1, 2)
    }

    @Test fun testFail() {
        assertFailure(mode, Parsers.fail<Any>("foo"), "a", 1, 1, "foo")
        assertEquals("foo", Parsers.fail<Any>("foo").toString())
    }

    @Test fun testOr_0Parser() {
        assertSame(Parsers.never<Any>(), Parsers.or<Any>())
    }

    @Test fun testOr_1Parser() {
        val parser: Parser<*> = Parsers.constant(1)
        assertSame(parser, Parsers.or(parser))
    }

    @Test fun testOr_withIterable() {
        val parser = Parsers.or(Arrays.asList(isChar('a'), isChar('b')))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('a')).parse("a", mode))
        assertFailure(mode, Parsers.or(areChars("abc"), areChars("ax")), "abx", 1, 3)
        assertFailure(mode, Parsers.or(areChars("ax"), areChars("abc")), "abx", 1, 3)
    }

    @Test fun testOr_2Parsers() {
        val parser = Parsers.or(isChar('a'), isChar('b'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('a')).parse("a", mode))
        assertFailure(mode, Parsers.or(areChars("abc"), areChars("ax")), "abx", 1, 3)
        assertFailure(mode, Parsers.or(areChars("ax"), areChars("abc")), "abx", 1, 3)
    }

    @Test fun testOr_3Parsers() {
        val parser = Parsers.or(isChar('a'), isChar('b'), isChar('c'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('c' as Any, parser.parse("c", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('b'), isChar('a')).parse("a", mode))
    }

    @Test fun testOr_4Parsers() {
        val parser = Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('c' as Any, parser.parse("c", mode))
        assertEquals('d' as Any, parser.parse("d", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('b'), isChar('c'), isChar('a')).parse("a", mode))
    }

    @Test fun testOr_5Parsers() {
        val parser = Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('c' as Any, parser.parse("c", mode))
        assertEquals('d' as Any, parser.parse("d", mode))
        assertEquals('e' as Any, parser.parse("e", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
                                                   isChar('d'), isChar('a')).parse("a", mode))
    }

    @Test fun testOr_6Parsers() {
        val parser = Parsers.or(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('c' as Any, parser.parse("c", mode))
        assertEquals('d' as Any, parser.parse("d", mode))
        assertEquals('e' as Any, parser.parse("e", mode))
        assertEquals('f' as Any, parser.parse("f", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
                                                   isChar('d'), isChar('e'), isChar('a')).parse("a", mode))
    }

    @Test fun testOr_7Parsers() {
        val parser = Parsers.or(
            isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('c' as Any, parser.parse("c", mode))
        assertEquals('d' as Any, parser.parse("d", mode))
        assertEquals('e' as Any, parser.parse("e", mode))
        assertEquals('f' as Any, parser.parse("f", mode))
        assertEquals('g' as Any, parser.parse("g", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
                                                   isChar('d'), isChar('e'), isChar('f'), isChar('a')).parse("a", mode))
    }

    @Test fun testOr_8Parsers() {
        val parser = Parsers.or(
            isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
            isChar('h'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('c' as Any, parser.parse("c", mode))
        assertEquals('d' as Any, parser.parse("d", mode))
        assertEquals('e' as Any, parser.parse("e", mode))
        assertEquals('f' as Any, parser.parse("f", mode))
        assertEquals('g' as Any, parser.parse("g", mode))
        assertEquals('h' as Any, parser.parse("h", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
                                                   isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('a')).parse("a", mode))
    }

    @Test fun testOr_9Parsers() {
        val parser = Parsers.or(
            isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
            isChar('h'), isChar('i'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('c' as Any, parser.parse("c", mode))
        assertEquals('d' as Any, parser.parse("d", mode))
        assertEquals('e' as Any, parser.parse("e", mode))
        assertEquals('f' as Any, parser.parse("f", mode))
        assertEquals('g' as Any, parser.parse("g", mode))
        assertEquals('h' as Any, parser.parse("h", mode))
        assertEquals('i' as Any, parser.parse("i", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
                                                   isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('h'), isChar('a')).parse("a", mode))
    }

    @Test fun testOr_10Parsers() {
        val parser = Parsers.or(
            isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'),
            isChar('h'), isChar('i'), isChar('j'))
        assertEquals("or", parser.toString())
        assertEquals('a' as Any, parser.parse("a", mode))
        assertEquals('b' as Any, parser.parse("b", mode))
        assertEquals('c' as Any, parser.parse("c", mode))
        assertEquals('d' as Any, parser.parse("d", mode))
        assertEquals('e' as Any, parser.parse("e", mode))
        assertEquals('f' as Any, parser.parse("f", mode))
        assertEquals('g' as Any, parser.parse("g", mode))
        assertEquals('h' as Any, parser.parse("h", mode))
        assertEquals('i' as Any, parser.parse("i", mode))
        assertEquals('a' as Any, Parsers.or(areChars("ab"), isChar('b'), isChar('c'),
                                                   isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('h'), isChar('i'),
                                                   isChar('a')).parse("a", mode))
    }

    @Test fun testLonger() {
        assertEquals('b' as Any, Parsers.longer(isChar('a'), areChars("ab")).parse("ab", mode))
        assertEquals('b' as Any, Parsers.longer(areChars("ab"), isChar('a')).parse("ab", mode))
        assertEquals('c' as Any, Parsers.longer(areChars("ab"), areChars("abc")).parse("abc", mode))
        assertEquals('c' as Any, Parsers.longer(areChars("abc"), areChars("ab")).parse("abc", mode))
        assertEquals("longest", Parsers.longer(isChar('a'), isChar('b')).toString())
    }

    @Test fun testShorter() {
        assertEquals('a' as Any,
                            Parsers.shorter(isChar('a'), areChars("ab")).followedBy(Scanners.isChar('b')).parse("ab", mode))
        assertEquals('a' as Any,
                            Parsers.shorter(areChars("ab"), isChar('a')).followedBy(Scanners.isChar('b')).parse("ab", mode))
        assertEquals('b' as Any,
                            Parsers.shorter(areChars("ab"), areChars("abc")).followedBy(Scanners.isChar('c')).parse("abc", mode))
        assertEquals('b' as Any,
                            Parsers.shorter(areChars("abc"), areChars("ab")).followedBy(Scanners.isChar('c')).parse("abc", mode))
        assertEquals("shortest", Parsers.shorter(isChar('a'), isChar('b')).toString())
    }

    @Test fun testLongest_0Parser() {
        assertSame(Parsers.never<Any>(), Parsers.longest<Any>())
    }

    @Test fun testLongest_1Parser() {
        val parser: Parser<*> = Parsers.constant(1)
        assertSame(parser, Parsers.longest(parser))
    }

    @Test fun testLongest() {
        assertEquals('b' as Any, Parsers.longest(isChar('a'), isChar('b'), areChars("ab")).parse("ab", mode))
        assertEquals('b' as Any, Parsers.longest(areChars("ab"), isChar('a')).parse("ab", mode))
        assertEquals('c' as Any, Parsers.longest(areChars("ab"), areChars("abc")).parse("abc", mode))
        assertEquals('c' as Any, Parsers.longest(areChars("abc"), areChars("ab")).parse("abc", mode))
        assertEquals('c' as Any, Parsers.longest(Arrays.asList(areChars("abc"), areChars("ab"))).parse("abc", mode))
        assertEquals("longest", Parsers.longest(isChar('a'), isChar('b')).toString())
    }

    @Test fun testShortest_0Parser() {
        assertSame(Parsers.never<Any>(), Parsers.shortest<Any>())
    }

    @Test fun testShortest_1Parser() {
        val parser: Parser<*> = Parsers.constant(1)
        assertSame(parser, Parsers.shortest(parser))
    }

    @Test fun testShortest() {
        assertEquals('a' as Any, Parsers.shortest(isChar('a'), areChars("ab")).followedBy(Scanners.isChar('b')).parse("ab", mode))
        assertEquals('a' as Any, Parsers.shortest(areChars("ab"), isChar('a')).followedBy(Scanners.isChar('b')).parse("ab", mode))
        assertEquals('b' as Any, Parsers.shortest(areChars("ab"), areChars("abc")).followedBy(Scanners.isChar('c')).parse("abc", mode))
        assertEquals('b' as Any, Parsers.shortest(areChars("abc"), areChars("ab")).followedBy(Scanners.isChar('c')).parse("abc", mode))
        assertEquals('b' as Any, Parsers.shortest(Arrays.asList(areChars("abc"), areChars("ab")))
            .followedBy(Scanners.isChar('c')).parse("abc", mode))
        assertEquals("shortest", Parsers.shortest(isChar('a'), isChar('b')).toString())
    }

    @Test fun testExpect() {
        assertFailure(mode, Parsers.expect<Any>("foo"), "", 1, 1, "foo expected")
        assertEquals("foo", Parsers.expect<Any>("foo").toString())
    }

    @Test fun testUnexpected() {
        assertFailure(mode, Parsers.unexpected<Any>("foo"), "", 1, 1, "unexpected foo")
        assertEquals("foo", Parsers.unexpected<Any>("foo").toString())
    }

    @Mock
    var map2: BiFunction<Char, Char, Int> = BiFunction { _, _ -> 0 }

    @Test fun testSequence_withMap2() {
        EasyMock.expect(map2.apply('a', 'b')).andReturn(1)
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), map2)
        assertEquals(map2.toString(), parser.toString())
        assertEquals(1 as Any, parser.parse("ab", mode))
    }

    @Test fun testSequence_withMap2_fails() {
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), map2)
        assertFailure(mode, parser, "xb", 1, 1)
        assertFailure(mode, parser, "ax", 1, 2)
    }

    @Mock
    var map3: Map3<Char, Char, Char, Int> =  Map3 { _, _, _ -> 0 }
    @Test fun testSequence_withMap3() {
        EasyMock.expect(map3.map('a', 'b', 'c')).andReturn(1)
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), map3)
        assertEquals(map3.toString(), parser.toString())
        assertEquals(1 as Any, parser.parse("abc", mode))
    }

    @Test fun testSequence_withMap3_fails() {
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), map3)
        assertFailure(mode, parser, "xbc", 1, 1)
        assertFailure(mode, parser, "axc", 1, 2)
        assertFailure(mode, parser, "abx", 1, 3)
    }

    @Mock
    var map4: Map4<Char, Char, Char, Char, Int> = Map4 { _, _, _, _ -> 0 }
    @Test fun testSequence_withMap4() {
        EasyMock.expect(map4.map('a', 'b', 'c', 'd')).andReturn(1)
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), map4)
        assertEquals(map4.toString(), parser.toString())
        assertEquals(1 as Any, parser.parse("abcd", mode))
    }

    @Test fun testSequence_withMap4_fails() {
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), map4)
        assertFailure(mode, parser, "xbcd", 1, 1)
        assertFailure(mode, parser, "axcd", 1, 2)
        assertFailure(mode, parser, "abxd", 1, 3)
        assertFailure(mode, parser, "abcx", 1, 4)
    }

    @Mock
    var map5: Map5<Char, Char, Char, Char, Char, Int> = Map5 { _, _, _, _, _ -> 0 }
    @Test fun testSequence_withMap5() {
        EasyMock.expect(map5.map('a', 'b', 'c', 'd', 'e')).andReturn(1)
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), map5)
        assertEquals(map5.toString(), parser.toString())
        assertEquals(1 as Any, parser.parse("abcde", mode))
    }

    @Mock
    var map6: Map6<Char, Char, Char, Char, Char, Char, Int> = Map6 { _, _, _, _, _, _ -> 0 }
    @Test fun testSequence_withMap6() {
        EasyMock.expect(map6.map('a', 'b', 'c', 'd', 'e', 'f')).andReturn(1)
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), map6)
        assertEquals(map6.toString(), parser.toString())
        assertEquals(1 as Any, parser.parse("abcdef", mode))
    }

    @Mock
    var map7: Map7<Char, Char, Char, Char, Char, Char, Char, Int> = Map7 { _, _, _, _, _, _, _ -> 0 }
    @Test fun testSequence_withMap7() {
        EasyMock.expect(map7.map('a', 'b', 'c', 'd', 'e', 'f', 'g')).andReturn(1)
        replay()
        val parser = Parsers.sequence(
            isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'), map7)
        assertEquals(map7.toString(), parser.toString())
        assertEquals(1 as Any, parser.parse("abcdefg", mode))
    }

    @Mock
    var map8: Map8<Char, Char, Char, Char, Char, Char, Char, Char, Int> = Map8 { _, _, _, _, _, _, _, _ -> 0 }
    @Test fun testSequence_withMap8() {
        EasyMock.expect(map8.map('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')).andReturn(1)
        replay()
        val parser = Parsers.sequence(
            isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), isChar('f'), isChar('g'), isChar('h'), map8)
        assertEquals(map8.toString(), parser.toString())
        assertEquals(1 as Any, parser.parse("abcdefgh", mode))
    }

    @Test fun testSequence_withMap5_fails() {
        replay()
        val parser = Parsers.sequence(isChar('a'), isChar('b'), isChar('c'), isChar('d'), isChar('e'), map5)
        assertFailure(mode, parser, "xbcde", 1, 1)
        assertFailure(mode, parser, "axcde", 1, 2)
        assertFailure(mode, parser, "abxde", 1, 3)
        assertFailure(mode, parser, "abcxe", 1, 4)
        assertFailure(mode, parser, "abcdx", 1, 5)
    }

    @Mock
    var fromToken: TokenMap<Int> = TokenMap { error("") }

    @Test fun testToken() {
        val token = Token(1, 1, 'a')
        EasyMock.expect(fromToken.map(token)).andReturn(2)
        replay()
        val parser = Parsers.token(fromToken)
        assertEquals(fromToken.toString(), parser.toString())
        assertEquals(2 as Any, parser.from(Parsers.constant(token).times(1)).parse("", mode))
    }

    @Test fun testToken_fails() {
        val token = Token(1, 1, 'a')
        EasyMock.expect(fromToken.map(token)).andReturn(null)
        replay()
        assertFailure(mode,
                      Parsers.token(fromToken).from(Parsers.constant(token).times(1)),
                      "n", 1, 2)
    }

    @Test fun testTokenType() {
        val token = Token(0, 1, 'a')
        val parser = Parsers.tokenType(Character::class.java, "character")
        assertEquals("character", parser.toString())
        assertEquals(Character.valueOf('a'), parser.from(Parsers.constant(token).times(1)).parse("", mode))
    }

    @Test fun testAnyToken() {
        assertEquals("any token", Parsers.ANY_TOKEN.toString())
        val token = Token(0, 1, 'a')
        assertEquals(Character.valueOf('a'),
                            Parsers.ANY_TOKEN.from(Parsers.constant(token).times(1)).parse("", mode))
        assertFailure(mode,
                      Parsers.ANY_TOKEN.from(Parsers.constant(token).times(0)), "", 1, 1)
    }

    @Test fun testIndex() {
        assertEquals(1 as Any, isChar('a').next(Parsers.INDEX).parse("a", mode))
        assertEquals("getIndex", Parsers.INDEX.toString())
    }

    @Test fun testSourceLocation() {
        assertEquals(1, isChar('a').next(Parsers.SOURCE_LOCATION).parse("a", mode).index.toLong())
        assertEquals(1, isChar('a').next(Parsers.SOURCE_LOCATION).parse("a", mode).line.toLong())
        assertEquals(2, isChar('a').next(Parsers.SOURCE_LOCATION).parse("a", mode).column.toLong())
    }

    @Test fun testSourceLocation_multipleLines() {
        val location = Parsers.between(Scanners.string("ab\ncd\ne"), Parsers.SOURCE_LOCATION, Scanners.string("f\ngh"))
            .parse("ab\ncd\nef\ngh")
        assertEquals(3, location.line.toLong())
        assertEquals(2, location.column.toLong())

        // Idempotent
        assertEquals(3, location.line.toLong())
        assertEquals(2, location.column.toLong())
    }

    @Test fun testSourceLocation_nested() {
        val parser = Parsers.ANY_TOKEN.many().next(Parsers.SOURCE_LOCATION)
            .from(Scanners.IDENTIFIER.token().lexer(Scanners.WHITESPACES))
        val location = parser.parse("ab\ncd\nef\ngh")
        assertEquals(4, location.line.toLong())
        assertEquals(3, location.column.toLong())

        // Idempotent
        assertEquals(4, location.line.toLong())
        assertEquals(3, location.column.toLong())
    }

    @Test fun testToArray() {
        val p1 = Parsers.constant(1)
        val p2 = Parsers.constant(2)
        val array = Parsers.toArray(Arrays.asList(p1, p2))
        assertEquals(2, array.size.toLong())
        assertSame(p1, array[0])
        assertSame(p2, array[1])
    }

    @Test fun testToArrayWithIteration() {
        val p1 = Parsers.constant(1)
        val p2 = Parsers.constant(2)
        val array = Parsers.toArrayWithIteration(Arrays.asList(p1, p2))
        assertEquals(2, array.size.toLong())
        assertSame(p1, array[0])
        assertSame(p2, array[1])
    }

    companion object {
        @JvmStatic @Parameterized.Parameters fun data(): Collection<Array<Any>> {
            return listOf(arrayOf(Parser.Mode.PRODUCTION), arrayOf(Parser.Mode.DEBUG))
        }
    }

}