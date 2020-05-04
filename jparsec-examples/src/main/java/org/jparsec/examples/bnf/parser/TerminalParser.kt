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
package org.jparsec.examples.bnf.parser

import org.jparsec.Indentation
import org.jparsec.Parser
import org.jparsec.Parsers.or
import org.jparsec.Scanners.lineComment
import org.jparsec.Terminals
import org.jparsec.Terminals.Companion.operators

/**
 * Parses terminals in a bnf.
 *
 * @author benyu
 */
object TerminalParser {
    private val OPERATORS = arrayOf("*", "+", "?", "|", "::=", "(", ")")
    private val TERMS = operators(*OPERATORS)
    private val COMMENT = lineComment("#")
    private val LITERAL = or<String?>(
        Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
        Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER)
    private val IDENT: Parser<*> = Terminals.Identifier.TOKENIZER
    val TOKENIZER: Parser<*> = or(TERMS.tokenizer(), LITERAL, IDENT)
    val INDENTATION = Indentation()
    fun term(name: String?): Parser<*> {
        return TERMS.token(name!!)
    }

    fun <T> parse(parser: Parser<T>, source: String?): T {
        return parser.from(INDENTATION.lexer(TOKENIZER, Indentation.WHITESPACES.or(COMMENT).many()))
            .parse(source)
    }
}