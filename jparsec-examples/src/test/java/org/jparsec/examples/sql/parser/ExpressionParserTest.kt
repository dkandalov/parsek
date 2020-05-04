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

import org.jparsec.examples.sql.ast.*
import org.jparsec.examples.sql.parser.RelationParserTest.Companion.table
import org.jparsec.examples.sql.parser.TerminalParserTest.Companion.assertParser
import org.jparsec.functors.Tuples.pair
import org.junit.Test

/**
 * Unit test for [ExpressionParser].
 *
 * @author Ben Yu
 */
class ExpressionParserTest {
    @Test fun testNumber() {
        assertParser(ExpressionParser.NUMBER, "1.2", NumberExpression("1.2"))
    }

    @Test fun testQualifiedName() {
        val parser = ExpressionParser.QUALIFIED_NAME
        assertParser(parser, "a", name("a"))
        assertParser(parser, "a . bc", name("a", "bc"))
    }

    @Test fun testQualifiedWildcard() {
        assertParser(ExpressionParser.QUALIFIED_WILDCARD, "a.b.*",
                     WildcardExpression(QualifiedName.of("a", "b")))
    }

    @Test fun testWildcard() {
        assertParser(ExpressionParser.WILDCARD, "a.b.*",
                     WildcardExpression(QualifiedName.of("a", "b")))
        assertParser(ExpressionParser.WILDCARD, "*",
                     WildcardExpression(QualifiedName.of()))
    }

    @Test fun testString() {
        assertParser(ExpressionParser.STRING, "'foo'", StringExpression("foo"))
    }

    @Test fun testFunctionCall() {
        val parser = ExpressionParser.functionCall(ExpressionParser.NUMBER)
        assertParser(parser, "f()", FunctionExpression.of(QualifiedName.of("f")))
        assertParser(parser, "a.b(1)",
                     FunctionExpression.of(QualifiedName.of("a", "b"), number(1)))
        assertParser(parser, "a.b(1, 2)",
                     FunctionExpression.of(QualifiedName.of("a", "b"), number(1), number(2)))
    }

    @Test fun testBetween() {
        val parser = ExpressionParser.between(ExpressionParser.NUMBER)
        assertParser(parser, "1 BETWEEN 0 and 2",
                     BetweenExpression(number(1), true, number(0), number(2)))
        assertParser(parser, "1 not between 2 and 0",
                     BetweenExpression(number(1), false, number(2), number(0)))
    }

    @Test fun testTuple() {
        val parser = ExpressionParser.tuple(ExpressionParser.NUMBER)
        assertParser(parser, "()", TupleExpression.of())
        assertParser(parser, "(1)", TupleExpression.of(number(1)))
        assertParser(parser, "(1, 2)", TupleExpression.of(number(1), number(2)))
        TerminalParserTest.assertFailure(parser, "1", 1, 1, "( expected, 1 encountered.")
        TerminalParserTest.assertFailure(parser, "", 1, 1)
    }

    @Test fun testSimpleCase() {
        val parser = ExpressionParser.simpleCase(ExpressionParser.NUMBER)
        assertParser(parser, "case 1 when 1 then 2 else 3 end",
                     simpleCase(number(1), number(1), number(2), number(3)))
        assertParser(parser, "case 1 when 1 then 2 end",
                     simpleCase(number(1), number(1), number(2), null))
    }

    @Test fun testFullCase() {
        val parser = ExpressionParser.fullCase(ExpressionParser.QUALIFIED_NAME, ExpressionParser.NUMBER)
        assertParser(parser, "case when a then 2 else 3 end",
                     fullCase(name("a"), number(2), number(3)))
        assertParser(parser, "case when a then 2 end", fullCase(name("a"), number(2), null))
    }

