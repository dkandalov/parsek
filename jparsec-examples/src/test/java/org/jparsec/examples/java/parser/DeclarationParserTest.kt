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

import org.jparsec.examples.java.ast.declaration.*
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertFailure
import org.jparsec.examples.java.parser.TerminalParserTest.Companion.assertResult
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Unit test for [DeclarationParser].
 *
 * @author Ben Yu
 */
class DeclarationParserTest {
    @Test fun testRemoveNulls() {
        val list: List<*> = ArrayList(listOf("a", "b", null, "1", "2", null))
        DeclarationParser.removeNulls(list)
        assertEquals(listOf("a", "b", "1", "2"), list)
    }

    @Test fun testFieldDef() {
        assertResult(FIELD, "int f;", FieldDef::class.java, "int f;")
        assertResult(FIELD, "static final int f;", FieldDef::class.java, "static final int f;")
        assertResult(FIELD, "int f = foo;", FieldDef::class.java, "int f = foo;")
        assertResult(FIELD, "int[] a = {foo};", FieldDef::class.java, "int[] a = {foo};")
    }

    @Test fun testBody() {
        val parser = DeclarationParser.body(FIELD)
        assertResult(parser, "{}", DefBody::class.java, "{}")
        assertResult(parser, "{int f;}", DefBody::class.java, "{int f;}")
        assertResult(parser, "{int f=foo; int g;}", DefBody::class.java, "{int f = foo; int g;}")
        assertResult(parser, "{;int f=foo;;; int g;;}", DefBody::class.java, "{int f = foo; int g;}")
    }

    @Test fun testTypeParameter() {
        val parser = DeclarationParser.TYPE_PARAMETER
        assertResult(parser, "T", TypeParameterDef::class.java, "T")
        assertResult(parser, "T extends F", TypeParameterDef::class.java, "T extends F")
        assertResult(parser, "T extends Enum<T>", TypeParameterDef::class.java, "T extends Enum<T>")
        assertResult(parser, "T extends Enum<?>", TypeParameterDef::class.java, "T extends Enum<?>")
        assertFailure(parser, "T extends ?", 1, 11, "? encountered.")
    }

    @Test fun testMethodDef() {
        val parser = DeclarationParser.methodDef(
            StatementParser.SYSTEM_MODIFIER, ExpressionParser.IDENTIFIER, StatementParser.BREAK)
        assertResult(parser, "public static void f();", MethodDef::class.java, "public static void f();")
        assertResult(parser, "String f() default foo;", MethodDef::class.java, "String f() default foo;")
        assertResult(parser, "void f() throws E;",
                     MethodDef::class.java, "void f() throws E;")
        assertResult(parser, "void f() throws E, F<T>;",
                     MethodDef::class.java, "void f() throws E, F<T>;")
        assertFailure(parser, "void f() throws", 1, 16)
        assertFailure(parser, "void f() throws E[];", 1, 18)
        assertResult(parser, "void f() {}", MethodDef::class.java, "void f() {}")
        assertResult(parser, "void f() {break; break;}",
                     MethodDef::class.java, "void f() {break; break;}")
        assertResult(parser, "void f(int i) {}",
                     MethodDef::class.java, "void f(int i) {}")
        assertResult(parser, "void f(final int i, List<Foo> l) {}",
                     MethodDef::class.java, "void f(final int i, List<Foo> l) {}")
        assertResult(parser, "<K, V extends K> void f(int i) {}",
                     MethodDef::class.java, "<K, V extends K> void f(int i) {}")
    }

    @Test fun testConstructorDef() {
        val parser = DeclarationParser.constructorDef(
            StatementParser.SYSTEM_MODIFIER, StatementParser.BREAK)
        assertResult(parser, "public Foo(){}", ConstructorDef::class.java, "public Foo() {}")
        assertResult(parser, "Foo() throws E{break;}", ConstructorDef::class.java, "Foo() throws E {break;}")
        assertResult(parser, "Foo(int i) {}", ConstructorDef::class.java, "Foo(int i) {}")
        assertResult(parser, "Foo(final int i, List<Foo> l) {}",
                     ConstructorDef::class.java, "Foo(final int i, List<Foo> l) {}")
    }

