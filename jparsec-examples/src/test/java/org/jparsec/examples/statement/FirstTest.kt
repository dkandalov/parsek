package org.jparsec.examples.statement

import org.jparsec.Parser
import org.jparsec.Parsers
import org.jparsec.Scanners
import org.jparsec.Terminals
import org.jparsec.examples.statement.ast.DoubleExpression
import org.jparsec.examples.statement.ast.SingleExpression
import org.jparsec.functors.Map
import org.junit.Assert
import org.junit.Test

class FirstTest {
    @Test fun testSingle1() {
        val exp = singleExpression01().from(TOKENIZER, IGNORED).parse("readonly")
        Assert.assertEquals("readonly", exp.s)
    }

    @Test fun testSingle2() {
        val exp = singleExpression02().from(TOKENIZER, IGNORED).parse("readonly var")
        Assert.assertEquals("var", exp.s)
    }

    @Test fun testDouble1() {
        val exp = doubleExpression01().from(TOKENIZER, IGNORED).parse("readonly var")
        Assert.assertEquals("readonly", exp.s)
        Assert.assertEquals("var", exp.s2)
    }

    @Test fun testDouble2() {
        val exp = doubleExpression02().from(TOKENIZER, IGNORED).parse("readonly var")
        Assert.assertEquals("readonly", exp.s)
        Assert.assertEquals("var", exp.s2)
    }

    companion object {
        //low-level grammar
        private val OPERATORS = Terminals.operators("=", "readonly", "var")

        //and here are the tokenizers, combined into our single tokenizer
        private val TOKENIZER: Parser<*> = Parsers.or(
            OPERATORS.tokenizer(),
            Terminals.IntegerLiteral.TOKENIZER,
            Terminals.Identifier.TOKENIZER,
            Terminals.Identifier.TOKENIZER)
        private val IGNORED = Parsers.or(
            Scanners.JAVA_LINE_COMMENT,
            Scanners.JAVA_BLOCK_COMMENT,
            Scanners.WHITESPACES).skipMany()

        private fun readonly(): Parser<String> {
            return OPERATORS.token("readonly").retn("readonly")
        }

        private fun `var`(): Parser<String> {
            return OPERATORS.token("var").retn("var")
        }

        private fun eq(): Parser<String> {
            return OPERATORS.token("=").retn("=")
        }

        //high-level grammar
        // *** Java 8 Syntax ***
        //parse two tokens and use the output of all tokens in sequence()
        private fun doubleExpression01(): Parser<DoubleExpression> {
            return Parsers.sequence(readonly(), `var`()) { s: String, s2: String -> DoubleExpression(s, s2) }
        }

        //same thing but use Java 8 functional interface
        private fun doubleExpression02(): Parser<DoubleExpression> {
            return Parsers.sequence(readonly(), `var`()) { s: String, s2: String -> DoubleExpression(s, s2) }
        }

        //parse two tokens but only take the output of last one in sequence()
        private fun singleExpression02(): Parser<SingleExpression> {
            return Parsers.sequence(readonly(), `var`()).map { s: String -> SingleExpression(s) }
        }

        // *** Java 6 Syntax ***
        //parse single value into a SingleExpression.
        //This is an example of the older Java 6 syntax, using map.
        //It is quite verbose (and is not recommended).
        private fun singleExpression01(): Parser<SingleExpression> {
            return Parsers.or(readonly())
                .map(object: Map<String, SingleExpression> {
                    override fun map(arg0: String): SingleExpression {
                        return SingleExpression(arg0)
                    }
                })
        }
    }
}