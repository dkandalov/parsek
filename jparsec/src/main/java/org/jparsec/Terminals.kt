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

import org.jparsec.Keywords.lexicon
import org.jparsec.Operators.lexicon
import org.jparsec.Parsers.or
import org.jparsec.Parsers.token
import org.jparsec.Parsers.tokenType
import org.jparsec.Tokens.Fragment
import org.jparsec.Tokens.ScientificNotation
import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.Checks.checkArgument
import org.jparsec.internal.util.Checks.checkNotNull
import org.jparsec.internal.util.Objects.`in`
import java.util.*
import java.util.function.Function

/**
 * Provides convenient API to build lexer and parsers for terminals.
 * The following example is a parser snippet for Java generic type expression such as
 * `List<String>`: <pre>   `Terminals terms = Terminals
 * .operators("?", "<", ">", ",")
 * .words(Scanners.IDENTIFIER)
 * .keywords("super", "extends")
 * .build();
 * Parser<String> typeName = Terminals.identifier();
 * Parser<?> wildcardWithUpperBound = terms.phrase("?", "extends");
 * ...
 * parser.from(terms.tokenizer(), Scanners.WHITESPACES.optional()).parse("List<String>");
`</pre> *
 *
 * @author Ben Yu
 */
class Terminals private constructor(lexicon: Lexicon): Lexicon(lexicon.words, lexicon.tokenizer) {
    /** Entry point for parser and tokenizers of character literal.  */
    object CharLiteral {
        /** [Parser] that recognizes [Character] tokens.  */
        @JvmField
        val PARSER = tokenType(Character::class.java, "character literal")

        /**
         * A tokenizer that parses single quoted character literal (escaped by `'\'`),
         * and then converts the character to a [Character] token.
         */
        @JvmField
        val SINGLE_QUOTE_TOKENIZER = Scanners.SINGLE_QUOTE_CHAR.map(TokenizerMaps.SINGLE_QUOTE_CHAR)
    }

    /** Entry point for parser and tokenizers of string literal.  */
    object StringLiteral {
        /** [Parser] that recognizes [String] tokens.  */
        @JvmField
        val PARSER = tokenType(String::class.java, "string literal")

        /**
         * A tokenizer that parses double quoted string literal (escaped by `'\'`),
         * and transforms the quoted content by applying escape characters.
         */
        @JvmField
        val DOUBLE_QUOTE_TOKENIZER = Scanners.DOUBLE_QUOTE_STRING.map(TokenizerMaps.DOUBLE_QUOTE_STRING)

        /**
         * A tokenizer that parses single quoted string literal (single quote is escaped with
         * another single quote), and transforms the quoted content by applying escape characters.
         */
        @JvmField
        val SINGLE_QUOTE_TOKENIZER = Scanners.SINGLE_QUOTE_STRING.map(TokenizerMaps.SINGLE_QUOTE_STRING)
    }

    /** Entry point for parser and tokenizers of integral number literal represented as [Long].  */
    object LongLiteral {
        /** [Parser] that recognizes [Long] tokens.  */
        val PARSER = tokenType(java.lang.Long::class.java, "integer literal")

        /**
         * A tokenizer that parses a decimal integer number (valid patterns are: `1, 10, 123`),
         * and converts the string to a [Long] value.
         */
        val DEC_TOKENIZER = Scanners.DEC_INTEGER.map(TokenizerMaps.DEC_AS_LONG)

        /**
         * A tokenizer that parses a octal integer number (valid patterns are:
         * `0, 07, 017, 0371` etc.), and converts the string to a [Long] value.
         *
         *
         *  An octal number has to start with 0.
         */
        val OCT_TOKENIZER = Scanners.OCT_INTEGER.map(TokenizerMaps.OCT_AS_LONG)

        /**
         * A tokenizer that parses a hex integer number (valid patterns are:
         * `0x1, 0Xff, 0xFe1` etc.), and converts the string to a [Long] value.
         *
         *
         *  A hex number has to start with either 0x or 0X.
         */
        val HEX_TOKENIZER = Scanners.HEX_INTEGER.map(TokenizerMaps.HEX_AS_LONG)

