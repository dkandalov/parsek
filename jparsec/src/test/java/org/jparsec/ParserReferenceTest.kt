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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

/**
 * Unit test for [Parser.Reference].
 *
 * @author Ben Yu
 */
@RunWith(Parameterized::class)
class ParserReferenceTest(private val mode: Parser.Mode) {
    @Test fun testLazy() {
        val ref = Parser.newReference<String>()
        assertNull(ref.get())
        val lazyParser = ref.lazy()
        assertEquals("lazy", lazyParser.toString())
        ref.set(Parsers.constant("foo"))
        assertEquals("foo", lazyParser.parse(""))
        ref.set(Parsers.constant("bar"))
        assertEquals("bar", lazyParser.parse(""))
    }

    @Test fun testUninitializedLazy() {
        val ref = Parser.newReference<String>()
        assertNull(ref.get())
        assertFailure(mode, ref.lazy(), "", 1, 1, "Uninitialized lazy parser reference")
    }

    companion object {
        @JvmStatic @Parameterized.Parameters fun data(): Collection<Array<Any>> {
            return Arrays.asList(arrayOf(Parser.Mode.PRODUCTION), arrayOf(Parser.Mode.DEBUG))
        }
    }

}