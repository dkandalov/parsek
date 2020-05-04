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

import org.jparsec.Parsers.never
import org.jparsec.examples.java.ast.expression.*
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertFailure
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertResult
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertToString
import org.junit.Test

/**
 * Unit test for [ExpressionParser].
 *
 * @author Ben Yu
 */
class ExpressionParserTest {
    @Test fun testNull() {
        assertResult(ExpressionParser.NULL, "null", NullExpression::class.java, "null")
    }

    @Test fun testIdentifier() {
        assertResult(ExpressionParser.IDENTIFIER, "foo", Identifier::class.java, "foo")
    }

    @Test fun testSuper() {
        assertResult(ExpressionParser.SUPER, "super", SuperExpression::class.java, "super")
    }

    @Test fun testThis() {
        val parser = ExpressionParser.THIS
        assertResult(parser, "this", ThisExpression::class.java, "this")
        assertResult(parser, "foo.this", ThisExpression::class.java, "foo.this")
        assertResult(parser, "A.b.this", ThisExpression::class.java, "A.b.this")
    }

    @Test fun testCharLiteral() {
        val parser = ExpressionParser.CHAR_LITERAL
        assertResult(parser, "'a'", CharLiteral::class.java, "a")
        assertResult(parser, "'\\''", CharLiteral::class.java, "'")
    }

    @Test fun testStringLiteral() {
        val parser = ExpressionParser.STRING_LITERAL
        assertResult(parser, "\"\"", StringLiteral::class.java, "")
        assertResult(parser, "\"foo\"", StringLiteral::class.java, "foo")
        assertResult(parser, "\"\\\"\"", StringLiteral::class.java, "\"")
    }

    @Test fun testBooleanLiteral() {
        val parser = ExpressionParser.BOOLEAN_LITERAL
        assertResult(parser, "true", BooleanLiteral::class.java, "true")
        assertResult(parser, "false", BooleanLiteral::class.java, "false")
    }

    @Test fun testClassLiteral() {
        val parser = ExpressionParser.CLASS_LITERAL
        assertResult(parser, "int.class", ClassLiteral::class.java, "int.class")
        assertResult(parser, "Integer.class", ClassLiteral::class.java, "Integer.class")
        assertResult(parser, "java.lang.Integer.class", ClassLiteral::class.java, "java.lang.Integer.class")
        assertResult(parser, "Map<Integer, String>.class",
                     ClassLiteral::class.java, "Map<Integer, String>.class")
    }

    @Test fun testIntegerLiteral() {
        val parser = ExpressionParser.INTEGER_LITERAL
        assertResult(parser, "123", IntegerLiteral::class.java, "123")
        assertResult(parser, "0x123L", IntegerLiteral::class.java, "0X123L")
        assertResult(parser, "0123f", IntegerLiteral::class.java, "0123F")
    }

    @Test fun testDecimalLiteral() {
        val parser = ExpressionParser.DECIMAL_LITERAL
        assertResult(parser, "123.0", DecimalPointNumberLiteral::class.java, "123.0")
        assertResult(parser, "123.0D", DecimalPointNumberLiteral::class.java, "123.0")
        assertResult(parser, "0.123F", DecimalPointNumberLiteral::class.java, "0.123F")
    }

    @Test fun testCastOrExpression() {
        val parser = ExpressionParser.castOrExpression(ExpressionParser.IDENTIFIER)
        assertResult(parser, "(foo)", Identifier::class.java, "foo")
        assertResult(parser, "(foo) bar", CastExpression::class.java, "((foo) bar)")
        assertResult(parser, "(foo<int>) bar", CastExpression::class.java, "((foo<int>) bar)")
        assertFailure(parser, "(foo<int>) ", 1, 12)
    }

    @Test fun testInstanceOf() {
        assertToString(InstanceOfExpression::class.java, "(1 instanceof int)",
                       TerminalParser.parse(ExpressionParser.INSTANCE_OF, "instanceof int").apply(literal(1)))
        assertToString(InstanceOfExpression::class.java, "(1 instanceof List<int>)",
                       TerminalParser.parse(ExpressionParser.INSTANCE_OF, "instanceof List<int>").apply(literal(1)))
    }

    @Test fun testQualifiedExpr() {
        assertToString(QualifiedExpression::class.java, "(1.foo)",
                       TerminalParser.parse(ExpressionParser.QUALIFIED_EXPR, ".foo").apply(literal(1)))
    }

