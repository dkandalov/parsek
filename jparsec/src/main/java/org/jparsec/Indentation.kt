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

import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.Lists.arrayList
import org.jparsec.internal.util.Objects
import org.jparsec.pattern.CharPredicate
import org.jparsec.pattern.Pattern
import org.jparsec.pattern.Patterns
import java.util.*

/**
 * Processes indentation based lexical structure according to the
 * [Off-side rule](http://en.wikipedia.org/wiki/Off-side_rule).
 *
 * @author Ben Yu
 */
class Indentation
/** Creates a [Indentation] object that generates default indent and outdent tokens.  */ @JvmOverloads constructor(private val indent: Any = Punctuation.INDENT, private val outdent: Any = Punctuation.OUTDENT) {
    @Private
    internal enum class Punctuation {
        INDENT, OUTDENT, LF
    }

    /** A [Parser] that recognizes the generated `indent` token.  */
    fun indent(): Parser<Token?> {
        return token(indent)
    }

    /** A [Parser] that recognizes the generated `outdent` token.  */
    fun outdent(): Parser<Token?> {
        return token(outdent)
    }

    /**
     * A [Parser] that greedily runs `tokenizer`, and translates line feed characters
     * (`'\n'`) to `indent` and `outdent` tokens.
     * Return values are wrapped in [Token] objects and collected in a [List].
     * Patterns recognized by `delim` are ignored.
     */
    fun lexer(tokenizer: Parser<*>, delim: Parser<*>): Parser<List<Token>> {
        val lf: Parser<*> = Scanners.isChar('\n').retn(Punctuation.LF)
        return Parsers.or(tokenizer, lf).lexer(delim)
            .map { tokens -> analyzeIndentations(tokens, Punctuation.LF) }
    }

    /**
     * Analyzes indentation by looking at the first token after each `lf` and inserting
     * `indent` and `outdent` tokens properly.
     */
    fun analyzeIndentations(tokens: List<Token>, lf: Any?): List<Token> {
        if (tokens.isEmpty()) {
            return tokens
        }
        val size = tokens.size
        val result: MutableList<Token> = arrayList(size + size / 16)
        val indentations = Stack<Int>()
        var freshLine = true
        var lfIndex = 0
        for (token in tokens) {
            if (freshLine) {
                var indentation = token.index() - lfIndex
                if (Objects.equals(token.value(), lf)) {
                    // if first token on a line is lf, indentation is ignored.
                    indentation = 0
                }
                newLine(token, indentations, indentation, result)
            }
            if (Objects.equals(token.value(), lf)) {
                freshLine = true
                lfIndex = token.index() + token.length()
            } else {
                freshLine = false
                result.add(token)
            }
        }
        val lastToken = tokens[tokens.size - 1]
        val endIndex = lastToken.index() + lastToken.length()
        val outdentToken = pseudoToken(endIndex, outdent)
        for (i in 0 until indentations.size - 1) {
            // add outdent for every remaining indentation except the first one
            result.add(outdentToken)
        }
        return result
    }

    private fun newLine(
        token: Token, indentations: Stack<Int>, indentation: Int, result: MutableList<Token>) {
        while (true) {
            if (indentations.isEmpty()) {
                indentations.add(indentation)
                return
            }
            val previousIndentation = indentations.peek()
            if (previousIndentation < indentation) {
                // indent
                indentations.push(indentation)
                result.add(pseudoToken(token.index(), indent))
                return
            } else if (previousIndentation > indentation) {
                // outdent
                indentations.pop()
                if (indentations.isEmpty()) {
                    return
                }
                result.add(pseudoToken(token.index(), outdent))
                continue
            }
            return
        }
    }

    companion object {
        /**
         * A [CharPredicate] that returns true only if the character isn't line feed
         * and [Character.isWhitespace] returns true.
         */
        val INLINE_WHITESPACE: CharPredicate = object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                return c != '\n' && Character.isWhitespace(c)
            }

            override fun toString(): String {
                return "whitespace"
            }
        }

        /**
         * A [Pattern] object that matches a line continuation. i.e. a backslash character
         * (`'\'`) followed by some whitespaces and ended by a line feed character (`'\n'`).
         * Is useful if the line feed character plays a role in the syntax (as in
         * indentation-sensitive languages) and line continuation is supported.
         */
        val LINE_CONTINUATION = Patterns.sequence(
            Patterns.isChar('\\'), Patterns.many(INLINE_WHITESPACE), Patterns.isChar('\n'))

        /**
         * A [Pattern] object that matches one or more whitespace characters or line continuations,
         * where the line feed character (`'\n'`) is escaped by the backslash character
         * (`'\'`).
         */
        val INLINE_WHITESPACES = Patterns.many1(INLINE_WHITESPACE)

        /**
         * A [Parser] that recognizes 1 or more whitespace characters on the same line.
         * Line continutation (escaped by a backslash character `'\'`) is considered the same line.
         */
        @JvmField
        val WHITESPACES = INLINE_WHITESPACES.or(LINE_CONTINUATION).many1().toScanner("whitespaces")
        private fun token(value: Any): Parser<Token?> {
            return Parsers.token(InternalFunctors.tokenWithSameValue(value))
        }

        private fun pseudoToken(index: Int, value: Any): Token {
            return Token(index, 0, value)
        }
    }
    /**
     * Creates an [Indentation] object that uses `indent` and `outdent` as the
     * token values for indentation and outdentation.
     */
}