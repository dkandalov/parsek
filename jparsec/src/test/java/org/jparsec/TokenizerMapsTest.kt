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

import org.jparsec.Tokens.Fragment
import org.junit.Assert
import org.junit.Test
import java.util.function.Function

/**
 * Unit test for [TokenizerMaps].
 *
 * @author Ben Yu
 */
class TokenizerMapsTest {
    @Test fun testFragment() {
        assertFragment("foo", TokenizerMaps.fragment("foo"))
    }

    @Test fun testReservedFragment() {
        assertFragment(Tokens.Tag.RESERVED, TokenizerMaps.RESERVED_FRAGMENT)
    }

    @Test fun testIdentifierFragment() {
        assertFragment(Tokens.Tag.IDENTIFIER, TokenizerMaps.IDENTIFIER_FRAGMENT)
    }

    @Test fun testIntegerFragment() {
        assertFragment(Tokens.Tag.INTEGER, TokenizerMaps.INTEGER_FRAGMENT)
    }

    @Test fun testDecimalFragment() {
        assertFragment(Tokens.Tag.DECIMAL, TokenizerMaps.DECIMAL_FRAGMENT)
    }

    @Test fun testSingleQuoteChar() {
        Assert.assertEquals("SINGLE_QUOTE_CHAR", TokenizerMaps.SINGLE_QUOTE_CHAR.toString())
        Assert.assertEquals(Character.valueOf('a'), TokenizerMaps.SINGLE_QUOTE_CHAR.apply("'a'"))
        Assert.assertEquals(Character.valueOf('a'), TokenizerMaps.SINGLE_QUOTE_CHAR.apply("'\\a'"))
        try {
            TokenizerMaps.SINGLE_QUOTE_CHAR.apply("'abc'")
            Assert.fail()
        } catch (e: IllegalStateException) {
        }
    }

    @Test fun testDecAsLong() {
        Assert.assertEquals("DEC_AS_LONG", TokenizerMaps.DEC_AS_LONG.toString())
        Assert.assertEquals(java.lang.Long.valueOf(123L), TokenizerMaps.DEC_AS_LONG.apply("123"))
    }

    @Test fun testOctAsLong() {
        Assert.assertEquals("OCT_AS_LONG", TokenizerMaps.OCT_AS_LONG.toString())
        Assert.assertEquals(java.lang.Long.valueOf(10L), TokenizerMaps.OCT_AS_LONG.apply("012"))
    }

    @Test fun testHexAsLong() {
        Assert.assertEquals("HEX_AS_LONG", TokenizerMaps.HEX_AS_LONG.toString())
        Assert.assertEquals(java.lang.Long.valueOf(255L), TokenizerMaps.HEX_AS_LONG.apply("0xff"))
    }

    @Test fun testDoubleQuoteString() {
        Assert.assertEquals("DOUBLE_QUOTE_STRING", TokenizerMaps.DOUBLE_QUOTE_STRING.toString())
        Assert.assertEquals("c:\\home", TokenizerMaps.DOUBLE_QUOTE_STRING.apply("\"c:\\\\home\""))
    }

    @Test fun testSingleQuoteString() {
        Assert.assertEquals("SINGLE_QUOTE_STRING", TokenizerMaps.SINGLE_QUOTE_STRING.toString())
        Assert.assertEquals("'a'", TokenizerMaps.SINGLE_QUOTE_STRING.apply("'''a'''"))
    }

    @Test fun testScientificNotation() {
        Assert.assertEquals("SCIENTIFIC_NOTATION", TokenizerMaps.SCIENTIFIC_NOTATION.toString())
        Assert.assertEquals(Tokens.scientificNotation("1", "2"),
                            TokenizerMaps.SCIENTIFIC_NOTATION.apply("1e2"))
        Assert.assertEquals(Tokens.scientificNotation("1", "2"),
                            TokenizerMaps.SCIENTIFIC_NOTATION.apply("1e+2"))
        Assert.assertEquals(Tokens.scientificNotation("1", "-2"),
                            TokenizerMaps.SCIENTIFIC_NOTATION.apply("1e-2"))
        Assert.assertEquals(Tokens.scientificNotation("1.2", "30"),
                            TokenizerMaps.SCIENTIFIC_NOTATION.apply("1.2E30"))
        Assert.assertEquals(Tokens.scientificNotation("0", "0"),
                            TokenizerMaps.SCIENTIFIC_NOTATION.apply("0E0"))
    }

    companion object {
        private fun assertFragment(tag: Any, map: Function<String, Fragment>) {
            val fragment = map.apply("foo")
            Assert.assertEquals(tag, fragment.tag())
            Assert.assertEquals("foo", fragment.text())
            Assert.assertEquals(tag.toString(), map.toString())
        }
    }
}