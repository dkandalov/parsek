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

import org.junit.Assert
import org.junit.Test
import java.lang.String
import java.util.*

/**
 * Unit test for [Keywords].
 *
 * @author Ben Yu
 */
class KeywordsTest {
    @Test fun testLexicon_caseSensitive() {
        val keywords = Arrays.asList("foo", "Bar")
        val lexicon = Keywords.lexicon(
            Scanners.IDENTIFIER, keywords, StringCase.CASE_SENSITIVE, TokenizerMaps.IDENTIFIER_FRAGMENT)
        for (keyword in keywords) {
            Assert.assertEquals(Tokens.reserved(keyword), lexicon.word(keyword))
        }
        for (keyword in keywords) {
            Assert.assertEquals(Tokens.reserved(keyword), lexicon.tokenizer.parse(keyword))
        }
        Assert.assertEquals(Tokens.identifier("FOO"), lexicon.tokenizer.parse("FOO"))
        Assert.assertEquals(Tokens.identifier("baz"), lexicon.tokenizer.parse("baz"))
    }

    @Test fun testLexicon_caseInsensitive() {
        val keywords = Arrays.asList("foo", "Bar")
        val lexicon = Keywords.lexicon(
            Scanners.IDENTIFIER, keywords, StringCase.CASE_INSENSITIVE, TokenizerMaps.IDENTIFIER_FRAGMENT)
        for (keyword in keywords) {
            Assert.assertEquals(Tokens.reserved(keyword), lexicon.word(keyword))
            Assert.assertEquals(Tokens.reserved(keyword), lexicon.word(keyword.toUpperCase()))
        }
        for (keyword in keywords) {
            Assert.assertEquals(Tokens.reserved(keyword), lexicon.tokenizer.parse(keyword))
            Assert.assertEquals(Tokens.reserved(keyword), lexicon.tokenizer.parse(keyword.toUpperCase()))
        }
        Assert.assertEquals(Tokens.identifier("baz"), lexicon.tokenizer.parse("baz"))
    }

    @Test fun testUnique() {
        Asserts.assertArrayEquals(
            Keywords.unique(String.CASE_INSENSITIVE_ORDER, "foo", "Foo", "foo", "bar"),
            "bar", "foo")
    }
}