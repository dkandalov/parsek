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

import org.jparsec.examples.java.ast.statement.*
import org.jparsec.examples.java.ast.statement.Annotation
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertFailure
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertParser
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertResult
import org.junit.Test

/**
 * Unit test for [StatementParser].
 *
 * @author Ben Yu
 */
class StatementParserTest {
    @Test fun testNop() {
        assertResult(StatementParser.NOP, ";", NopStatement::class.java, ";")
    }

    @Test fun testSystemModifier() {
        val parser = StatementParser.SYSTEM_MODIFIER
        assertParser(parser, "private", SystemModifier.PRIVATE)
        assertParser(parser, "protected", SystemModifier.PROTECTED)
        assertParser(parser, "public", SystemModifier.PUBLIC)
        assertParser(parser, "static", SystemModifier.STATIC)
        assertParser(parser, "transient", SystemModifier.TRANSIENT)
        assertParser(parser, "volatile", SystemModifier.VOLATILE)
        assertParser(parser, "final", SystemModifier.FINAL)
        assertParser(parser, "abstract", SystemModifier.ABSTRACT)
        assertParser(parser, "synchronized", SystemModifier.SYNCHRONIZED)
        assertParser(parser, "native", SystemModifier.NATIVE)
    }

    @Test fun testAnnotation() {
        val parser = StatementParser.annotation(ExpressionParser.IDENTIFIER)
        assertResult(parser, "@Foo", Annotation::class.java, "@Foo")
        assertResult(parser, "@org.codehaus.jparsec.Foo",
                     Annotation::class.java, "@org.codehaus.jparsec.Foo")
        assertResult(parser, "@Foo()", Annotation::class.java, "@Foo()")
        assertResult(parser, "@Foo(foo)", Annotation::class.java, "@Foo(foo)")
        assertResult(parser, "@Foo(foo=bar)", Annotation::class.java, "@Foo(foo=bar)")
        assertResult(parser, "@Foo(foo={bar})", Annotation::class.java, "@Foo(foo={bar})")
        assertResult(parser, "@Foo(foo={bar}, a=b)", Annotation::class.java, "@Foo(foo={bar}, a=b)")
        assertFailure(parser, "Foo", 1, 1)
        assertFailure(parser, "@Foo({{foo}})", 1, 7)
    }

    @Test fun testModifier() {
        val parser = StatementParser.modifier(ExpressionParser.IDENTIFIER)
        assertParser(parser, "private", SystemModifier.PRIVATE)
        assertResult(parser, "@Foo(foo)", Annotation::class.java, "@Foo(foo)")
    }

    @Test fun testBreak() {
        val parser = StatementParser.BREAK
        assertResult(parser, "break;", BreakStatement::class.java, "break;")
        assertResult(parser, "break foo;", BreakStatement::class.java, "break foo;")
    }

    @Test fun testContinue() {
        val parser = StatementParser.CONTINUE
        assertResult(parser, "continue;", ContinueStatement::class.java, "continue;")
        assertResult(parser, "continue foo;", ContinueStatement::class.java, "continue foo;")
    }

    @Test fun testReturnStatement() {
        val parser = StatementParser.returnStatement(ExpressionParser.IDENTIFIER)
        assertResult(parser, "return;", ReturnStatement::class.java, "return;")
        assertResult(parser, "return foo;", ReturnStatement::class.java, "return foo;")
    }

    @Test fun testBlockStatement() {
        val parser = StatementParser.blockStatement(StatementParser.NOP)
        assertResult(parser, "{}", BlockStatement::class.java, "{}")
        assertResult(parser, "{;}", BlockStatement::class.java, "{;}")
        assertResult(parser, "{;;}", BlockStatement::class.java, "{; ;}")
    }

    @Test fun testSynchronizedBlockStatement() {
        val parser = StatementParser.synchronizedBlock(SIMPLE_STATEMENT)
        assertResult(parser, "synchronized {foo;}",
                     SynchronizedBlockStatement::class.java, "synchronized {foo;}")
    }

