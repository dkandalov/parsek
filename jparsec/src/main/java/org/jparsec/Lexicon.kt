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

import org.jparsec.InternalFunctors.tokenWithSameValue
import org.jparsec.Parsers.never
import org.jparsec.Parsers.or
import org.jparsec.Parsers.sequence
import org.jparsec.Parsers.token
import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.Checks.checkArgument
import java.util.function.Function

/**
 * A [Lexicon] is a group of lexical words that can be tokenized by a single tokenizer.
 *
 * @author Ben Yu
 */
open class Lexicon(
    /** Maps lexical word name to token value.  */
    @JvmField val words: Function<String, Any?>,
    /** The scanner that recognizes any of the lexical word.  */
    @JvmField val tokenizer: Parser<*>
) {

    /**
     * Returns the tokenizer that tokenizes all terminals (operators, keywords, identifiers etc.)
     * managed in this instance.
     */
    fun tokenizer(): Parser<*> {
        return tokenizer
    }

    /**
     * A [Parser] that recognizes a sequence of tokens identified by `tokenNames`, as an
     * atomic step.
     */
    fun phrase(vararg tokenNames: String): Parser<String> {
        val wordParsers = tokenNames.map { token(it) }
        val phrase = tokenNames.joinToString(" ")
        return sequence(wordParsers).atomic().retn(phrase).label(phrase)
    }

    /** A [Parser] that recognizes a token identified by any of `tokenNames`.  */
    fun token(vararg tokenNames: String): Parser<Token?> {
        if (tokenNames.isEmpty()) return never()
        val ps: List<Parser<Token?>> = tokenNames.map { token(tokenWithSameValue(word(it))) }
        return or(ps)
    }

    /** A [Parser] that recognizes the token identified by `tokenName`.  */
    fun token(tokenName: String): Parser<Token?> {
        return token(tokenWithSameValue(word(tokenName)))
    }

    /**
     * Gets the token value identified by the token text. This text is the operator or the keyword.
     *
     * @param name the token text.
     * @return the token object.
     * @exception IllegalArgumentException if the token object does not exist.
     */
    @Private fun word(name: String): Any {
        val p = words.apply(name)
        checkArgument(p != null, "token %s unavailable", name)
        return p!!
    }

    /** Returns a [Lexicon] instance that's a union of `this` and `that`.  */
    fun union(that: Lexicon): Lexicon {
        return Lexicon(fallback(words, that.words), or(tokenizer, that.tokenizer))
    }

    companion object {
        /**
         * Returns a [Function] that delegates to `function` and falls back to
         * `defaultFunction` for null return values.
         */
        fun <F, T> fallback(
            function: Function<F, T>, defaultFunction: Function<in F, out T>): Function<F, T> {
            return Function { from: F ->
                val result: T? = function.apply(from)
                result ?: defaultFunction.apply(from)
            }
        }
    }
}