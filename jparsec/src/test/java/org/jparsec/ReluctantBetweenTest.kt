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

import org.jparsec.functors.Pair
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

/**
 *
 * @author michael
 */
@RunWith(Parameterized::class)
class ReluctantBetweenTest(private val mode: Parser.Mode) {
    @Test fun parsing_input_with_delimiting_character_inside_delimiters() {
        val relunctant: Parser<*> = Parsers.tuple(
            Scanners.IDENTIFIER.followedBy(Scanners.isChar(':')),
            Scanners.ANY_CHAR.many().source())
        Assert.assertEquals(Pair("hello", "world)"),
                            relunctant.reluctantBetween(Scanners.isChar('('), Scanners.isChar(')')).parse("(hello:world))", mode))
    }

    @Test fun parsing_simple_input() {
        Assert.assertEquals("hello",
                            Scanners.IDENTIFIER.many().source().reluctantBetween(Scanners.isChar('('), Scanners.isChar(')'))
                                .followedBy(Scanners.ANY_CHAR.skipMany()).parse("(hello)and the rest", mode))
        Assert.assertEquals("hello",
                            Scanners.IDENTIFIER.many().source().reluctantBetween(Scanners.isChar('('), Scanners.isChar(')')).parse("(hello)", mode))
        Assert.assertEquals("hello",
                            Scanners.IDENTIFIER.many().source().reluctantBetween(Scanners.isChar('('), Scanners.isChar(')').optional(Unit))
                                .parse("(hello", mode))
        Assert.assertEquals("",
                            Scanners.IDENTIFIER.many().source().reluctantBetween(Scanners.isChar('('), Scanners.isChar(')')).parse("()", mode))
    }

    @Test fun parsing_incorrect_input() {
        Asserts.assertFailure(mode,
                              Scanners.IDENTIFIER.many().source().reluctantBetween(Scanners.isChar('('), Scanners.isChar(')')),
                              "(hello", 1, 7)
    }

    companion object {
        @JvmStatic @Parameterized.Parameters fun data(): Collection<Array<Any>> {
            return Arrays.asList(arrayOf(Parser.Mode.PRODUCTION), arrayOf(Parser.Mode.DEBUG))
        }
    }

}