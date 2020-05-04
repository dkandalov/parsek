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

/**
 * Parser state for scanner.
 *
 * @author Ben Yu
 */
internal class ScannerState: ParseContext {
    private val end: Int

    constructor(source: CharSequence): this(null, source, 0, SourceLocator(source))
    constructor(module: String?, source: CharSequence, from: Int, locator: SourceLocator): super(source, from, module, locator) {
        end = source.length
    }

    /**
     * @param module the current module name for error reporting
     * @param source the source string
     * @param from from where do we start to scan?
     * @param end till where do we stop scanning? (exclusive)
     * @param locator the locator for mapping index to line and column number
     * @param originalResult the original result value
     */
    constructor(
        module: String?,
        source: CharSequence,
        from: Int,
        end: Int,
        locator: SourceLocator,
        originalResult: Any?
    ): super(source, originalResult, from, module, locator) {
        this.end = end
    }

    override fun peekChar(): Char {
        return source[at]
    }

    override val isEof: Boolean
        get() = end == at

    override fun toIndex(pos: Int): Int {
        return pos
    }

    override fun getInputName(pos: Int): String {
        return if (pos >= end) EOF else Character.toString(source[pos])
    }

    override fun characters(): CharSequence {
        return source
    }

    override val token: Token
        get() = throw IllegalStateException("Parser not on token level")

    fun <T> run(parser: Parser<T>): T {
        if (!applyWithExceptionWrapped(parser)) {
            val exception = ParserException(renderError(), module, locator.locate(errorIndex()))
            exception.parseTree = buildErrorParseTree()
            throw exception
        }
        return parser.getReturn(this)
    }

    private fun applyWithExceptionWrapped(parser: Parser<*>): Boolean {
        return try {
            parser.apply(this)
        } catch (e: RuntimeException) {
            if (e is ParserException) throw e
            val wrapper = ParserException(e, null, module, locator.locate(index))
            // Use the successful parse tree because we are interrupted abruptly by an exception
            // So no need to take the "farthest error path".
            wrapper.parseTree = buildParseTree()
            throw wrapper
        }
    }
}