    @Test fun testArithmetic() {
        val parser = ExpressionParser.arithmetic(ExpressionParser.NUMBER)
        assertParser(parser, "1", number(1))
        assertParser(parser, "((1))", number(1))
        assertParser(parser, "1 + 2", BinaryExpression(number(1), Op.PLUS, number(2)))
        assertParser(parser, "2 * (1 + (2))",
                     BinaryExpression(number(2), Op.MUL,
                                      BinaryExpression(number(1), Op.PLUS, number(2))))
        assertParser(parser, "2 - 1 / (2)",
                     BinaryExpression(number(2), Op.MINUS,
                                      BinaryExpression(number(1), Op.DIV, number(2))))
        assertParser(parser, "2 * 1 % -2",
                     BinaryExpression(
                         BinaryExpression(number(2), Op.MUL, number(1)),
                         Op.MOD, UnaryExpression(Op.NEG, number(2))))
        assertParser(parser, "f(1)", FunctionExpression.of(QualifiedName.of("f"), number(1)))
        assertParser(parser, "foo.bar(1, 2) + baz(foo.bar(1 / 2))",
                     BinaryExpression(
                         FunctionExpression.of(QualifiedName.of("foo", "bar"), number(1), number(2)),
                         Op.PLUS,
                         FunctionExpression.of(QualifiedName.of("baz"),
                                               FunctionExpression.of(QualifiedName.of("foo", "bar"), BinaryExpression(
                                                   number(1), Op.DIV, number(2))))))
    }

    @Test fun testExpression() {
        val parser = ExpressionParser.expression(ExpressionParser.NUMBER)
        assertParser(parser,
                     "1 + case a when a then count(a.b.*) end - case when 1 then a * 2 else b end",
                     BinaryExpression(
                         BinaryExpression(
                             number(1),
                             Op.PLUS,
                             simpleCase(name("a"), name("a"), function("count", wildcard("a", "b")), null)),
                         Op.MINUS,
                         fullCase(number(1), BinaryExpression(name("a"), Op.MUL, number(2)), name("b"))
                     )
        )
    }

    @Test fun testCompare() {
        val parser = ExpressionParser.compare(ExpressionParser.NUMBER)
        assertParser(parser, "1 = 1", BinaryExpression(number(1), Op.EQ, number(1)))
        assertParser(parser, "1 < 2", BinaryExpression(number(1), Op.LT, number(2)))
        assertParser(parser, "1 <= 2", BinaryExpression(number(1), Op.LE, number(2)))
        assertParser(parser, "1 <> 2", BinaryExpression(number(1), Op.NE, number(2)))
        assertParser(parser, "2 > 1", BinaryExpression(number(2), Op.GT, number(1)))
        assertParser(parser, "2 >= 1", BinaryExpression(number(2), Op.GE, number(1)))
        assertParser(parser, "1 is null",
                     BinaryExpression(number(1), Op.IS, NullExpression.instance))
        assertParser(parser, "1 is not null",
                     BinaryExpression(number(1), Op.NOT, NullExpression.instance))
        assertParser(parser, "1 like 2", LikeExpression(number(1), true, number(2), null))
        assertParser(parser, "1 BETWEEN 0 and 2",
                     BetweenExpression(number(1), true, number(0), number(2)))
        assertParser(parser, "1 not between 2 and 0",
                     BetweenExpression(number(1), false, number(2), number(0)))
    }

    @Test fun testLike() {
        val parser = ExpressionParser.like(ExpressionParser.NUMBER)
        assertParser(parser, "1 like 2", LikeExpression(number(1), true, number(2), null))
        assertParser(parser, "1 not like 2", LikeExpression(number(1), false, number(2), null))
        assertParser(parser, "1 like 2 escape 3",
                     LikeExpression(number(1), true, number(2), number(3)))
        assertParser(parser, "1 not like 2 escape 3",
                     LikeExpression(number(1), false, number(2), number(3)))
    }

    @Test fun testLogical() {
        val parser = ExpressionParser.logical(ExpressionParser.NUMBER)
        assertParser(parser, "1", number(1))
        assertParser(parser, "(1)", number(1))
        assertParser(parser, "((1))", number(1))
        assertParser(parser, "not 1", UnaryExpression(Op.NOT, number(1)))
        assertParser(parser, "1 and 2", BinaryExpression(number(1), Op.AND, number(2)))
        assertParser(parser, "1 or 2", BinaryExpression(number(1), Op.OR, number(2)))
        assertParser(parser, "1 or 2 and 3", BinaryExpression(number(1), Op.OR,
                                                              BinaryExpression(number(2), Op.AND, number(3))))
        assertParser(parser, "1 or NOT 2", BinaryExpression(number(1), Op.OR,
                                                            UnaryExpression(Op.NOT, number(2))))
        assertParser(parser, "not 1 and 2", BinaryExpression(
            UnaryExpression(Op.NOT, number(1)), Op.AND, number(2)))
    }

