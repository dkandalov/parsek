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
import java.util.function.Function

/**
 * Unit test for [Lexicon].
 *
 * @author Ben Yu
 */
class LexiconTest {
    @Test fun testWord() {
        val tokenizer: Parser<*> = Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER
        val lexicon = Lexicon(Function { _: String? -> "foo" }, tokenizer)
        Assert.assertSame(tokenizer, lexicon.tokenizer)
        Assert.assertEquals("foo", lexicon.word("whatever"))
    }

    @Test fun testWord_throwsForNullValue() {
        val tokenizer: Parser<*> = Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER
        val lexicon = Lexicon(Function { _: String -> null }, tokenizer)
        Assert.assertSame(tokenizer, lexicon.tokenizer)
        try {
            lexicon.word("whatever")
            Assert.fail()
        } catch (e: IllegalArgumentException) {
        }
    }
}