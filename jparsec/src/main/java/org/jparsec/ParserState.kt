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

/**
 * Represents [ParseContext] for token level parsing.
 *
 * @author Ben Yu
 */
internal class ParserState(
    module: String?,
    source: CharSequence,
    private val input: Array<Token?>,
    at: Int,
    locator: SourceLocator, // in case a terminating eof token is not explicitly created, the implicit one is used.
    private val endIndex: Int,
    result: Any?
): ParseContext(source, result, at, module, locator) {
    override val isEof: Boolean
        get() = at >= input.size

    override fun toIndex(pos: Int): Int {
        return if (pos >= input.size) endIndex else input[pos]!!.index()
    }

    override val token: Token
        get() = input[at]!!

    override fun peekChar(): Char {
        throw IllegalStateException(USED_ON_TOKEN_INPUT)
    }

    override fun characters(): CharSequence {
        throw IllegalStateException(USED_ON_TOKEN_INPUT)
    }

    override fun getInputName(pos: Int): String {
        return if (pos >= input.size) EOF else input[pos].toString()
    }

    companion object {
        private const val USED_ON_TOKEN_INPUT = ("Cannot scan characters on tokens."
            + "\nThis normally happens when you are using a character-level parser on token input."
            + " For example: Scanners.string(foo).from(tokenizer).parse(text) will result in this error"
            + " because scanner works on characters while it's used as a token-level parser.")
    }

}