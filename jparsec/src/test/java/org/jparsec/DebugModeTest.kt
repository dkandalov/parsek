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

import org.jparsec.error.ParserException
import org.jparsec.pattern.CharPredicates
import org.junit.Assert
import org.junit.Test
import java.util.*

class DebugModeTest {
    @Test fun runtimeExceptionPopulatesErrorParseTree() {
        val parser: Parser<*> = Scanners.string("hello").source().label("word")
            .map<Any> { from: String? -> throw RuntimeException("intentional") }.label("throws")
        try {
            parser.parse("hello", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("hello", node("throws", null, "hello", stringNode("word", "hello"))),
                            e.parseTree)
        }
    }

    @Test fun emptyParseTreeInParserException() {
        try {
            Scanners.string("hello").parse("", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode(""), e.parseTree)
        }
    }

    @Test fun nonLabeledParserDoesNotPopulateErrorParseTree() {
        try {
            Scanners.string("hello").source().parse("hello world", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("hello"), e.parseTree)
        }
    }

    @Test fun nonLabeledParserDoesNotPopulateParseTree() {
        val tree = Scanners.string("hello").source().parseTree("hello")
        assertParseTree(rootNode("hello"), tree)
    }

    @Test fun partialMatchDoesNotPopulateErrorParseTree() {
        try {
            val parser: Parser<*> = Scanners.string("hello ").source().label("hi")
            parser.parse("hello", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode(""), e.parseTree)
        }
    }

    @Test fun explicitLabelPopulatesErrorParseTree() {
        try {
            Scanners.string("hello").source().label("hi").parse("hello world", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("hello", stringNode("hi", "hello")), e.parseTree)
        }
    }

    @Test fun explicitLabelPopulatesParseTree() {
        val tree = Scanners.string("hello").source().label("hi").parseTree("hello")
        assertParseTree(rootNode("hello", stringNode("hi", "hello")), tree)
    }

    @Test fun twoChildrenNodesInErrorParseTree() {
        val parser: Parser<*> = Parsers.sequence(
            Scanners.string("hello").source().label("hi"),
            Scanners.string("world").source().label("you"))
        try {
            parser.parse("helloworld ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("helloworld", stringNode("hi", "hello"), stringNode("you", "world")),
                            e.parseTree)
        }
    }

    @Test fun twoChildrenNodesInParseTree() {
        val parser: Parser<*> = Parsers.sequence(
            Scanners.string("hello").source().label("hi"),
            Scanners.string("world").source().label("you"))
        val tree = parser.parseTree("helloworld")
        assertParseTree(rootNode("helloworld", stringNode("hi", "hello"), stringNode("you", "world")),
                        tree)
    }