        /**
         * A tokenizer that parses decimal, hex, and octal numbers and converts the string to a
         * `Long` value.
         */
        val TOKENIZER = or(HEX_TOKENIZER, DEC_TOKENIZER, OCT_TOKENIZER)
    }

    /** Entry point for any arbitrary integer literal represented as a [String].  */
    object IntegerLiteral {
        /**
         * [Parser] that recognizes [Tokens.Fragment] tokens tagged as [Tag.INTEGER].
         */
        @JvmField
        val PARSER = fragment(Tokens.Tag.INTEGER)

        /**
         * A tokenizer that parses a integer number (valid patterns are: `0, 00, 1, 10`)
         * and returns a [Fragment] token tagged as [Tag.INTEGER].
         */
        @JvmField
        val TOKENIZER = Scanners.INTEGER.map(TokenizerMaps.INTEGER_FRAGMENT)
    }

    /** Entry point for parser and tokenizers of decimal number literal represented as [String]. */
    object DecimalLiteral {
        /**
         * [Parser] that recognizes [Tokens.Fragment] tokens tagged as [Tag.DECIMAL].
         */
        @JvmField
        val PARSER = fragment(Tokens.Tag.DECIMAL)

        /**
         * A tokenizer that parses a decimal number (valid patterns are: `1, 2.3, 00, 0., .23`)
         * and returns a [Fragment] token tagged as [Tag.DECIMAL].
         */
        @JvmField
        val TOKENIZER = Scanners.DECIMAL.map(TokenizerMaps.DECIMAL_FRAGMENT)
    }

    /** Entry point for parser and tokenizers of scientific notation literal.  */
    object ScientificNumberLiteral {
        /** [Parser] that recognies [ScientificNotation] tokens.  */
        val PARSER = tokenType(ScientificNotation::class.java, "scientific number literal")

        /**
         * A tokenizer that parses a scientific notation and converts the string to a
         * [ScientificNotation] value.
         */
        val TOKENIZER = Scanners.SCIENTIFIC_NOTATION.map(TokenizerMaps.SCIENTIFIC_NOTATION)
    }

    /** Entry point for parser and tokenizers of regular identifier.  */
    object Identifier {
        /**
         * [Parser] that recognizes identifier tokens.
         * i.e. [Tokens.Fragment] tokens tagged as [Tag.IDENTIFIER].
         * [Fragment.text] is returned as parser result.
         */
        @JvmField
        val PARSER = fragment(Tokens.Tag.IDENTIFIER)

        /**
         * A tokenizer that parses any identifier and returns a [Fragment] token tagged as
         * [Tag.IDENTIFIER].
         *
         *
         *  An identifier starts with an alphabetic character or underscore,
         * and is followed by 0 or more alphanumeric characters or underscore.
         */
        @JvmField
        val TOKENIZER = Scanners.IDENTIFIER.map(TokenizerMaps.IDENTIFIER_FRAGMENT)
    }

    /**
     * Starts to build a new `Terminals` instance that recognizes words not already recognized
     * by `this` `Terminals` instance (typically operators).
     *
     *
     * By default identifiers are recognized through [.identifier] during token-level
     * parsing phase. Use [Builder.tokenizeWordsWith] to tokenize differently, and choose an
     * alternative token-level parser accordingly.
     *
     * @param wordScanner defines words recognized by the new instance
     * @since 2.2
     */
    fun words(wordScanner: Parser<String>): Builder {
        return Builder(wordScanner)
    }

