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

import org.jparsec.OperatorTable.Associativity
import org.junit.Assert
import org.junit.Test
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Unit test for [OperatorTable].
 *
 * @author Ben Yu
 */
class OperatorTableTest {
    @Test fun testAssociativityOrder() {
        assertTotalOrder(Associativity.PREFIX, Associativity.POSTFIX, Associativity.LASSOC, Associativity.NASSOC, Associativity.RASSOC)
    }

    @Test fun testOperatorOrder() {
        assertTotalOrder(
            operator(2, Associativity.PREFIX), operator(2, Associativity.POSTFIX),
            operator(2, Associativity.LASSOC), operator(2, Associativity.NASSOC), operator(2, Associativity.RASSOC),
            operator(1, Associativity.PREFIX), operator(1, Associativity.POSTFIX),
            operator(1, Associativity.LASSOC), operator(1, Associativity.NASSOC), operator(1, Associativity.RASSOC))
    }

    @Test fun testGetOperators() {
        val table = OperatorTable<Int>()
            .infixl(BINARY_OP, 2)
            .infixr(BINARY_OP, 1)
            .prefix(UNARY_OP, 4)
            .postfix(UNARY_OP, 3)
            .postfix(UNARY_OP, 3)
            .infixn(BINARY_OP, 5)
        Assert.assertNotNull(table)
        val operators = table.operators()
        Assert.assertEquals(6, operators.size.toLong())
        Assert.assertEquals(5, operators[0].precedence.toLong())
        Assert.assertEquals(4, operators[1].precedence.toLong())
        Assert.assertEquals(3, operators[2].precedence.toLong())
        Assert.assertEquals(3, operators[3].precedence.toLong())
        Assert.assertEquals(2, operators[4].precedence.toLong())
        Assert.assertEquals(1, operators[5].precedence.toLong())
    }

    companion object {
        private val OP: Parser<*> = Parsers.never<Any>()
        private val UNARY_OP = Parsers.never<Function<Int, Int>>()
        private val BINARY_OP = Parsers.never<BiFunction<Int, Int, Int>>()
        private fun <T: Comparable<T>?> assertTotalOrder(vararg objects: T) {
            for (i in 0 until objects.size) {
                assertSameOrder(objects[i])
                for (j in i + 1 until objects.size) {
                    assertOrder(objects[i], objects[j])
                }
            }
        }

        private fun <T: Comparable<T>?> assertOrder(obj1: T, obj2: T) {
            Assert.assertTrue(obj1.toString() + " should be before " + obj2, obj1!!.compareTo(obj2) < 0)
            Assert.assertTrue(obj2.toString() + " should be after " + obj1, obj2!!.compareTo(obj1) > 0)
        }

        private fun <T: Comparable<T>?> assertSameOrder(obj: T) {
            Assert.assertEquals(obj.toString() + " should be equal to itself", 0, obj!!.compareTo(obj).toLong())
        }

        private fun operator(precedence: Int, associativity: Associativity): OperatorTable.Operator {
            return OperatorTable.Operator(OP, precedence, associativity)
        }
    }
}