    @Test fun testSubscript() {
        assertToString(ArraySubscriptExpression::class.java, "1[foo]",
                       TerminalParser.parse(ExpressionParser.subscript(ExpressionParser.IDENTIFIER), "[foo]").apply(literal(1)))
    }

    @Test fun testQualifiedMethodCall() {
        val parser = ExpressionParser.qualifiedMethodCall(ExpressionParser.IDENTIFIER)
        assertToString(MethodCallExpression::class.java, "1.f(a, b)",
                       TerminalParser.parse(parser, ".f(a,b)").apply(literal(1)))
        assertToString(MethodCallExpression::class.java, "1.f()",
                       TerminalParser.parse(parser, ".f()").apply(literal(1)))
    }

    @Test fun testQualifiedNew() {
        val parser = ExpressionParser.qualifiedNew(ExpressionParser.IDENTIFIER, EMPTY_BODY)
        assertToString(NewExpression::class.java, "1.new int(a, b) {}",
                       TerminalParser.parse(parser, ".new int(a,b){}").apply(literal(1)))
        assertToString(NewExpression::class.java, "1.new int(a)",
                       TerminalParser.parse(parser, ".new int(a)").apply(literal(1)))
    }

    @Test fun testSimpleMethodCall() {
        assertResult(ExpressionParser.simpleMethodCall(ExpressionParser.IDENTIFIER), "f(a,b)",
                     MethodCallExpression::class.java, "f(a, b)")
    }

    @Test fun testSimpleNewExpression() {
        val parser = ExpressionParser.simpleNewExpression(ExpressionParser.IDENTIFIER, EMPTY_BODY)
        assertResult(parser, "new Foo(a,b)", NewExpression::class.java, "new Foo(a, b)")
        assertResult(parser, "new Foo(a,b){}", NewExpression::class.java, "new Foo(a, b) {}")
    }

    @Test fun testNewArrayWithExplicitLength() {
        val parser = ExpressionParser.newArrayWithExplicitLength(ExpressionParser.IDENTIFIER)
        assertResult(parser, "new int[n]", NewArrayExpression::class.java, "new int[n]")
        assertResult(parser, "new int[][n]", NewArrayExpression::class.java, "new int[][n]")
        assertResult(parser, "new int[n]{}", NewArrayExpression::class.java, "new int[n] {}")
        assertResult(parser, "new int[n]{a,b,c}", NewArrayExpression::class.java, "new int[n] {a, b, c}")
    }

    @Test fun testNewArrayWithoutExplicitLength() {
        val parser = ExpressionParser.newArrayWithoutExplicitLength(ExpressionParser.IDENTIFIER)
        assertResult(parser, "new int[]{}", NewArrayExpression::class.java, "new int[] {}")
        assertResult(parser, "new int[]{a,b,c}", NewArrayExpression::class.java, "new int[] {a, b, c}")
        assertFailure(parser, "new int[]", 1, 10)
    }

    @Test fun testConditional() {
        assertToString(ConditionalExpression::class.java, "(1 ? a : 2)",
                       TerminalParser.parse(ExpressionParser.conditional(ExpressionParser.IDENTIFIER), "?a:").apply(literal(1), literal(2)))
    }

    @Test fun testAtom() {
        val parser = ExpressionParser.ATOM
        assertResult(parser, "null", NullExpression::class.java, "null")
        assertResult(parser, "this", ThisExpression::class.java, "this")
        assertResult(parser, "super", SuperExpression::class.java, "super")
        assertResult(parser, "int.class", ClassLiteral::class.java, "int.class")
        assertResult(parser, "true", BooleanLiteral::class.java, "true")
        assertResult(parser, "false", BooleanLiteral::class.java, "false")
        assertResult(parser, "'a'", CharLiteral::class.java, "a")
        assertResult(parser, "\"foo\"", StringLiteral::class.java, "foo")
        assertResult(parser, "123l", IntegerLiteral::class.java, "123L")
        assertResult(parser, "1.2f", DecimalPointNumberLiteral::class.java, "1.2F")
        assertResult(parser, "1.2e10f", ScientificNumberLiteral::class.java, "1.2e10F")
        assertResult(parser, "1", IntegerLiteral::class.java, "1")
        assertResult(parser, "1.0", DecimalPointNumberLiteral::class.java, "1.0")
        assertResult(parser, "foo", Identifier::class.java, "foo")
    }