    /**
     * Builds [Terminals] instance by defining the words and keywords recognized.
     * The following example implements a calculator with logical operators: <pre>   `Terminals terms = Terminals
     * .operators("<", ">", "=", ">=", "<=")
     * .words(Scanners.IDENTIFIER)
     * .caseInsensitiveKeywords("and", "or")
     * .build();
     * Parser<String> var = Terminals.identifier();
     * Parser<Integer> integer = Terminals.IntegerLiteral.PARSER.map(...);
     * Parser<?> and = terms.token("and");
     * Parser<?> lessThan = terms.token("<");
     * ...
     * Parser<?> parser = grammar.from(
     * terms.tokenizer().or(IntegerLiteral.TOKENIZER), Scanners.WHITSPACES.optional());
    `</pre> *
     *
     * @since 2.2
     */
    inner class Builder internal constructor(wordScanner: Parser<String>) {
        private val wordScanner: Parser<String>
        private var keywords: Collection<String> = ArrayList()
        private var stringCase = StringCase.CASE_SENSITIVE
        private var wordTokenMap: Function<String, *> = TokenizerMaps.IDENTIFIER_FRAGMENT

        /**
         * Defines keywords. Keywords are special words with their own grammar rules.
         * To get the parser for a keyword, call `token(keyword)`.
         *
         *
         * Note that if you call [.keywords] or [.caseInsensitiveKeywords] multiple
         * times on the same [Builder] instance, the last call overwrites previous calls.
         */
        fun keywords(vararg keywords: String): Builder {
            return keywords(listOf(*keywords))
        }

        /**
         * Defines keywords. Keywords are special words with their own grammar rules.
         * To get the parser for a keyword, call `token(keyword)`.
         *
         *
         * Note that if you call [.keywords] or [.caseInsensitiveKeywords] multiple
         * times on the same [Builder] instance, the last call overwrites previous calls.
         */
        fun keywords(keywords: Collection<String>): Builder {
            this.keywords = keywords
            stringCase = StringCase.CASE_SENSITIVE
            return this
        }

        /**
         * Defines case insensitive keywords. Keywords are special words with their own grammar
         * rules. To get the parser for a keyword, call `token(keyword)`.
         *
         *
         * Note that if you call [.keywords] or [.caseInsensitiveKeywords] multiple
         * times on the same [Builder] instance, the last call overwrites previous calls.
         */
        fun caseInsensitiveKeywords(vararg keywords: String): Builder {
            return caseInsensitiveKeywords(listOf(*keywords))
        }

        /**
         * Defines case insensitive keywords. Keywords are special words with their own grammar
         * rules. To get the parser for a keyword, call `token(keyword)`.
         *
         *
         * Note that if you call [.keywords] or [.caseInsensitiveKeywords] multiple
         * times on the same [Builder] instance, the last call overwrites previous calls.
         */
        fun caseInsensitiveKeywords(keywords: Collection<String>): Builder {
            this.keywords = keywords
            stringCase = StringCase.CASE_INSENSITIVE
            return this
        }

        /** Configures alternative tokenization strategy for words (except keywords).  */
        fun tokenizeWordsWith(wordMap: Function<String, *>): Builder {
            wordTokenMap = checkNotNull(wordMap)
            return this
        }

        /** Builds a new [Terminals] instance that recognizes words defined in this builder.  */
        fun build(): Terminals {
            return Terminals(
                union(lexicon(wordScanner, keywords, stringCase, wordTokenMap)))
        }

        init {
            this.wordScanner = checkNotNull(wordScanner)
        }
    }