    @Test fun testInitializerDef() {
        val parser = DeclarationParser.initializerDef(StatementParser.BREAK)
        assertResult(parser, "static {}", ClassInitializerDef::class.java, "static {}")
        assertResult(parser, "static {break;}", ClassInitializerDef::class.java, "static {break;}")
        assertResult(parser, " {}", ClassInitializerDef::class.java, "{}")
        assertResult(parser, " {break;}", ClassInitializerDef::class.java, "{break;}")
    }

    @Test fun testClassDef() {
        val parser = DeclarationParser.classDef(StatementParser.SYSTEM_MODIFIER, FIELD)
        assertResult(parser, "public final class Foo {}", ClassDef::class.java, "public final class Foo {}")
        assertResult(parser, "final class Foo<T> {}", ClassDef::class.java, "final class Foo<T> {}")
        assertResult(parser, "final class Foo<T extends Foo<T>, K> {}",
                     ClassDef::class.java, "final class Foo<T extends Foo<T>, K> {}")
        assertResult(parser, "final class Foo<T extends Foo<T>> extends ArrayList<?> {}",
                     ClassDef::class.java, "final class Foo<T extends Foo<T>> extends ArrayList<?> {}")
        assertResult(parser, "final class Foo<T extends Foo<T>> implements List<?> {}",
                     ClassDef::class.java, "final class Foo<T extends Foo<T>> implements List<?> {}")
        assertResult(parser, "final class Foo<T extends Foo<T>> implements List<?>, Iterable<T> {}",
                     ClassDef::class.java, "final class Foo<T extends Foo<T>> implements List<?>, Iterable<T> {}")
        assertResult(parser, "final class Foo<T extends Foo<T>> {public static final String S = foo;}",
                     ClassDef::class.java, "final class Foo<T extends Foo<T>> {public static final String S = foo;}")
        assertResult(parser, "final class Foo<T extends Foo<T>> {int i; int j;}",
                     ClassDef::class.java, "final class Foo<T extends Foo<T>> {int i; int j;}")
    }

    @Test fun testInterfaceDef() {
        val parser = DeclarationParser.interfaceDef(StatementParser.SYSTEM_MODIFIER, FIELD)
        assertResult(parser, "public native interface Foo {}",
                     InterfaceDef::class.java, "public native interface Foo {}")
        assertResult(parser, "interface Foo<T> {}", InterfaceDef::class.java, "interface Foo<T> {}")
        assertResult(parser, "interface Foo<T extends Foo<T>, K> {}",
                     InterfaceDef::class.java, "interface Foo<T extends Foo<T>, K> {}")
        assertResult(parser, "interface Foo<T extends Foo<T>> extends List<?> {}",
                     InterfaceDef::class.java, "interface Foo<T extends Foo<T>> extends List<?> {}")
        assertFailure(parser, "interface Foo implements List {}", 1, 15, "implements encountered.")
        assertResult(parser, "interface Foo<T extends Foo<T>> extends List<?>, Iterable<T> {}",
                     InterfaceDef::class.java, "interface Foo<T extends Foo<T>> extends List<?>, Iterable<T> {}")
        assertResult(parser, "interface Foo<T extends Foo<T>> {public static String S = foo;}",
                     InterfaceDef::class.java, "interface Foo<T extends Foo<T>> {public static String S = foo;}")
        assertResult(parser, "interface Foo<T extends Foo<T>> {int i; int j;}",
                     InterfaceDef::class.java, "interface Foo<T extends Foo<T>> {int i; int j;}")
    }

    @Test fun testAnnotationDef() {
        val parser = DeclarationParser.annotationDef(StatementParser.modifier(ExpressionParser.IDENTIFIER), FIELD)
        assertResult(parser, "@interface Foo{}", AnnotationDef::class.java, "@interface Foo {}")
        assertResult(parser, "@Target({METHOD, FIELD}) @RetentionPolicy(RUNTIME) @interface Foo{}",
                     AnnotationDef::class.java, "@Target({METHOD, FIELD}) @RetentionPolicy(RUNTIME) @interface Foo {}")
        assertResult(parser, "@interface Foo{int i;int j;}",
                     AnnotationDef::class.java, "@interface Foo {int i; int j;}")
    }