    @Test fun testWhileStatement() {
        val parser = StatementParser.whileStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT)
        assertResult(parser, "while(foo) bar;", WhileStatement::class.java, "while (foo) bar;")
    }

    @Test fun testDoWhileStatement() {
        val parser = StatementParser.doWhileStatement(SIMPLE_STATEMENT, ExpressionParser.IDENTIFIER)
        assertResult(parser, "do bar;while(foo);", DoWhileStatement::class.java, "do bar; while (foo);")
    }

    @Test fun testIfStatement() {
        val parser = StatementParser.ifStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT)
        assertResult(parser, "if (foo) bar;", IfStatement::class.java, "if (foo) bar;")
        assertResult(parser, "if (foo) bar; else baz;", IfStatement::class.java, "if (foo) bar; else baz;")
        assertResult(parser, "if (foo) bar; else if(baz) baz;",
                     IfStatement::class.java, "if (foo) bar; else if (baz) baz;")
    }

    @Test fun testSwitchStatement() {
        val parser = StatementParser.switchStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT)
        assertResult(parser, "switch (foo) {}", SwitchStatement::class.java, "switch (foo) {}")
        assertResult(parser, "switch (foo) { default:}", SwitchStatement::class.java, "switch (foo) {}")
        assertResult(parser, "switch (foo) { case foo:}",
                     SwitchStatement::class.java, "switch (foo) { case foo:}")
        assertResult(parser, "switch (foo) { case foo: case bar: baz;}",
                     SwitchStatement::class.java, "switch (foo) { case foo: case bar: baz;}")
        assertResult(parser, "switch (foo) { case foo: case bar: baz; default: x;}",
                     SwitchStatement::class.java, "switch (foo) { case foo: case bar: baz; default: x;}")
    }

    @Test fun testForeachStatement() {
        val parser = StatementParser.foreachStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT)
        assertResult(parser, "for(Foo foo : foos) bar;",
                     ForeachStatement::class.java, "for (Foo foo : foos) bar;")
    }

    @Test fun testVarStatement() {
        val parser = StatementParser.varStatement(ExpressionParser.IDENTIFIER)
        assertResult(parser, "int i;", VarStatement::class.java, "int i;")
        assertResult(parser, "int i=n;", VarStatement::class.java, "int i = n;")
        assertResult(parser, "final int i=n;", VarStatement::class.java, "final int i = n;")
        assertResult(parser, "final int[] a1={}, a2, a3={m, n};",
                     VarStatement::class.java, "final int[] a1 = {}, a2, a3 = {m, n};")
    }

    @Test fun testForStatement() {
        val parser = StatementParser.forStatement(ExpressionParser.IDENTIFIER, SIMPLE_STATEMENT)
        assertResult(parser, "for(;;)foo;", ForStatement::class.java, "for (;;) foo;")
        assertResult(parser, "for(int i=m;;)foo;", ForStatement::class.java, "for (int i = m;;) foo;")
        assertResult(parser, "for(init;;)foo;", ForStatement::class.java, "for (init;;) foo;")
        assertResult(parser, "for(init1,init2;;)foo;", ForStatement::class.java, "for (init1, init2;;) foo;")
        assertResult(parser, "for(;cond;)foo;", ForStatement::class.java, "for (;cond;) foo;")
        assertResult(parser, "for(;;a)foo;", ForStatement::class.java, "for (;;a) foo;")
        assertResult(parser, "for(;;a,b)foo;", ForStatement::class.java, "for (;;a, b) foo;")
        assertResult(parser, "for(int i=m, j=n;cond;a,b)foo;",
                     ForStatement::class.java, "for (int i = m, j = n;cond;a, b) foo;")
    }

    @Test fun testExpression() {
        assertResult(SIMPLE_STATEMENT, "foo;", ExpressionStatement::class.java, "foo;")
    }

    @Test fun testExpressionList() {
        val parser = StatementParser.expressionList(ExpressionParser.IDENTIFIER)
        assertResult(parser, "foo;", ExpressionListStatement::class.java, "foo;")
        assertResult(parser, "foo,bar;", ExpressionListStatement::class.java, "foo, bar;")
    }

    @Test fun testAssertStatement() {
        val parser = StatementParser.assertStatement(ExpressionParser.IDENTIFIER)
        assertResult(parser, "assert foo;", AssertStatement::class.java, "assert foo;")
        assertResult(parser, "assert foo : bar;", AssertStatement::class.java, "assert foo : bar;")
    }

    @Test fun testParameter() {
        val parser = StatementParser.parameter(StatementParser.SYSTEM_MODIFIER)
        assertResult(parser, "int f", ParameterDef::class.java, "int f")
        assertResult(parser, "final int[] f", ParameterDef::class.java, "final int[] f")
        assertResult(parser, "static final int[] f", ParameterDef::class.java, "static final int[] f")
        assertResult(parser, "final int[]... f", ParameterDef::class.java, "final int[]... f")
        assertResult(parser, "final List<Integer>... f",
                     ParameterDef::class.java, "final List<Integer>... f")
        assertResult(parser, "final int... f", ParameterDef::class.java, "final int... f")
    }

    @Test fun testTryStatement() {
        val parser = StatementParser.tryStatement(StatementParser.SYSTEM_MODIFIER, SIMPLE_STATEMENT)
        assertResult(parser, "try {foo;} catch(E e) {bar;}",
                     TryStatement::class.java, "try {foo;} catch (E e) {bar;}")
        assertResult(parser, "try {foo;} catch(E e) {bar;}catch(E2 e) {baz;}",
                     TryStatement::class.java, "try {foo;} catch (E e) {bar;} catch (E2 e) {baz;}")
        assertResult(parser, "try {foo;} finally{bar;}",
                     TryStatement::class.java, "try {foo;} finally {bar;}")
        assertResult(parser, "try {foo;} catch(E e){bar;}finally{bar;}",
                     TryStatement::class.java, "try {foo;} catch (E e) {bar;} finally {bar;}")
        assertResult(parser, "try {foo;} catch(E e){bar;}catch(E e2){bar2;}finally{bar;}",
                     TryStatement::class.java, "try {foo;} catch (E e) {bar;} catch (E e2) {bar2;} finally {bar;}")
        assertFailure(parser, "try{foo;}catch(E e1, E e2){bar;}", 1, 20)
    }

    @Test fun testThrowStatement() {
        val parser = StatementParser.throwStatement(ExpressionParser.IDENTIFIER)
        assertResult(parser, "throw foo;", ThrowStatement::class.java, "throw foo;")
    }

    @Test fun testThisCallStatement() {
        val parser = StatementParser.thisCall(ExpressionParser.IDENTIFIER)
        assertResult(parser, "this();", ThisCallStatement::class.java, "this();")
        assertResult(parser, "this(foo);", ThisCallStatement::class.java, "this(foo);")
        assertResult(parser, "this(foo,bar);", ThisCallStatement::class.java, "this(foo, bar);")
    }

    @Test fun testSuperCallStatement() {
        val parser = StatementParser.superCall(ExpressionParser.IDENTIFIER)
        assertResult(parser, "super();", SuperCallStatement::class.java, "super();")
        assertResult(parser, "super(foo);", SuperCallStatement::class.java, "super(foo);")
        assertResult(parser, "super(foo,bar);", SuperCallStatement::class.java, "super(foo, bar);")
    }

    // Makes sure the parts are correctly aggregated to create the statement parser.
    @Test fun testStatement() {
        val parser = StatementParser.statement(ExpressionParser.IDENTIFIER)
        assertResult(parser, ";", NopStatement::class.java, ";")
        assertResult(parser, "foo: bar;", LabelStatement::class.java, "foo: bar;")
        assertResult(parser, "foo: bar: baz;", LabelStatement::class.java, "foo: bar: baz;")
        assertResult(parser, "break foo;", BreakStatement::class.java, "break foo;")
        assertResult(parser, "continue foo;", ContinueStatement::class.java, "continue foo;")
        assertResult(parser, "return foo;", ReturnStatement::class.java, "return foo;")
        assertResult(parser, "foo;", ExpressionStatement::class.java, "foo;")
        assertResult(parser, "{foo;}", BlockStatement::class.java, "{foo;}")
        assertResult(parser, "synchronized {foo;}",
                     SynchronizedBlockStatement::class.java, "synchronized {foo;}")
        assertResult(parser, "while(foo) bar;", WhileStatement::class.java, "while (foo) bar;")
        assertResult(parser, "do bar;while(foo);", DoWhileStatement::class.java, "do bar; while (foo);")
        assertResult(parser, "if (foo) bar; else if(baz) baz;",
                     IfStatement::class.java, "if (foo) bar; else if (baz) baz;")
        assertResult(parser, "switch (foo) { case foo: case bar: baz; default: x;}",
                     SwitchStatement::class.java, "switch (foo) { case foo: case bar: baz; default: x;}")
        assertResult(parser, "for(Foo<Bar>[][] foo : foos) bar;",
                     ForeachStatement::class.java, "for (Foo<Bar>[][] foo : foos) bar;")
        assertResult(parser, "for(int i=m, j=n;cond;a,b)foo;",
                     ForStatement::class.java, "for (int i = m, j = n;cond;a, b) foo;")
        assertResult(parser, "for(init1,init2;;)foo;", ForStatement::class.java, "for (init1, init2;;) foo;")
        assertResult(parser, "final int[] a1={}, a2, a3={m, n};",
                     VarStatement::class.java, "final int[] a1 = {}, a2, a3 = {m, n};")
        assertResult(parser, "assert foo : bar;", AssertStatement::class.java, "assert foo : bar;")
        assertResult(parser, "try {foo;} catch(E e){bar;}catch(E e2){bar2;}finally{bar;}",
                     TryStatement::class.java, "try {foo;} catch (E e) {bar;} catch (E e2) {bar2;} finally {bar;}")
        assertResult(parser, "throw foo;", ThrowStatement::class.java, "throw foo;")
        assertResult(parser, "this(foo, bar);", ThisCallStatement::class.java, "this(foo, bar);")
        assertResult(parser, "super(foo, bar);", SuperCallStatement::class.java, "super(foo, bar);")
    }

    companion object {
        private val SIMPLE_STATEMENT = StatementParser.expression(ExpressionParser.IDENTIFIER)
    }
}