    @Test fun grandChildInOrParserPopulatesErrorParseTree() {
        val parser: Parser<*> = Parsers.or(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        try {
            parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("helloworld",
                         stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
                e.parseTree)
        }
    }

    @Test fun grandChildInOrParserPopulatesParseTree() {
        val parser: Parser<*> = Parsers.or(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        val tree = parser.label("greeting").parseTree("helloworld")
        assertParseTree(
            rootNode("helloworld",
                     stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
            tree)
    }

    @Test fun longerGrandChildMatchesInLongerParserPopulatesErrorParseTree() {
        val parser: Parser<*> = Parsers.longer(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        try {
            parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("helloworld",
                         stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
                e.parseTree)
        }
    }

    @Test fun longerGrandChildMatchesInLongerParserPopulatesParseTree() {
        val parser: Parser<*> = Parsers.longer(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        val tree = parser.label("greeting").parseTree("helloworld")
        assertParseTree(
            rootNode("helloworld",
                     stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
            tree)
    }

    @Test fun shorterGrandChildMatchesInLongerParserPopulatesErrorParseTree() {
        val parser: Parser<*> = Parsers.longer(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        try {
            parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("helloworld",
                         stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
                e.parseTree)
        }
    }

    @Test fun shorterGrandChildMatchesInLongerParserPopulatesParseTree() {
        val parser: Parser<*> = Parsers.longer(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        val tree = parser.label("greeting").parseTree("helloworld")
        assertParseTree(
            rootNode("helloworld",
                     stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
            tree)
    }

    @Test fun bothGrandChildrenMatchInLongerParserPopulatesErrorParseTree() {
        val parser: Parser<*> = Parsers.longer(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string(" world")).source().label("hi you"))
        try {
            parser.label("greeting").parse("hello world x", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("hello world",
                         stringNode("greeting", "hello world", stringNode("hi you", "hello world"))),
                e.parseTree)
        }
    }

    @Test fun bothGrandChildrenMatchInLongerParserPopulatesParseTree() {
        val parser: Parser<*> = Parsers.longer(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string(" world")).source().label("hi you"))
        val tree = parser.label("greeting").parseTree("hello world")
        assertParseTree(
            rootNode("hello world",
                     stringNode("greeting", "hello world", stringNode("hi you", "hello world"))),
            tree)
    }

    @Test fun longerGrandChildMatchesInShorterParserPopulatesErrorParseTree() {
        val parser: Parser<*> = Parsers.shorter(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        try {
            parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("helloworld",
                         stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
                e.parseTree)
        }
    }

    @Test fun longerGrandChildMatchesInShorterParserPopulatesParseTree() {
        val parser: Parser<*> = Parsers.shorter(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        val tree = parser.label("greeting").parseTree("helloworld")
        assertParseTree(
            rootNode("helloworld",
                     stringNode("greeting", "helloworld", stringNode("hi you", "helloworld"))),
            tree)
    }

    @Test fun failedOptionalAttemptDoesNotPopulateErrorParseTree() {
        val parser: Parser<*> = Scanners.string("hello ").label("hi").asOptional().source()
        try {
            parser.parse("helloworld", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode(""), e.parseTree)
        }
    }

    @Test fun succeededOptionalAttemptDoesNotPopulateParseTree() {
        val parser: Parser<*> = Scanners.string("hello").label("hi").optional(Unit).source()
        val tree = parser.parseTree("hello")
        assertParseTree(rootNode("hello", node("hi", null, "hello")), tree)
    }

    @Test fun succeededAsOptionalAttemptDoesNotPopulateParseTree() {
        val parser: Parser<*> = Scanners.string("hello").label("hi").source().asOptional()
        val tree = parser.parseTree("hello")
        assertParseTree(rootNode("hello", node("hi", null, "hello")), tree)
    }

    @Test fun shorterGrandChildMatchesInShorterParserPopulatesErrorParseTree() {
        val parser: Parser<*> = Parsers.shorter(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        try {
            parser.label("greeting").parse("hello world", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("hello ",
                         stringNode("greeting", "hello ", stringNode("hi", "hello "))),
                e.parseTree)
        }
    }

    @Test fun shorterGrandChildMatchesInShorterParserPopulatesParseTree() {
        val parser: Parser<*> = Parsers.shorter(
            Scanners.string("hello ").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        val tree = parser.followedBy(Scanners.ANY_CHAR.skipMany()).label("greeting")
            .parseTree("hello world")
        assertParseTree(
            rootNode("hello world",
                     node("greeting", "hello ", "hello world", stringNode("hi", "hello "))),
            tree)
    }

    @Test fun bothGrandChildrenMatchInShorterParserPopulatesErrorParseTree() {
        val parser: Parser<*> = Parsers.shorter(
            Scanners.string("hello").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        try {
            parser.label("greeting").parse("helloworldx", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("hello",
                         stringNode("greeting", "hello", stringNode("hi", "hello"))),
                e.parseTree)
        }
    }

    @Test fun bothGrandChildrenMatchInShorterParserPopulatesParseTree() {
        val parser: Parser<*> = Parsers.shorter(
            Scanners.string("hello").source().label("hi"),
            Scanners.string("hello").next(Scanners.string("world")).source().label("hi you"))
        val tree = parser.label("greeting").followedBy(Scanners.ANY_CHAR.skipMany())
            .parseTree("helloworld")
        assertParseTree(
            rootNode("helloworld",
                     stringNode("greeting", "hello", stringNode("hi", "hello"))),
            tree)
    }

    @Test fun ifElseParserWithTrueBranchFailedPopulatesErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar('@').label("?")
            .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"))
        try {
            parser.label("id").parse("@abc", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("@", node("id", null, "@", node("?", null, "@"))), e.parseTree)
        }
    }

    @Test fun ifElseParserWithTrueBranchSucceededPopulatesErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar('@').label("?")
            .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"))
        try {
            parser.label("id").parse("@123x", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("@123", node("id", "123", "@123", node("?", null, "@"), stringNode("tel", "123"))),
                e.parseTree)
        }
    }

    @Test fun ifElseParserWithTrueBranchSucceededPopulatesParseTree() {
        val parser: Parser<*> = Scanners.isChar('@').label("?")
            .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"))
        val tree = parser.label("id").parseTree("@123")
        assertParseTree(
            rootNode("@123", node("id", "123", "@123", node("?", null, "@"), stringNode("tel", "123"))),
            tree)
    }

    @Test fun ifElseParserWithFalseBranchFailedPopulatesErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar('@').label("?")
            .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"))
        try {
            parser.label("id").parse("123", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("", node("id", null, "")), e.parseTree)
        }
    }

    @Test fun ifElseParserWithFalseBranchSucceededPopulatesErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar('@').label("?")
            .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"))
        try {
            parser.label("id").parse("Ben ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("Ben", stringNode("id", "Ben", stringNode("name", "Ben"))),
                            e.parseTree)
        }
    }

    @Test fun ifElseParserWithFalseBranchSucceededPopulatesParseTree() {
        val parser: Parser<*> = Scanners.isChar('@').label("?")
            .ifelse(Scanners.INTEGER.label("tel"), Scanners.IDENTIFIER.label("name"))
        val tree = parser.label("id").parseTree("Ben")
        assertParseTree(rootNode("Ben", stringNode("id", "Ben", stringNode("name", "Ben"))), tree)
    }

    @Test fun endByProducesEmptyListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'))
        try {
            parser.label("digits").parse("; ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("", node("digits", listOf<String>(), "")),
                e.parseTree)
        }
    }

    @Test fun endByProducesSingleElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'))
        try {
            parser.label("digits").parse("1; ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1;", node("digits", listOf("1"), "1;", stringNode("d", "1"))),
                e.parseTree)
        }
    }

    @Test fun endByProducesTwoElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'))
        try {
            parser.label("digits").parse("1;2; ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1;2;", node("digits", listOf("1", "2"), "1;2;",
                                      stringNode("d", "1"),
                                      stringNode("d", "2").leading(1))),
                e.parseTree)
        }
    }

    @Test fun endByProducesEmptyListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'))
        val tree = parser.label("digits").parseTree("")
        assertParseTree(rootNode("", node("digits", listOf<String>(), "")), tree)
    }

    @Test fun endByProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'))
        val tree = parser.label("digits").parseTree("1;")
        assertParseTree(
            rootNode("1;", node("digits", listOf("1"), "1;", stringNode("d", "1"))),
            tree)
    }

    @Test fun endByProducesTwoElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy(Scanners.isChar(';'))
        val tree = parser.label("digits").parseTree("1;2;")
        assertParseTree(
            rootNode("1;2;", node("digits", listOf("1", "2"), "1;2;",
                                  stringNode("d", "1"),
                                  stringNode("d", "2").leading(1))),
            tree)
    }

