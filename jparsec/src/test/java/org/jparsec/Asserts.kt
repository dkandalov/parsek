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

import org.jparsec.error.ParserException
import org.junit.Assert
import org.junit.Assert.*

/**
 * Extra assertions.
 *
 * @author Ben Yu
 */
object Asserts {
    @JvmStatic fun assertFailure(
        mode: Parser.Mode, parser: Parser<*>, source: String?, line: Int, column: Int) {
        try {
            parser.parse(source, mode)
            fail()
        } catch (e: ParserException) {
            assertEquals(line.toLong(), e.line.toLong())
            assertEquals(column.toLong(), e.column.toLong())
        }
    }

    @JvmStatic fun assertFailure(
        mode: Parser.Mode, parser: Parser<*>,
        source: String?, line: Int, column: Int, expectedMessage: String?) {
        try {
            parser.parse(source, mode)
            fail()
        } catch (e: ParserException) {
            assertTrue(e.message, e.message!!.contains(expectedMessage!!))
            assertEquals(line.toLong(), e.line.toLong())
            assertEquals(column.toLong(), e.column.toLong())
        }
    }

    @JvmStatic fun assertFailure(
        parser: Parser<*>, source: String?, line: Int, column: Int,
        module: String?, expectedMessage: String?) {
        try {
            parser.parse(source, module)
            fail()
        } catch (e: ParserException) {
            assertTrue(e.message, e.message!!.contains(module!!))
            assertTrue(e.message, e.message!!.contains(expectedMessage!!))
            assertEquals(line.toLong(), e.line.toLong())
            assertEquals(column.toLong(), e.column.toLong())
        }
    }

    @JvmStatic fun assertFailure(mode: Parser.Mode, parser: Parser<*>, source: String?, line: Int, column: Int, cause: Class<out Throwable?>) {
        try {
            parser.parse(source, mode)
            fail()
        } catch (e: ParserException) {
            assertEquals(line.toLong(), e.line.toLong())
            assertEquals(column.toLong(), e.column.toLong())
            assertTrue(cause.isInstance(e.cause))
        }
    }

    @JvmStatic fun assertParser(mode: Parser.Mode, parser: Parser<*>, source: String, value: Any?, rest: String) {
        assertEquals(value, parser.followedBy(Scanners.string(rest)).parse(source, mode))
    }

    fun <T> assertArrayEquals(actual: Array<T>, vararg expected: T) {
        assertEquals(listOf(*expected), listOf(*actual))
    }

    fun assertScanner(mode: Parser.Mode, scanner: Parser<Unit>, source: String?, remaining: String) {
        Assert.assertNull(scanner.followedBy(Scanners.string(remaining)).parse(source, mode))
    }

    fun assertStringScanner(mode: Parser.Mode, scanner: Parser<String>, source: String, remaining: String) {
        assertEquals(source.substring(0, source.length - remaining.length),
                            scanner.followedBy(Scanners.string(remaining)).parse(source, mode))
    }

    fun assertStringScanner(mode: Parser.Mode, scanner: Parser<String>, source: String?) {
        assertEquals(source, scanner.parse(source, mode))
    }
}