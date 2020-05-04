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
package org.jparsec.examples.bnf.parser

import org.jparsec.Parser
import org.jparsec.examples.bnf.ast.*
import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [RuleParser].
 *
 * @author benyu
 */
class RuleParserTest {
    @Test fun testLiteral() {
        assertParser(RuleParser.LITERAL, "'foo'", LiteralRule::class.java, "'foo'")
        assertParser(RuleParser.LITERAL, "\"bar\"", LiteralRule::class.java, "'bar'")
        assertParser(RuleParser.LITERAL, "'\"'", LiteralRule::class.java, "'\"'")
        assertParser(RuleParser.LITERAL, "\"'\"", LiteralRule::class.java, "'''")
    }

    @Test fun testIdent() {
        assertParser(RuleParser.IDENT, "foo", RuleReference::class.java, "foo")
    }

    @Test fun testRule() {
        val parser = RuleParser.rule()
        assertParser(parser, "foo", RuleReference::class.java, "foo")
        assertParser(parser, "'foo'", LiteralRule::class.java, "'foo'")
        assertParser(parser, "foo bar", SequentialRule::class.java, "foo bar")
        assertParser(parser, "foo bar | baz |'foo'", AltRule::class.java, "(foo bar | baz | 'foo')")
        assertParser(parser, "foo bar | \n  baz |'foo'", AltRule::class.java, "(foo bar | (baz | 'foo'))")
        assertParser(parser, "foo bar  baz |'foo'", AltRule::class.java, "(foo bar baz | 'foo')")
        assertParser(parser, "foo bar \n  baz |'foo'", SequentialRule::class.java, "foo bar (baz | 'foo')")
        assertParser(parser, "foo bar (baz |'foo')", SequentialRule::class.java, "foo bar (baz | 'foo')")
        assertParser(parser, "foo \n| bar", AltRule::class.java, "(foo | bar)")
        assertParser(parser, "foo | \n  bar | baz \n| 'foo'",
                     AltRule::class.java, "(foo | (bar | baz) | 'foo')")
    }

    @Test fun testRuleDef() {
        val parser = RuleParser.RULE_DEF
        assertParser(parser, "foo ::= bar | 'baz' \n  'a' | 'b'",
                     RuleDef::class.java, "foo ::= (bar | 'baz' ('a' | 'b'))")
    }

    @Test fun testRuleDefs() {
        val parser = RuleParser.RULE_DEFS
        assertParser(parser, "foo ::= bar \n baz | 'baz' \n\n #line comment \nbar::='bar'",
                     MutableList::class.java, "[foo ::= bar (baz | 'baz'), bar ::= 'bar']")
    }

    companion object {
        private fun assertParser(parser: Parser<*>, source: String, expectedClass: Class<*>, string: String) {
            val result = TerminalParser.parse(parser, source)!!
            Assert.assertTrue(expectedClass.isInstance(result))
            Assert.assertEquals(string, result.toString())
        }
    }
}