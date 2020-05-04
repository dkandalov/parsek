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
package org.jparsec.error

import org.jparsec.ParseTree
import org.jparsec.error.ErrorReporter.toString

/**
 * Is thrown when any grammar error happens or any exception is thrown during parsing.
 *
 * @author Ben Yu
 */
class ParserException: RuntimeException {
    /**
     * Returns the detailed description of the error, or `null` if none.
     */
    val errorDetails: ParseErrorDetails?

    /**
     * Returns the location of the error.
     *
     */
    @get:Deprecated("Use {@link #getLine} and {@link #getColumn} instead.")
    @JvmField val location: Location

    /**
     * Returns the parse tree until the parse error happened, when
     * [parseTree()][Parser.parseTree] was invoked.
     * `null` if absent.
     *
     * @since 2.3
     */
    var parseTree: ParseTree? = null

    /** Returns the module name, or `null` if none.  */
    @get:Deprecated("")
    @Deprecated("")
    val moduleName: String?

    /**
     * Creates a [ParserException] object.
     *
     * @param details the [ParseErrorDetails] that describes the error details.
     * @param location the error location.
     */
    constructor(details: ParseErrorDetails?, location: Location): this(details, null, location) {}

    /**
     * Creates a [ParserException] object.
     *
     * @param details the [ParseErrorDetails] that describes the error details.
     * @param moduleName the module name.
     * @param location the error location.
     */
    @Deprecated("")
    constructor(details: ParseErrorDetails?, moduleName: String?, location: Location): super(toErrorMessage(null, moduleName, details, location)) {
        errorDetails = details
        this.moduleName = moduleName
        this.location = location
    }

    /**
     * Creates a [ParserException] object.
     *
     * @param cause the exception that causes this.
     * @param details the [ParseErrorDetails] that describes the error details.
     * @param moduleName the module name.
     * @param location the location.
     */
    @Deprecated("")
    constructor(
        cause: Throwable, details: ParseErrorDetails?, moduleName: String?, location: Location): super(toErrorMessage(cause.message, moduleName, details, location), cause) {
        errorDetails = details
        this.location = location
        this.moduleName = moduleName
    }

    /**
     * Returns the line where the error occurred.
     *
     * @since 3.1
     */
    val line: Int
        get() = location.line

    /**
     * Returns the column where the error occurred.
     *
     * @since 3.1
     */
    val column: Int
        get() = location.column

    companion object {
        private fun toErrorMessage(
            message: String?, module: String?, details: ParseErrorDetails?, location: Location): String {
            val buf = StringBuilder()
            if (message != null && message.length > 0) {
                buf.append(message).append('\n')
            }
            if (module != null) {
                buf.append('(').append(module).append(") ")
            }
            buf.append(toString(details, location))
            return buf.toString()
        }
    }
}