    @Test fun endBy1ProducesEmptyListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'))
        try {
            parser.label("digits").parse("; ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("", node("digits", null, "")),
                e.parseTree)
        }
    }

    @Test fun endBy1ProducesSingleElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'))
        try {
            parser.label("digits").parse("1; ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1;", node("digits", listOf("1"), "1;", stringNode("d", "1"))),
                e.parseTree)
        }
    }

    @Test fun endBy1ProducesTwoElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'))
        try {
            parser.label("digits").parse("1;2; ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1;2;", node("digits", listOf("1", "2"), "1;2;",
                                      stringNode("d", "1"),
                                      stringNode("d", "2").leading(1))),
                e.parseTree)
        }
    }

    @Test fun endBy1ProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'))
        val tree = parser.label("digits").parseTree("1;")
        assertParseTree(
            rootNode("1;", node("digits", listOf("1"), "1;", stringNode("d", "1"))),
            tree)
    }

    @Test fun endBy1ProducesTwoElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").endBy1(Scanners.isChar(';'))
        val tree = parser.label("digits").parseTree("1;2;")
        assertParseTree(
            rootNode("1;2;", node("digits", listOf("1", "2"), "1;2;",
                                  stringNode("d", "1"),
                                  stringNode("d", "2").leading(1))),
            tree)
    }

    @Test fun sepByProducesEmptyListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','))
        try {
            parser.label("digits").parse(" ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("", node("digits", listOf<String>(), "")),
                e.parseTree)
        }
    }

    @Test fun sepByProducesSingleElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','))
        try {
            parser.label("digits").parse("1 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1", node("digits", listOf("1"), "1", stringNode("d", "1"))),
                e.parseTree)
        }
    }

    @Test fun sepByProducesTwoElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','))
        try {
            parser.label("digits").parse("1,2 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1,2", node("digits",
                                     listOf("1", "2"), "1,2",
                                     stringNode("d", "1"),
                                     stringNode("d", "2").leading(1)
                )),
                e.parseTree)
        }
    }

    @Test fun sepByProducesEmptyListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("")
        assertParseTree(
            rootNode("", node("digits", listOf<String>(), "")),
            tree)
    }

    @Test fun sepByProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("1")
        assertParseTree(
            rootNode("1", node("digits", listOf("1"), "1", stringNode("d", "1"))),
            tree)
    }

    @Test fun sepByProducesTwoElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("1,2")
        assertParseTree(
            rootNode("1,2", node("digits",
                                 listOf("1", "2"), "1,2",
                                 stringNode("d", "1"),
                                 stringNode("d", "2").leading(1)
            )),
            tree)
    }

    @Test fun sepBy1ProducesEmptyListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','))
        try {
            parser.label("digits").parse("", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("", node("digits", null, "")),
                e.parseTree)
        }
    }

    @Test fun sepBy1ProducesSingleElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','))
        try {
            parser.label("digits").parse("1 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1", node("digits", listOf("1"), "1", stringNode("d", "1"))),
                e.parseTree)
        }
    }

    @Test fun sepBy1ProducesTwoElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','))
        try {
            parser.label("digits").parse("1,2 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1,2", node("digits",
                                     listOf("1", "2"), "1,2",
                                     stringNode("d", "1"),
                                     stringNode("d", "2").leading(1)
                )),
                e.parseTree)
        }
    }

    @Test fun sepBy1ProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("1")
        assertParseTree(
            rootNode("1", node("digits", listOf("1"), "1", stringNode("d", "1"))),
            tree)
    }

    @Test fun sepBy1ProducesTwoElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepBy1(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("1,2")
        assertParseTree(
            rootNode("1,2", node("digits",
                                 listOf("1", "2"), "1,2",
                                 stringNode("d", "1"),
                                 stringNode("d", "2").leading(1)
            )),
            tree)
    }

    @Test fun sepEndBy1ProducesEmptyListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','))
        try {
            parser.label("digits").parse(" ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("", node("digits", null, "")),
                e.parseTree)
        }
    }

    @Test fun sepEndBy1ProducesSingleElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','))
        try {
            parser.label("digits").parse("1, ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1,", node("digits", listOf("1"), "1,",
                                    stringNode("d", "1"))),
                e.parseTree)
        }
    }

    @Test fun sepEndBy1ProducesListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','))
        try {
            parser.label("digits").parse("1,2,3 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1,2,3", node("digits", listOf("1", "2", "3"), "1,2,3",
                                       stringNode("d", "1"),
                                       stringNode("d", "2").leading(1),
                                       stringNode("d", "3").leading(1))),
                e.parseTree)
        }
    }

    @Test fun sepEndBy1ProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("1,")
        assertParseTree(
            rootNode("1,", node("digits", listOf("1"), "1,",
                                stringNode("d", "1"))),
            tree)
    }

    @Test fun sepEndBy1ProducesListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy1(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("1,2,3")
        assertParseTree(
            rootNode("1,2,3", node("digits", listOf("1", "2", "3"), "1,2,3",
                                   stringNode("d", "1"),
                                   stringNode("d", "2").leading(1),
                                   stringNode("d", "3").leading(1))),
            tree)
    }

    @Test fun sepEndByProducesEmptyListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','))
        try {
            parser.label("digits").parse(" ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("", node("digits", listOf<String>(), "")),
                e.parseTree)
        }
    }

    @Test fun sepEndByProducesSingleElementListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','))
        try {
            parser.label("digits").parse("1, ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1,", node("digits", listOf("1"), "1,",
                                    stringNode("d", "1"))),
                e.parseTree)
        }
    }

    @Test fun sepEndByProducesListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','))
        try {
            parser.label("digits").parse("1,2,3 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("1,2,3", node("digits", listOf("1", "2", "3"), "1,2,3",
                                       stringNode("d", "1"),
                                       stringNode("d", "2").leading(1),
                                       stringNode("d", "3").leading(1))),
                e.parseTree)
        }
    }

    @Test fun sepEndByProducesEmptyListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("")
        assertParseTree(
            rootNode("", node("digits", listOf<String>(), "")),
            tree)
    }

    @Test fun sepEndByProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("1,")
        assertParseTree(
            rootNode("1,", node("digits", listOf("1"), "1,",
                                stringNode("d", "1"))),
            tree)
    }

    @Test fun sepEndByProducesListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").sepEndBy(Scanners.isChar(','))
        val tree = parser.label("digits").parseTree("1,2,3")
        assertParseTree(
            rootNode("1,2,3", node("digits", listOf("1", "2", "3"), "1,2,3",
                                   stringNode("d", "1"),
                                   stringNode("d", "2").leading(1),
                                   stringNode("d", "3").leading(1))),
            tree)
    }

    @Test fun manyProducesEmptyListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many()
        val tree = parser.label("digits").parseTree("")
        assertParseTree(rootNode("", node("digits", listOf<String>(), "")), tree)
    }

    @Test fun manyProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many()
        val tree = parser.label("digits").parseTree("1")
        assertParseTree(
            rootNode("1", node("digits", listOf("1"), "1", stringNode("d", "1"))), tree)
    }

    @Test fun manyProducesTwoElementsListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many()
        val tree = parser.label("digits").parseTree("12")
        assertParseTree(
            rootNode("12", node("digits", listOf("1", "2"), "12",
                                stringNode("d", "1"), stringNode("d", "2"))),
            tree)
    }

    @Test fun many1ProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many1()
        val tree = parser.label("digits").parseTree("1")
        assertParseTree(
            rootNode("1", node("digits", listOf("1"), "1", stringNode("d", "1"))), tree)
    }

    @Test fun many1ProducesTwoElementsListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many1()
        val tree = parser.label("digits").parseTree("12")
        assertParseTree(
            rootNode("12", node("digits", listOf("1", "2"), "12",
                                stringNode("d", "1"), stringNode("d", "2"))),
            tree)
    }

    @Test fun atMostProducesEmptyListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0, 2)
        val tree = parser.label("digits").parseTree("")
        assertParseTree(rootNode("", node("digits", listOf<String>(), "")), tree)
    }

    @Test fun atMostProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0, 2)
        val tree = parser.label("digits").parseTree("1")
        assertParseTree(
            rootNode("1", node("digits", listOf("1"), "1", stringNode("d", "1"))), tree)
    }

    @Test fun atMostProducesTwoElementsListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0, 2)
        val tree = parser.label("digits").parseTree("12")
        assertParseTree(
            rootNode("12", node("digits", listOf("1", "2"), "12",
                                stringNode("d", "1"), stringNode("d", "2"))),
            tree)
    }

    @Test fun timesProducesEmptyListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0)
        val tree = parser.label("digits").parseTree("")
        assertParseTree(rootNode("", node("digits", listOf<String>(), "")), tree)
    }

    @Test fun timesProducesSingleElementListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(1)
        val tree = parser.label("digits").parseTree("1")
        assertParseTree(
            rootNode("1", node("digits", listOf("1"), "1", stringNode("d", "1"))), tree)
    }

    @Test fun timesProducesTwoElementsListInParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(2)
        val tree = parser.label("digits").parseTree("12")
        assertParseTree(
            rootNode("12", node("digits", listOf("1", "2"), "12",
                                stringNode("d", "1"), stringNode("d", "2"))),
            tree)
    }

    @Test fun manyProducesListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many()
        try {
            parser.label("digits").parse("123 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("123", node("digits", listOf("1", "2", "3"), "123",
                                     stringNode("d", "1"),
                                     stringNode("d", "2"),
                                     stringNode("d", "3"))),
                e.parseTree)
        }
    }

    @Test fun many1ProducesListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").many1()
        try {
            parser.label("digits").parse("123 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("123", node("digits", listOf("1", "2", "3"), "123",
                                     stringNode("d", "1"),
                                     stringNode("d", "2"),
                                     stringNode("d", "3"))),
                e.parseTree)
        }
    }

    @Test fun atLeastProducesListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").atLeast(1)
        try {
            parser.label("digits").parse("123 ", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("123", node("digits", listOf("1", "2", "3"), "123",
                                     stringNode("d", "1"),
                                     stringNode("d", "2"),
                                     stringNode("d", "3"))),
                e.parseTree)
        }
    }

    @Test fun timesProducesListInErrorParseTree() {
        val parser: Parser<*> = Scanners.isChar(CharPredicates.IS_DIGIT).source().label("d").times(0, 2)
        try {
            parser.label("digits").parse("123", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(
                rootNode("12", node("digits", listOf("1", "2"), "12",
                                    stringNode("d", "1"),
                                    stringNode("d", "2"))),
                e.parseTree)
        }
    }

    @Test fun terminalsPhrasePopulatedInErrorParseTree() {
        val terminals = Terminals.operators("if", "then")
        val parser = terminals.phrase("if", "then")
            .from(terminals.tokenizer(), Scanners.WHITESPACES)
        try {
            parser.parse("if then then", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("if then ", node("if then", "if then", "if then ")),
                            e.parseTree)
        }
    }

    @Test fun tokenLevelLabelPopulatedInErrorParseTree() {
        val terminals = Terminals.operators("if", "then")
        val parser: Parser<*> = terminals.token("if").retn(true).label("condition")
            .from(terminals.tokenizer().label("token"), Scanners.WHITESPACES)
        try {
            parser.parse("if then", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("if ", node("condition", true, "if ")),
                            e.parseTree)
        }
    }

    @Test fun unrecognizedCharactersReportedInTokenLevelErrorParseTree() {
        val terminals = Terminals.operators("if", "then")
        val parser: Parser<*> = terminals.token("if").retn(true).label("condition")
            .from(terminals.tokenizer().label("token"), Scanners.WHITESPACES)
        try {
            parser.parse("if x", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            val root = e.parseTree
            Assert.assertEquals(0, root!!.beginIndex.toLong())
            Assert.assertEquals(3, root.endIndex.toLong())
            Assert.assertEquals(1, root.children.size.toLong())
            val child = root.children[0]
            Assert.assertEquals("token", child!!.name)
            Assert.assertEquals(0, child.beginIndex.toLong())
            Assert.assertEquals(2, child.endIndex.toLong())
            Assert.assertEquals("if", child.value.toString())
        }
    }

    @Test fun errorInOuterScanner() {
        val parser: Parser<*> = Scanners.nestedScanner(
            Scanners.string("ab").label("outer1").next(Scanners.isChar('c').label("outer2"))
                .label("outer"),
            Scanners.ANY_CHAR.label("inner").skipMany())
        try {
            parser.parse("abd", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            assertParseTree(rootNode("ab", node("outer", null, "ab", node("outer1", null, "ab"))), e.parseTree)
        }
    }

    @Test fun errorInInnerScanner() {
        val parser: Parser<*> = Scanners.nestedScanner(
            Scanners.string("ab").source().label("outer1")
                .next(Scanners.isChar('c').source().label("outer2"))
                .source()
                .label("outer"),
            Scanners.string("a").source().label("inner1")
                .next(Scanners.string("bd").source().label("inner2"))
                .label("inner")
                .retn(Unit))
        try {
            parser.parse("abc", Parser.Mode.DEBUG)
            Assert.fail()
        } catch (e: ParserException) {
            val tree = e.parseTree
            Assert.assertEquals(0, tree!!.beginIndex.toLong())
            Assert.assertEquals(1, tree.endIndex.toLong())
            Assert.assertEquals(2, tree.children.size.toLong())
            assertParseTree(
                stringNode("outer", "abc",
                           stringNode("outer1", "ab"),
                           stringNode("outer2", "c")),
                tree.children[0])
            assertParseTree(
                node("inner", null, "a", stringNode("inner1", "a")),
                tree.children[1])
        }
    }

    @Test fun parseToTreeWithTreePopulatedAtTokenLevel() {
        val terms = Terminals.operators("+")
        val expr: Parser<*> = Parsers.sequence(
            Terminals.IntegerLiteral.PARSER.label("lhs"),
            terms.token("+").retn("+").label("plus"),
            Terminals.IntegerLiteral.PARSER.label("rhs"))
        val tokenizer: Parser<*> = Parsers.or(
            terms.tokenizer().label("op"), Terminals.IntegerLiteral.TOKENIZER.label("num"))
        val parser: Parser<*> = expr.source().label("expr").from(tokenizer, Scanners.WHITESPACES)
        val tree = parser.parseTree("1 + 2")
        assertParseTree(
            rootNode("1 + 2",
                     stringNode("expr", "1 + 2",
                                stringNode("lhs", "1").trailing(1),
                                stringNode("plus", "+").trailing(1),
                                stringNode("rhs", "2"))),
            tree)
    }

    @Test fun parseToTreeWithTreePopulatedAtCharacterLevel() {
        val expr: Parser<*> = Parsers.sequence(
            Scanners.INTEGER.label("lhs"),
            Scanners.isChar('+').source().label("plus"),
            Scanners.INTEGER.label("rhs"))
        val tree = expr.source().label("expr").parseTree("1+2")
        assertParseTree(
            rootNode("1+2",
                     stringNode("expr", "1+2",
                                stringNode("lhs", "1"),
                                stringNode("plus", "+"),
                                stringNode("rhs", "2"))),
            tree)
    }

    @Test fun parseToTreeWithTreePopulatedAtCharacterLevelWithNestedScanner() {
        val expr: Parser<*> = Parsers.sequence(
            Scanners.INTEGER.label("lhs"),
            Scanners.isChar('+').source().label("plus"),
            Scanners.INTEGER.label("rhs"))
        val parser: Parser<*> = Scanners.nestedScanner(expr, Scanners.ANY_CHAR.skipMany())
            .source()
            .label("expr")
        assertParseTree(
            rootNode("1+2",
                     stringNode("expr", "1+2",
                                stringNode("lhs", "1"),
                                stringNode("plus", "+"),
                                stringNode("rhs", "2"))),
            parser.parseTree("1+2"))
    }

    @Test fun parseToTreeWithEmptyTree() {
        val expr: Parser<*> = Parsers.sequence(
            Scanners.INTEGER,
            Scanners.isChar('+').source(),
            Scanners.INTEGER)
        val tree = expr.source().parseTree("1+2")
        assertParseTree(rootNode("1+2"), tree)
    }

    @Test fun parseToTreeWithCharacterLevelTreeDiscarded() {
        val terms = Terminals.operators("+")
        val expr: Parser<*> = Parsers.sequence(
            Terminals.IntegerLiteral.PARSER,
            terms.token("+").retn("+"),
            Terminals.IntegerLiteral.PARSER)
        val tokenizer: Parser<*> = Parsers.or(
            terms.tokenizer().label("op"), Terminals.IntegerLiteral.TOKENIZER.label("num"))
        val parser: Parser<*> = expr.source().from(tokenizer, Scanners.WHITESPACES)
        val tree = parser.parseTree("1 + 2")
        assertParseTree(rootNode("1 + 2"), tree)
    }

    private class MatchNode(val name: String, val value: Any?, val matched: String, val children: List<MatchNode>) {
        var leading = 0
        var trailing = 0
        fun leading(offset: Int): MatchNode {
            leading = offset
            return this
        }

        fun trailing(offset: Int): MatchNode {
            trailing = offset
            return this
        }

    }

    companion object {
        private fun assertParseTree(expected: MatchNode, actual: ParseTree?) {
            assertParseTree(0, expected, actual)
        }

        private fun assertParseTree(offset: Int, expected: MatchNode, actual: ParseTree?) {
            var offset = offset
            Assert.assertNotNull(actual)
            Assert.assertEquals(actual.toString(), expected.name, actual!!.name)
            Assert.assertEquals("beginIndex of $actual", offset + expected.leading.toLong(),
                                actual.beginIndex.toLong())
            Assert.assertEquals("endIndex of $actual",
                                offset + expected.leading + expected.matched.length + expected.trailing.toLong(),
                                actual.endIndex.toLong())
            Assert.assertEquals("value of $actual", expected.value, actual.value)
            Assert.assertEquals("children.size() of $actual", expected.children.size.toLong(),
                                actual.children.size.toLong())
            for (i in expected.children.indices) {
                val expectedChild = expected.children[i]
                assertParseTree(offset, expectedChild, actual.children[i])
                offset += expectedChild.matched.length + expectedChild.leading + expectedChild.trailing
            }
        }

        private fun stringNode(name: String, matched: String, vararg children: MatchNode): MatchNode {
            return node(name, matched, matched, *children)
        }

        private fun rootNode(matched: String, vararg children: MatchNode): MatchNode {
            return node("root", null, matched, *children)
        }

        private fun node(name: String, value: Any?, matched: String, vararg children: MatchNode): MatchNode {
            return MatchNode(name, value, matched, listOf(*children))
        }
    }
}