    @Test fun testEnumDef() {
        val parser = DeclarationParser.enumDef(ExpressionParser.IDENTIFIER, FIELD)
        assertResult(parser, "enum Foo {}", EnumDef::class.java, "enum Foo {}")
        assertResult(parser, "enum Foo {FOO{int x;}}", EnumDef::class.java, "enum Foo {FOO {int x;}}")
        assertResult(parser, "@For(Test) enum Foo {}", EnumDef::class.java, "@For(Test) enum Foo {}")
        assertResult(parser, "enum Foo implements Comparable<Foo>, Serializable {}",
                     EnumDef::class.java, "enum Foo implements Comparable<Foo>, Serializable {}")
        assertResult(parser, "enum Foo {ONE, TWO(two); int i; int j;}",
                     EnumDef::class.java, "enum Foo {ONE, TWO(two); int i; int j;}")
        assertResult(parser, "enum Foo {ONE, TWO}", EnumDef::class.java, "enum Foo {ONE, TWO}")
    }

    @Test fun testQualifiedName() {
        val parser = DeclarationParser.QUALIFIED_NAME
        assertResult(parser, "foo.bar", QualifiedName::class.java, "foo.bar")
        assertResult(parser, "foo", QualifiedName::class.java, "foo")
    }

    @Test fun testPackage() {
        val parser = DeclarationParser.PACKAGE
        assertResult(parser, "package foo.bar;", QualifiedName::class.java, "foo.bar")
        assertResult(parser, "package foo;", QualifiedName::class.java, "foo")
    }

    @Test fun testImport() {
        val parser = DeclarationParser.IMPORT
        assertResult(parser, "import foo;", Import::class.java, "import foo;")
        assertResult(parser, "import foo.bar;", Import::class.java, "import foo.bar;")
        assertResult(parser, "import foo.bar.*;", Import::class.java, "import foo.bar.*;")
        assertResult(parser, "import static foo;", Import::class.java, "import static foo;")
        assertResult(parser, "import static foo.*;", Import::class.java, "import static foo.*;")
    }

    @Test fun testProgram() {
        val parser = DeclarationParser.program()
        assertResult(parser, "package foo; import foo.bar.*; class Foo {int[] a = {1}; Foo(){}}",
                     Program::class.java, "package foo; import foo.bar.*; class Foo {int[] a = {1}; Foo() {}}")
        assertResult(parser, "class Foo {{} static {}}",
                     Program::class.java, "class Foo {{} static {}}")
        assertResult(parser, "package foo; import foo.bar.*; enum Foo {}",
                     Program::class.java, "package foo; import foo.bar.*; enum Foo {}")
        assertResult(parser, "enum Foo {;static {1;} static {2;} {3;} {4;}}",
                     Program::class.java, "enum Foo {; static {1;} static {2;} {3;} {4;}}")
        assertResult(parser, "package foo; import foo.bar.*; interface Foo {int i = 1;}",
                     Program::class.java, "package foo; import foo.bar.*; interface Foo {int i = 1;}")
        assertResult(parser, "package foo; import foo.bar.*; @interface Foo {int[] value() default {1};}",
                     Program::class.java, "package foo; import foo.bar.*; @interface Foo {int[] value() default {1};}")
        assertResult(parser, "import foo.bar.*; class Foo<T> implements Bar {} interface Bar {}",
                     Program::class.java, "import foo.bar.*; class Foo<T> implements Bar {} interface Bar {}")
        assertResult(parser, "class Foo {class Bar {}}",
                     Program::class.java, "class Foo {class Bar {}}")
        assertResult(parser, "class Foo {private static final class Bar {}}",
                     Program::class.java, "class Foo {private static final class Bar {}}")
        assertResult(parser, "class Foo {enum Bar {B}}",
                     Program::class.java, "class Foo {enum Bar {B}}")
        assertResult(parser, "class Foo {@interface Bar {;;}}",
                     Program::class.java, "class Foo {@interface Bar {}}")
    }

    companion object {
        private val FIELD = DeclarationParser.fieldDef(ExpressionParser.IDENTIFIER)
    }
}