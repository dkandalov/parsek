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
import org.jparsec.examples.java.ast.expression.*
import org.jparsec.examples.java.ast.statement.Annotation
import org.jparsec.examples.java.ast.statement.Modifier
import org.jparsec.examples.sql.ast.FunctionExpression
import org.junit.Test
import java.io.IOException
import java.net.URL

/**
 * Integration test for the entire java parser.
 *
 * @author benyu
 */
class JavaParserIntegrationTest {
    @Test @Throws(Exception::class) fun testParse() {
        parseJavaSourceFiles(
            DeclarationParser::class.java, StatementParser::class.java, ExpressionParser::class.java,
            TerminalParser::class.java, JavaLexer::class.java, TypeLiteralParser::class.java,
            Program::class.java, Declaration::class.java, Member::class.java, Modifier::class.java,
            FieldDef::class.java, MethodDef::class.java, ConstructorDef::class.java,
            AnnotationDef::class.java, InterfaceDef::class.java, ClassDef::class.java,
            Expression::class.java, NewArrayExpression::class.java, QualifiedExpression::class.java,
            Operator::class.java, NewExpression::class.java, FunctionExpression::class.java, Annotation::class.java)
    }

    companion object {
        @Throws(IOException::class) private fun parseJavaSourceFiles(vararg classes: Class<*>) {
            for (cls in classes) {
                parseJavaSourceFile(cls)
            }
        }

        @Throws(IOException::class) private fun parseJavaSourceFile(cls: Class<*>) {
            DeclarationParser.parse(toSourceUrl(cls))
        }

        private fun toSourceUrl(cls: Class<*>): URL {
            return cls.getResource(cls.simpleName + ".java")
                ?: throw IllegalArgumentException("Cannot find source file for " + cls.name)
        }
    }
}