    companion object {
        /**
         * [Parser] that recognizes reserved word tokens.
         * i.e. [Tokens.Fragment] tokens tagged as [Tag.RESERVED].
         * [Fragment.text] is returned as parser result.
         */
        val RESERVED = fragment(Tokens.Tag.RESERVED)

        /**
         * Returns a [Terminals] object for lexing and parsing the operators with names specified in
         * `ops`, and for lexing and parsing the keywords case insensitively. Parsers for operators
         * and keywords can be obtained through [.token]; parsers for identifiers through
         * [.identifier].
         *
         *
         * In detail, keywords and operators are lexed as [Tokens.Fragment] with
         * [Tag.RESERVED] tag. Words that are not among `keywords` are lexed as
         * `Fragment` with [Tag.IDENTIFIER] tag.
         *
         *
         * A word is defined as an alphanumeric  string that starts with `[_a - zA - Z]`,
         * with 0 or more `[0 - 9_a - zA - Z]` following.
         *
         * @param ops the operator names.
         * @param keywords the keyword names.
         * @return the Terminals instance.
         */
        @Deprecated("""Use {@code operators(ops)
   *                 .words(Scanners.IDENTIFIER)
   *                 .caseInsensitiveKeywords(keywords)
   *                 .build()} instead.""")
        fun caseInsensitive(ops: Array<String>, keywords: Array<String>): Terminals {
            return operators(*ops).words(Scanners.IDENTIFIER).caseInsensitiveKeywords(Arrays.asList(*keywords)).build()
        }

        /**
         * Returns a [Terminals] object for lexing and parsing the operators with names specified in
         * `ops`, and for lexing and parsing the keywords case sensitively. Parsers for operators
         * and keywords can be obtained through [.token]; parsers for identifiers through
         * [.identifier].
         *
         *
         * In detail, keywords and operators are lexed as [Tokens.Fragment] with
         * [Tag.RESERVED] tag. Words that are not among `keywords` are lexed as
         * `Fragment` with [Tag.IDENTIFIER] tag.
         *
         *
         * A word is defined as an alphanumeric string that starts with `[_a - zA - Z]`,
         * with 0 or more `[0 - 9_a - zA - Z]` following.
         *
         * @param ops the operator names.
         * @param keywords the keyword names.
         * @return the Terminals instance.
         */
        @Deprecated("""Use {@code operators(ops)
   *                 .words(Scanners.IDENTIFIER)
   *                 .keywords(keywords)
   *                 .build()} instead.""")
        fun caseSensitive(ops: Array<String>, keywords: Array<String>): Terminals {
            return operators(*ops).words(Scanners.IDENTIFIER).keywords(Arrays.asList(*keywords)).build()
        }

        /**
         * Returns a [Terminals] object for lexing and parsing the operators with names specified in
         * `ops`, and for lexing and parsing the keywords case insensitively. Parsers for operators
         * and keywords can be obtained through [.token]; parsers for identifiers through
         * [.identifier].
         *
         *
         * In detail, keywords and operators are lexed as [Tokens.Fragment] with
         * [Tag.RESERVED] tag. Words that are not among `keywords` are lexed as
         * `Fragment` with [Tag.IDENTIFIER] tag.
         *
         * @param wordScanner the scanner that returns a word in the language.
         * @param ops the operator names.
         * @param keywords the keyword names.
         * @return the Terminals instance.
         */
        @Deprecated("""Use {@code operators(ops)
   *                 .words(wordScanner)
   *                 .caseInsensitiveKeywords(keywords)
   *                 .build()} instead.""")
        fun caseInsensitive(
            wordScanner: Parser<String>, ops: Array<String>, keywords: Array<String>): Terminals {
            return operators(*ops)
                .words(wordScanner)
                .caseInsensitiveKeywords(*keywords)
                .build()
        }

        /**
         * Returns a [Terminals] object for lexing and parsing the operators with names specified in
         * `ops`, and for lexing and parsing the keywords case sensitively. Parsers for operators
         * and keywords can be obtained through [.token]; parsers for identifiers through
         * [.identifier].
         *
         *
         * In detail, keywords and operators are lexed as [Tokens.Fragment] with
         * [Tag.RESERVED] tag. Words that are not among `keywords` are lexed as
         * `Fragment` with [Tag.IDENTIFIER] tag.
         *
         * @param wordScanner the scanner that returns a word in the language.
         * @param ops the operator names.
         * @param keywords the keyword names.
         * @return the Terminals instance.
         */
        @Deprecated("""Use {@code operators(ops)
   *                 .words(wordScanner)
   *                 .keywords(keywords)
   *                 .build()} instead.""")
        fun caseSensitive(
            wordScanner: Parser<String>, ops: Array<String>, keywords: Array<String>): Terminals {
            return operators(*ops)
                .words(wordScanner)
                .keywords(*keywords)
                .build()
        }

        /**
         * Returns a [Terminals] object for lexing and parsing the operators with names specified in
         * `ops`, and for lexing and parsing the keywords case insensitively. Parsers for operators
         * and keywords can be obtained through [.token]; parsers for identifiers through
         * [.identifier].
         *
         *
         * In detail, keywords and operators are lexed as [Tokens.Fragment] with
         * [Tag.RESERVED] tag. Words that are not among `keywords` are lexed as
         * `Fragment` with [Tag.IDENTIFIER] tag.
         *
         * @param wordScanner the scanner that returns a word in the language.
         * @param ops the operator names.
         * @param keywords the keyword names.
         * @param wordMap maps the text to a token value for non-keywords recognized by
         * `wordScanner`.
         * @return the Terminals instance.
         */
        @Deprecated("""Use {@code operators(ops)
   *                 .words(wordScanner)
   *                 .tokenizeWordsWith(wordMap)
   *                 .caseInsensitiveKeywords(keywords)
   *                 .build()} instead.""")
        fun caseInsensitive(
            wordScanner: Parser<String>, ops: Array<String>, keywords: Array<String>, wordMap: Function<String, *>): Terminals {
            return operators(*ops)
                .words(wordScanner)
                .caseInsensitiveKeywords(*keywords)
                .tokenizeWordsWith(wordMap)
                .build()
        }

        /**
         * Returns a [Terminals] object for lexing and parsing the operators with names specified in
         * `ops`, and for lexing and parsing the keywords case sensitively. Parsers for operators
         * and keywords can be obtained through [.token]; parsers for identifiers through
         * [.identifier].
         *
         *
         * In detail, keywords and operators are lexed as [Tokens.Fragment] with
         * [Tag.RESERVED] tag. Words that are not among `keywords` are lexed as
         * `Fragment` with [Tag.IDENTIFIER] tag.
         *
         * @param wordScanner the scanner that returns a word in the language.
         * @param ops the operator names.
         * @param keywords the keyword names.
         * @param wordMap maps the text to a token value for non-keywords recognized by
         * `wordScanner`.
         * @return the Terminals instance.
         */
        @Deprecated("""Use {@code operators(ops)
   *                 .words(wordScanner)
   *                 .tokenizeWordsWith(wordMap)
   *                 .keywords(keywords)
   *                 .build()} instead.""")
        fun caseSensitive(
            wordScanner: Parser<String>, ops: Array<String>, keywords: Array<String>, wordMap: Function<String, *>): Terminals {
            return operators(*ops)
                .words(wordScanner)
                .keywords(*keywords)
                .tokenizeWordsWith(wordMap)
                .build()
        }

        /**
         * Returns a [Terminals] object for lexing the operators with names specified in
         * `ops`. Operators are lexed as [Tokens.Fragment] with [Tag.RESERVED] tag.
         * For example, to get the parser for operator "?", simply call `token("?")`.
         *
         *
         * If words and keywords need to be parsed, they can be configured via [.words].
         *
         * @param ops the operator names.
         * @return the Terminals instance.
         */
        fun operators(vararg ops: String): Terminals {
            return operators(Arrays.asList(*ops))
        }

        /**
         * Returns a [Terminals] object for lexing the operators with names specified in
         * `ops`. Operators are lexed as [Tokens.Fragment] with [Tag.RESERVED] tag.
         * For example, to get the parser for operator "?", simply call `token("?")`.
         *
         *
         * If words and keywords need to be parsed, they can be configured via [.words].
         *
         * @param ops the operator names.
         * @return the Terminals instance.
         * @since 2.2
         */
        fun operators(ops: Collection<String>): Terminals {
            return Terminals(lexicon(ops))
        }

        /**
         * Returns a [Parser] that recognizes identifiers (a.k.a words, variable names etc).
         * Equivalent to [Identifier.PARSER].
         *
         * @since 2.2
         */
        fun identifier(): Parser<String?> {
            return Identifier.PARSER
        }

        /**
         * Returns a [Parser] that recognizes [Tokens.Fragment] token values
         * tagged with one of `tags`.
         */
        @JvmStatic fun fragment(vararg tags: Any): Parser<String?> {
            return token(fromFragment(*tags))
        }

        /**
         * Returns a [TokenMap] object that only recognizes [Tokens.Fragment] token values
         * tagged with one of `tags`.
         */
        fun fromFragment(vararg tags: Any): TokenMap<String?> {
            return object: TokenMap<String?> {
                override fun map(token: Token): String? {
                    val value = token.value()
                    return if (value is Fragment) {
                        if (`in`(value.tag(), *tags)) value.text() else null
                    } else null
                }

                override fun toString(): String {
                    if (tags.isEmpty()) return ""
                    return if (tags.size == 1) tags[0].toString() else "[" + tags.joinToString(", ") + "]"
                }
            }
        }

        @Private fun checkDup(a: Iterable<String>, b: Iterable<String>) {
            for (s1 in a) {
                for (s2 in b) {
                    checkArgument(s1 != s2, "%s duplicated", s1)
                }
            }
        }
    }
}