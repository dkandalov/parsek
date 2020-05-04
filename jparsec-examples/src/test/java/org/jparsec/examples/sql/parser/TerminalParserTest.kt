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
package org.jparsec.examples.sql.parser

import org.jparsec.Parser
import org.jparsec.error.ParserException
import org.jparsec.examples.sql.ast.QualifiedName
import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * Unit test for [TerminalParser].
 *
 * @author Ben Yu
 */
class TerminalParserTest {
    @Test fun testNumber() {
        assertParser(TerminalParser.NUMBER, "1.2 ", "1.2")
        assertParser(TerminalParser.NUMBER, " 1", "1")
    }

    @Test fun testName() {
        assertParser(TerminalParser.NAME, "foo", "foo")
        assertParser(TerminalParser.NAME, " foo\n", "foo")
        assertParser(TerminalParser.NAME, "[foo]", "foo")
        assertParser(TerminalParser.NAME, "[ foo] /*comment*/", "foo")
        assertParser(TerminalParser.NAME, "[select] --comment", "select")
        assertFailure(TerminalParser.NAME, "select", 1, 1)
    }

    @Test fun testString() {
        assertParser(TerminalParser.STRING, "'foo'", "foo")
        assertParser(TerminalParser.STRING, "'foo''s'", "foo's")
        assertParser(TerminalParser.STRING, "''", "")
    }

    @Test fun testQualifiedName() {
        assertQualifiedName("foo", "foo")
        assertQualifiedName("foo.bar", "foo", "bar")
        assertQualifiedName("foo . bar.[select]", "foo", "bar", "select")
    }

    @Test fun testPhrase() {
        TerminalParser.parse(TerminalParser.phrase("inner join"), " inner join ")
        assertFailure(TerminalParser.phrase("inner join"), "[inner] join", 1, 1)
    }

    @Test fun testTerm() {
        TerminalParser.parse(TerminalParser.term("select"), "select")
        TerminalParser.parse(TerminalParser.term("select"), "SELECT")
        TerminalParser.parse(TerminalParser.term("select"), " SELEcT --coment")
        assertFailure(TerminalParser.term("select"), "[select]", 1, 1)
    }

    companion object {
        private fun assertQualifiedName(source: String, vararg names: String) {
            val qname = TerminalParser.parse(TerminalParser.QUALIFIED_NAME, source)
            Assert.assertEquals(QualifiedName(Arrays.asList(*names)), qname)
        }

        fun assertParser(parser: Parser<*>?, source: String?, value: Any?) {
            Assert.assertEquals(value, TerminalParser.parse(parser, source))
        }

        @JvmOverloads fun assertFailure(parser: Parser<*>?, source: String?, line: Int, column: Int, errorMessage: String? = "") {
            try {
                TerminalParser.parse(parser, source)
                Assert.fail()
            } catch (e: ParserException) {
                Assert.assertTrue(e.message, e.message!!.contains(errorMessage!!))
                Assert.assertEquals(line.toLong(), e.location.line.toLong())
                Assert.assertEquals(column.toLong(), e.location.column.toLong())
            }
        }
    }
}