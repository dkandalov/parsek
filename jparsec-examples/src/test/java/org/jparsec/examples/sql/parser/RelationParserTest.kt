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
import org.jparsec.examples.sql.ast.*
import org.jparsec.examples.sql.parser.TerminalParserTest.Companion.assertParser
import org.junit.Test

/**
 * Unit test for [RelationParser].
 *
 * @author Ben Yu
 */
class RelationParserTest {
    @Test fun testTable() {
        assertParser(RelationParser.TABLE, "a.b", table("a", "b"))
    }

    @Test fun testAliasable() {
        val parser = RelationParser.aliasable(RelationParser.TABLE)
        assertParser(parser, "table t", AliasedRelation(table("table"), "t"))
        assertParser(parser, "table as t", AliasedRelation(table("table"), "t"))
        assertParser(parser, "table", table("table"))
    }

    @Test fun testOrderByItem() {
        val parser = RelationParser.orderByItem(ExpressionParser.NUMBER)
        assertParser(parser, "1", OrderBy.Item(ExpressionParserTest.number(1), true))
        assertParser(parser, "1 asc", OrderBy.Item(ExpressionParserTest.number(1), true))
        assertParser(parser, "1 desc", OrderBy.Item(ExpressionParserTest.number(1), false))
    }

    @Test fun testOrderByClause() {
        val parser = RelationParser.orderByClause(ExpressionParser.NUMBER)
        assertParser(parser, "order by 1, 2 desc, 3 asc", OrderBy(listOf(
            OrderBy.Item(ExpressionParserTest.number(1), true), OrderBy.Item(ExpressionParserTest.number(2), false),
            OrderBy.Item(ExpressionParserTest.number(3), true))))
    }

    @Test fun testInnerJoin() {
        val parser = RelationParser.INNER_JOIN
        assertParser(parser, "join", JoinType.INNER)
        assertParser(parser, "inner join", JoinType.INNER)
    }

    @Test fun testLeftJoin() {
        val parser = RelationParser.LEFT_JOIN
        assertParser(parser, "left join", JoinType.LEFT)
        assertParser(parser, "left outer join", JoinType.LEFT)
    }

    @Test fun testRightJoin() {
        val parser = RelationParser.RIGHT_JOIN
        assertParser(parser, "right join", JoinType.RIGHT)
        assertParser(parser, "right outer join", JoinType.RIGHT)
    }

    @Test fun testFullJoin() {
        val parser = RelationParser.FULL_JOIN
        assertParser(parser, "full join", JoinType.FULL)
        assertParser(parser, "full outer join", JoinType.FULL)
    }

    @Test fun testJoin() {
        val parser = RelationParser.join(RelationParser.TABLE, ExpressionParser.NUMBER)
        assertParser(parser, "a", table("a"))
        assertParser(parser, "a cross join table2 as b",
                     CrossJoinRelation(table("a"), AliasedRelation(table("table2"), "b")))
        assertParser(parser, "a inner join b on 1",
                     JoinRelation(table("a"), JoinType.INNER, table("b"), ExpressionParserTest.number(1)))
        assertParser(parser, "a inner join b on 1 left join c on 2 cross join d",
                     CrossJoinRelation(
                         JoinRelation(
                             JoinRelation(table("a"), JoinType.INNER, table("b"), ExpressionParserTest.number(1)), JoinType.LEFT, table("c"), ExpressionParserTest.number(2)),
                         table("d")))
        assertParser(parser, "a cross join b inner join c right join d on 1 on 2",
                     JoinRelation(CrossJoinRelation(table("a"), table("b")),
                                  JoinType.INNER,
                                  JoinRelation(table("c"), JoinType.RIGHT, table("d"), ExpressionParserTest.number(1)),
                                  ExpressionParserTest.number(2)))
        assertParser(parser, "a cross join (b FULL join c on 1)",
                     CrossJoinRelation(table("a"),
                                       JoinRelation(table("b"), JoinType.FULL, table("c"), ExpressionParserTest.number(1))))
    }

    @Test fun testUnion() {
        val parser = RelationParser.union(RelationParser.TABLE)
        assertParser(parser, "a", table("a"))
        assertParser(parser, "a union b", UnionRelation(table("a"), false, table("b")))
        assertParser(parser, "a union all b union (c)",
                     UnionRelation(
                         UnionRelation(table("a"), true, table("b")),
                         false, table("c")
                     )
        )
        assertParser(parser, "a union all (b union (c))",
                     UnionRelation(
                         table("a"),
                         true,
                         UnionRelation(table("b"), false, table("c"))
                     )
        )
    }

    @Test fun testProjection() {
        val parser = RelationParser.projection(ExpressionParser.NUMBER)
        assertParser(parser, "1", Projection(ExpressionParserTest.number(1), null))
        assertParser(parser, "1 id", Projection(ExpressionParserTest.number(1), "id"))
        assertParser(parser, "1 as id", Projection(ExpressionParserTest.number(1), "id"))
    }