    @Test fun testExists() {
        assertParser(ExpressionParser.exists(RelationParser.TABLE), "exists t",
                     UnaryRelationalExpression(table("t"), Op.EXISTS))
    }

    @Test fun testNotExists() {
        assertParser(ExpressionParser.notExists(RelationParser.TABLE), "not exists t",
                     UnaryRelationalExpression(table("t"), Op.NOT_EXISTS))
    }

    @Test fun testInRelation() {
        assertParser(ExpressionParser.inRelation(ExpressionParser.NUMBER, RelationParser.TABLE),
                     "1 in (table)",
                     BinaryRelationalExpression(number(1), Op.IN, table("table")))
    }

    @Test fun testNotInRelation() {
        assertParser(ExpressionParser.notInRelation(ExpressionParser.NUMBER, RelationParser.TABLE),
                     "1 not in (table)",
                     BinaryRelationalExpression(number(1), Op.NOT_IN, table("table")))
    }

    @Test fun testIn() {
        assertParser(ExpressionParser.`in`(ExpressionParser.NUMBER), "1 in (2)",
                     BinaryExpression(number(1), Op.IN, TupleExpression.of(number(2))))
        assertParser(ExpressionParser.`in`(ExpressionParser.NUMBER), "1 in (2, 3)",
                     BinaryExpression(number(1), Op.IN, TupleExpression.of(number(2), number(3))))
    }

    @Test fun testNotIn() {
        assertParser(ExpressionParser.notIn(ExpressionParser.NUMBER), "1 not in (2)",
                     BinaryExpression(number(1), Op.NOT_IN, TupleExpression.of(number(2))))
        assertParser(ExpressionParser.notIn(ExpressionParser.NUMBER), "1 not in (2, 3)",
                     BinaryExpression(number(1), Op.NOT_IN, TupleExpression.of(number(2), number(3))))
    }

    @Test fun testCondition() {
        val parser = ExpressionParser.condition(ExpressionParser.NUMBER, RelationParser.TABLE)
        assertParser(parser, "1 = 2", BinaryExpression(number(1), Op.EQ, number(2)))
        assertParser(parser, "1 is null",
                     BinaryExpression(number(1), Op.IS, NullExpression.instance))
        assertParser(parser, "1 is not null",
                     BinaryExpression(number(1), Op.NOT, NullExpression.instance))
        assertParser(parser, "1 like 2", LikeExpression(number(1), true, number(2), null))
        assertParser(parser, "(1 < 2 or not exists t)",
                     BinaryExpression(
                         BinaryExpression(number(1), Op.LT, number(2)),
                         Op.OR,
                         UnaryExpression(Op.NOT,
                                         UnaryRelationalExpression(table("t"), Op.EXISTS))
                     )
        )
    }

    companion object {
        fun number(i: Int): Expression {
            return NumberExpression(i.toString())
        }

        fun name(vararg names: String?): Expression {
            return QualifiedNameExpression.of(*names)
        }

        fun wildcard(vararg owners: String?): Expression {
            return WildcardExpression(QualifiedName.of(*owners))
        }

        fun function(name: String?, vararg args: Expression?): Expression {
            return FunctionExpression(QualifiedName.of(name), listOf(*args))
        }

        fun fullCase(`when`: Expression?, then: Expression?, defaultValue: Expression?): Expression {
            return FullCaseExpression(listOf(pair(`when`, then)), defaultValue)
        }

        fun simpleCase(expr: Expression?, `when`: Expression?, then: Expression?, defaultValue: Expression?): Expression {
            return SimpleCaseExpression(expr, listOf(pair(`when`, then)), defaultValue)
        }
    }
}