    @Test fun testExpression() {
        val parser = ExpressionParser.expression(ExpressionParser.IDENTIFIER, EMPTY_BODY, StatementParser.expression(ExpressionParser.IDENTIFIER))
        assertResult(parser, "foo", Identifier::class.java, "foo")
        assertResult(parser, "(foo)", Identifier::class.java, "foo")
        assertResult(parser, "((foo))", Identifier::class.java, "foo")
        assertResult(parser, "foo[bar[baz]]", ArraySubscriptExpression::class.java, "foo[bar[baz]]")
        assertResult(parser, "(foo) (bar)", CastExpression::class.java, "((foo) bar)")
        assertResult(parser, "(foo) (bar) baz", CastExpression::class.java, "((foo) ((bar) baz))")
        assertResult(parser, "new Foo(a,b)", NewExpression::class.java, "new Foo(a, b)")
        assertResult(parser, "new int[n]", NewArrayExpression::class.java, "new int[n]")
        assertResult(parser, "new int[n]{}", NewArrayExpression::class.java, "new int[n] {}")
        assertResult(parser, "new int[]{a,b,c}", NewArrayExpression::class.java, "new int[] {a, b, c}")
        assertResult(parser, "foo(a)", MethodCallExpression::class.java, "foo(a)")
        assertResult(parser, "foo.f()", MethodCallExpression::class.java, "foo.f()")
        assertResult(parser, "foo().bar().baz()", MethodCallExpression::class.java, "foo().bar().baz()")
        assertResult(parser, "foo.new Foo()", NewExpression::class.java, "foo.new Foo()")
        assertResult(parser, "foo.bar.baz", QualifiedExpression::class.java, "((foo.bar).baz)")
        assertResult(parser, "foo.bar.new Foo()", NewExpression::class.java, "(foo.bar).new Foo()")
        assertResult(parser, "foo++", PostfixUnaryExpression::class.java, "(foo++)")
        assertResult(parser, "foo++--", PostfixUnaryExpression::class.java, "((foo++)--)")
        assertResult(parser, "++foo", PrefixUnaryExpression::class.java, "(++foo)")
        assertResult(parser, "++--foo", PrefixUnaryExpression::class.java, "(++(--foo))")
        assertResult(parser, "++foo--", PrefixUnaryExpression::class.java, "(++(foo--))")
        assertResult(parser, "+foo", PrefixUnaryExpression::class.java, "(+foo)")
        assertResult(parser, "+-foo", PrefixUnaryExpression::class.java, "(+(-foo))")
        assertResult(parser, "!foo", PrefixUnaryExpression::class.java, "(!foo)")
        assertResult(parser, "!!foo", PrefixUnaryExpression::class.java, "(!(!foo))")
        assertResult(parser, "~foo", PrefixUnaryExpression::class.java, "(~foo)")
        assertResult(parser, "~~foo", PrefixUnaryExpression::class.java, "(~(~foo))")
        assertResult(parser, "foo+bar", BinaryExpression::class.java, "(foo + bar)")
        assertResult(parser, "a+b*c/d-e%f", BinaryExpression::class.java, "((a + ((b * c) / d)) - (e % f))")
        assertResult(parser, "a<<b", BinaryExpression::class.java, "(a << b)")
        assertResult(parser, "a>>b", BinaryExpression::class.java, "(a >> b)")
        assertResult(parser, "a>>>b", BinaryExpression::class.java, "(a >>> b)")
        assertResult(parser, "a>b", BinaryExpression::class.java, "(a > b)")
        assertResult(parser, "a>b<c", BinaryExpression::class.java, "((a > b) < c)")
        assertResult(parser, "a>=b", BinaryExpression::class.java, "(a >= b)")
        assertResult(parser, "a>=b<=c", BinaryExpression::class.java, "((a >= b) <= c)")
        assertResult(parser, "a instanceof int", InstanceOfExpression::class.java, "(a instanceof int)")
        assertResult(parser, "a instanceof int instanceof boolean",
                     InstanceOfExpression::class.java, "((a instanceof int) instanceof boolean)")
        assertResult(parser, "a==b", BinaryExpression::class.java, "(a == b)")
        assertResult(parser, "a==b!=c", BinaryExpression::class.java, "((a == b) != c)")
        assertResult(parser, "a&b&c", BinaryExpression::class.java, "((a & b) & c)")
        assertResult(parser, "a|b&c", BinaryExpression::class.java, "(a | (b & c))")
        assertResult(parser, "a&&b|c", BinaryExpression::class.java, "(a && (b | c))")
        assertResult(parser, "!a||b&&c", BinaryExpression::class.java, "((!a) || (b && c))")
        assertResult(parser, "x?c:d", ConditionalExpression::class.java, "(x ? c : d)")
        assertResult(parser, "a==b?b==c?x:y:z+n ? m: n",
                     ConditionalExpression::class.java, "((a == b) ? ((b == c) ? x : y) : ((z + n) ? m : n))")
        assertResult(parser, "a=b=c", BinaryExpression::class.java, "(a = (b = c))")
        assertResult(parser, "a+=b+=c", BinaryExpression::class.java, "(a += (b += c))")
        assertResult(parser, "a-=b-=c", BinaryExpression::class.java, "(a -= (b -= c))")
        assertResult(parser, "a*=b*=c", BinaryExpression::class.java, "(a *= (b *= c))")
        assertResult(parser, "a/=b/=c", BinaryExpression::class.java, "(a /= (b /= c))")
        assertResult(parser, "a%=b%=c", BinaryExpression::class.java, "(a %= (b %= c))")
        assertResult(parser, "a&=b&=c", BinaryExpression::class.java, "(a &= (b &= c))")
        assertResult(parser, "a|=b|=c", BinaryExpression::class.java, "(a |= (b |= c))")
        assertResult(parser, "a>>=b>>=c", BinaryExpression::class.java, "(a >>= (b >>= c))")
        assertResult(parser, "a>>>=b>>>=c", BinaryExpression::class.java, "(a >>>= (b >>>= c))")
        assertResult(parser, "a<<=b<<=c", BinaryExpression::class.java, "(a <<= (b <<= c))")
        assertResult(parser, "a^=b^=c", BinaryExpression::class.java, "(a ^= (b ^= c))")
        assertResult(parser, "Foo::new", ConstructorReference::class.java, "Foo::new")
        assertResult(parser, "Foo.Bar::new", ConstructorReference::class.java, "(Foo.Bar)::new")
        assertResult(parser, "x::new", ConstructorReference::class.java, "x::new")
        assertResult(parser, "Foo::create", MethodReference::class.java, "Foo::create")
        assertResult(parser, "Foo.Bar::create", MethodReference::class.java, "(Foo.Bar)::create")
        assertResult(parser, "x::create", MethodReference::class.java, "x::create")
        assertResult(parser, "Foo::<T>create", MethodReference::class.java, "Foo::<T>create")
        assertResult(parser, "Foo.Bar::<T>create", MethodReference::class.java, "(Foo.Bar)::<T>create")
        assertResult(parser, "x::<String>create", MethodReference::class.java, "x::<String>create")
        assertResult(parser, "() -> a", LambdaExpression::class.java, "() -> a;")
        assertResult(parser, "() -> {a;}", LambdaExpression::class.java, "() -> {a;}")
        assertResult(parser, "x -> {}", LambdaExpression::class.java, "(x) -> {}")
        assertResult(parser, "(x, y) -> {}", LambdaExpression::class.java, "(x, y) -> {}")
        assertResult(parser, "(String x, int y) -> {}", LambdaExpression::class.java, "(String x, int y) -> {}")
        assertResult(parser, "(String x, int y) -> {xyz;}", LambdaExpression::class.java, "(String x, int y) -> {xyz;}")
    }

    @Test fun testArrayInitializer() {
        val parser = ExpressionParser.arrayInitializer(ExpressionParser.IDENTIFIER)
        assertResult(parser, "{}", ArrayInitializer::class.java, "{}")
        assertResult(parser, "{foo,bar}", ArrayInitializer::class.java, "{foo, bar}")
        assertResult(parser, "{foo,bar,}", ArrayInitializer::class.java, "{foo, bar}")
    }

    companion object {
        private val EMPTY_BODY = DeclarationParser.body(never())
        fun literal(i: Int): IntegerLiteral {
            return IntegerLiteral(IntegerLiteral.Radix.DEC, Integer.toString(i), NumberType.INT)
        }
    }
}