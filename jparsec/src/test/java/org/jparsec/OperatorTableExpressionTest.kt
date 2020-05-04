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

import org.easymock.EasyMock
import org.jparsec.easymock.BaseMockTest
import org.junit.Assert
import org.junit.Test
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Unit test for [OperatorTable] for building expression parsers.
 *
 * @author Ben Yu
 */
class OperatorTableExpressionTest: BaseMockTest() {
    // Tests against a sample operator precedence grammar, with {@code +, -, *} as infix
    // left-associative operators, {@code ^} as right-associative operator, {@code ~} as prefix
    // operator, {@code %} as postfix operator, and {@code .} as infix non-associative operator.
    @Mock
    var negate: Function<String, String>? = null

    @Mock
    var plus: BiFunction<String, String, String>? = null

    @Mock
    var subtract: BiFunction<String, String, String>? = null

    @Mock
    var multiply: BiFunction<String, String, String>? = null

    @Mock
    var percent: Function<String, String>? = null

    @Mock
    var point: BiFunction<String, String, String>? = null

    @Mock
    var power: BiFunction<String, String, String>? = null
    @Test fun testBuildExpressionParser() {
        val source = "1+2.3-30%-1+~5*20000%%^2^1*~~3"
        EasyMock.expect(point!!.apply("2", "3")).andReturn("2.3")
        EasyMock.expect(plus!!.apply("1", "2.3")).andReturn("3.3")
        EasyMock.expect(percent!!.apply("30")).andReturn("0.3")
        EasyMock.expect(subtract!!.apply("3.3", "0.3")).andReturn("3.0")
        EasyMock.expect(subtract!!.apply("3.0", "1")).andReturn("2.0")
        EasyMock.expect(negate!!.apply("5")).andReturn("-5")
        EasyMock.expect(percent!!.apply("20000")).andReturn("200")
        EasyMock.expect(percent!!.apply("200")).andReturn("2")
        EasyMock.expect(negate!!.apply("3")).andReturn("-3")
        EasyMock.expect(negate!!.apply("-3")).andReturn("3")
        EasyMock.expect(power!!.apply("2", "1")).andReturn("2")
        EasyMock.expect(power!!.apply("2", "2")).andReturn("4")
        EasyMock.expect(multiply!!.apply("-5", "4")).andReturn("-20")
        EasyMock.expect(multiply!!.apply("-20", "3")).andReturn("-60")
        EasyMock.expect(plus!!.apply("2.0", "-60")).andReturn("-58.0")
        replay()
        Assert.assertEquals("-58.0", parser().parse(source))
    }

    @Test fun testEmptyOperatorTable() {
        val operand = Parsers.constant("foo")
        Assert.assertSame(operand, OperatorTable<String>().build(operand))
    }

    private fun parser(): Parser<String> {
        return OperatorTable<String>()
            .prefix(op("~", negate), 100)
            .postfix(op("%", percent), 80)
            .infixr(op("^", power), 40)
            .infixl(op("+", plus), 10)
            .infixl(op("-", subtract), 10)
            .infixl(op("*", multiply), 20)
            .infixn(op(".", point), 200)
            .build(Scanners.INTEGER.source())
    }

    companion object {
        private fun <T> op(name: String, value: T): Parser<T> {
            return Scanners.string(name).retn(value)
        }
    }
}