package org.jparsec.examples.statement

import org.jparsec.Parser
import org.jparsec.Parsers.or
import org.jparsec.Parsers.sequence
import org.jparsec.Scanners
import org.jparsec.Terminals
import org.jparsec.Terminals.Companion.operators
import org.jparsec.examples.statement.ast.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.function.BiFunction
import java.util.function.Function

class GrammarTest {
    @Test fun test1() {
        val exp = parse("var x = 50")
        assertEquals("x", exp.identExpr.s)
        assertEquals(50, exp.valueExpr.nVal.toLong())
    }

    @Test fun test2() {
        val exp = parse("readonly var x = 50")
        assertEquals("x", exp.identExpr.s)
        assertEquals(50, exp.valueExpr.nVal.toLong())
    }

    //you can test individual pieces of the grammar
    @Test fun test3() {
        val exp = assignment().from(TOKENIZER, IGNORED).parse("= 5")
        assertEquals(5, exp.nVal.toLong())
    }

    companion object {
        //low-level grammar
        private val OPERATORS = operators("=", "readonly", "var")

        //for integers and identifiers we need a PARSER and a TOKENIZER for each
        //here are the parsers
        private val IDENTIFIER_PARSER = Terminals.Identifier.PARSER
        private val INTEGER_PARSER = Terminals.IntegerLiteral.PARSER

        //and here are the tokenizers, combined into our single tokenizer
        private val TOKENIZER: Parser<*> = or(
            OPERATORS.tokenizer(),
            Terminals.IntegerLiteral.TOKENIZER,
            Terminals.Identifier.TOKENIZER,
            Terminals.Identifier.TOKENIZER)
        private val IGNORED = or(
            Scanners.JAVA_LINE_COMMENT,
            Scanners.JAVA_BLOCK_COMMENT,
            Scanners.WHITESPACES).skipMany()

        private fun readonly(): Parser<String> {
            return OPERATORS.token("readonly").retn("readonly")
        }

        private fun `var`(): Parser<String?> {
            return OPERATORS.token("var").retn("var")
        }

        private fun eq(): Parser<String> {
            return OPERATORS.token("=").retn("=")
        }

        //high-level grammar
        private fun readonlyExpression(): Parser<ReadonlyExpression> {
            return readonly().map(Function { s: String -> ReadonlyExpression(s) })
        }

        private fun varExpression(): Parser<VarExpression> {
            return `var`().map(Function { s: String? -> VarExpression(s!!) })
        }

        private fun ident(): Parser<IdentExpression> {
            return IDENTIFIER_PARSER.map(Function { s: String? -> IdentExpression(s!!) })
        }

        //'=' followed by an integer value
        private fun assignment(): Parser<ValueExpression> {
            return sequence(eq(), INTEGER_PARSER).map(Function { s: String? -> ValueExpression(s!!) })
        }

        //var x = 5
        private fun full(): Parser<FullExpression> {
            return sequence(
                readonly().asOptional(),
                `var`(),
                sequence(
                    ident(),
                    assignment(),
                    BiFunction { identExpr, valueExpr -> FullExpression(identExpr, valueExpr) }
                )
            )
        }

        private fun parse(input: String): FullExpression {
            val grammar = full()
            return grammar.from(TOKENIZER, IGNORED).parse(input)
        }
    }
}