    @Test fun testSelectClause() {
        val parser = RelationParser.selectClause()
        assertParser(parser, "select", false)
        assertParser(parser, "select distinct", true)
    }

    @Test fun testFromClause() {
        val parser = RelationParser.fromClause(RelationParser.TABLE)
        assertListParser(parser, "from a", table("a"))
        assertListParser(parser, "from a x", AliasedRelation(table("a"), "x"))
        assertListParser(parser, "from table1 t1, t2",
                         AliasedRelation(table("table1"), "t1"), table("t2"))
    }

    @Test fun testGroupByClause() {
        val parser = RelationParser.groupByClause(ExpressionParser.NUMBER, ExpressionParser.NUMBER)
        assertParser(parser, "group by 1, 2", GroupBy(listOf(ExpressionParserTest.number(1), ExpressionParserTest.number(2)), null))
        assertParser(parser, "group by 1, 2 having 3",
                     GroupBy(listOf(ExpressionParserTest.number(1), ExpressionParserTest.number(2)), ExpressionParserTest.number(3)))
    }

    @Test fun testSelect() {
        val parser = RelationParser.select(ExpressionParser.NUMBER, ExpressionParser.NUMBER, RelationParser.TABLE)
        assertParser(parser, "select distinct 1, 2 as id from t1, t2",
                     Select(true,
                            listOf(Projection(ExpressionParserTest.number(1), null), Projection(ExpressionParserTest.number(2), "id")),
                            listOf(table("t1"), table("t2")),
                            null, null, null))
        assertParser(parser, "select 1 as id from t where 1",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), "id")),
                            listOf(table("t")),
                            ExpressionParserTest.number(1), null, null))
        assertParser(parser, "select 1 as id from t group by 2, 3",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), "id")),
                            listOf(table("t")),
                            null, GroupBy(listOf(ExpressionParserTest.number(2), ExpressionParserTest.number(3)), null), null))
        assertParser(parser, "select 1 as id from t group by 2, 3 having 4",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), "id")),
                            listOf(table("t")),
                            null, GroupBy(listOf(ExpressionParserTest.number(2), ExpressionParserTest.number(3)), ExpressionParserTest.number(4)), null))
        assertParser(parser, "select 1 as id from t order by 2 asc, 3 desc",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), "id")),
                            listOf(table("t")),
                            null, null, OrderBy(listOf(
                         OrderBy.Item(ExpressionParserTest.number(2), true), OrderBy.Item(ExpressionParserTest.number(3), false)))))
    }

    @Test fun testQuery() {
        val parser = RelationParser.query(ExpressionParser.NUMBER, ExpressionParser.NUMBER, RelationParser.TABLE)
        assertParser(parser, "select 1 from t",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), null)),
                            listOf(table("t")),
                            null, null, null))
        assertParser(parser, "select 1 from a union select distinct 2 from b",
                     UnionRelation(
                         Select(false,
                                listOf(Projection(ExpressionParserTest.number(1), null)),
                                listOf(table("a")),
                                null, null, null),
                         false,
                         Select(true,
                                listOf(Projection(ExpressionParserTest.number(2), null)),
                                listOf(table("b")),
                                null, null, null)))
    }

    @Test fun testCompleteQuery() {
        val parser = RelationParser.query()
        assertParser(parser, "select 1 from t",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), null)),
                            listOf(table("t")),
                            null, null, null))
        assertParser(parser, "select 1 from (select * from table) t",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), null)),
                            listOf<Relation>(AliasedRelation(
                                Select(false,
                                       listOf(Projection(ExpressionParserTest.wildcard(), null)),
                                       listOf(table("table")),
                                       null, null, null),
                                "t")),
                            null, null, null))
        assertParser(parser, "select 1 from t where x > 1",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), null)),
                            listOf(table("t")),
                            BinaryExpression(ExpressionParserTest.name("x"), Op.GT, ExpressionParserTest.number(1)), null, null))
        assertParser(parser, "select 1 from t where exists (select * from t2)",
                     Select(false,
                            listOf(Projection(ExpressionParserTest.number(1), null)),
                            listOf(table("t")),
                            UnaryRelationalExpression(
                                Select(false,
                                       listOf(Projection(ExpressionParserTest.wildcard(), null)),
                                       listOf(table("t2")),
                                       null, null, null),
                                Op.EXISTS), null, null))
        assertParser(parser, "select case when exists (select * from t1) then 1 end from t",
                     Select(false,
                            listOf(Projection(
                                ExpressionParserTest.fullCase(UnaryRelationalExpression(
                                    Select(false,
                                           listOf(Projection(ExpressionParserTest.wildcard(), null)),
                                           listOf(table("t1")),
                                           null, null, null), Op.EXISTS), ExpressionParserTest.number(1), null),
                                null)),
                            listOf(table("t")),
                            null, null, null))
    }

    companion object {
        @JvmStatic fun table(vararg names: String?): Relation {
            return TableRelation(QualifiedName.of(*names))
        }

        fun assertListParser(parser: Parser<*>?, source: String?, vararg expected: Any?) {
            assertParser(parser, source, listOf(*expected))
        }
    }
}