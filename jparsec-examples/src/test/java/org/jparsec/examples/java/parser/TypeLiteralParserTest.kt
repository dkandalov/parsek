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
package org.jparsec.examples.java.parser

import org.jparsec.examples.java.ast.type.ArrayTypeLiteral
import org.jparsec.examples.java.ast.type.SimpleTypeLiteral
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertFailure
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertResult
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertToString
import org.junit.Test
import java.util.*

/**
 * Unit test for [@link TypeLiteralParser}.
 *
 * @author Ben Yu
 */
class TypeLiteralParserTest {
    @Test fun testArrayOf() {
        val intType = SimpleTypeLiteral(Arrays.asList("int"), TypeLiteralParser.EMPTY_TYPE_ARGUMENT_LIST)
        assertToString(ArrayTypeLiteral::class.java, "int[]",
                                  TerminalParser.parse(TypeLiteralParser.ARRAY_OF, "[]").apply(intType))
    }

    @Test fun testTypeLiteral() {
        val parser = TypeLiteralParser.TYPE_LITERAL
        assertResult(parser, "int", SimpleTypeLiteral::class.java, "int")
        assertResult(parser, "a.b.c", SimpleTypeLiteral::class.java, "a.b.c")
        assertResult(parser, "java.util.Map<K, V>", SimpleTypeLiteral::class.java, "java.util.Map<K, V>")
        assertResult(parser, "Pair<A, Pair<A,B>>", SimpleTypeLiteral::class.java, "Pair<A, Pair<A, B>>")
        assertResult(parser, "Pair<?, ?>", SimpleTypeLiteral::class.java, "Pair<?, ?>")
        assertResult(parser, "List<? extends List<?>>",
                                        SimpleTypeLiteral::class.java, "List<? extends List<?>>")
        assertFailure(parser, "?", 1, 1)
        assertFailure(parser, "List<? extends ?>", 1, 16)
        assertResult(parser, "Pair<? extends A, ? super B>",
                                        SimpleTypeLiteral::class.java, "Pair<? extends A, ? super B>")
        assertResult(parser, "int[]", ArrayTypeLiteral::class.java, "int[]")
        assertResult(parser, "Pair<A, Pair<A,B>>[]", ArrayTypeLiteral::class.java, "Pair<A, Pair<A, B>>[]")
        assertResult(parser, "int[][]", ArrayTypeLiteral::class.java, "int[][]")
    }

    @Test fun testElementTypeLiteral() {
        val parser = TypeLiteralParser.ELEMENT_TYPE_LITERAL
        assertResult(parser, "int", SimpleTypeLiteral::class.java, "int")
        assertFailure(parser, "int[]", 1, 4)
    }

    @Test fun testArrayTypeLiteral() {
        val parser = TypeLiteralParser.ARRAY_TYPE_LITERAL
        assertResult(parser, "int[]", ArrayTypeLiteral::class.java, "int[]")
        assertFailure(parser, "int", 1, 4)
    }
}