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

import org.jparsec.Parsers.or
import org.jparsec.Parsers.sequence
import org.jparsec.pattern.CharPredicate
import org.jparsec.pattern.CharPredicates
import org.jparsec.pattern.Pattern
import org.jparsec.pattern.Patterns
import org.jparsec.pattern.Patterns.notString

/**
 * Provides common [Parser] implementations that scan the source and match certain string
 * patterns.
 *
 *
 * Some scanners like [.IDENTIFIER] and [.INTEGER] return the matched string,
 * while others like [.WHITESPACES] return nothing, as indicated by the [Void]
 * type parameter. In case the matched string is still needed nonetheless,
 * use the [Parser.source] method.
 *
 * @author Ben Yu
 */
object Scanners {
    /** A scanner that scans greedily for 1 or more whitespace characters.  */
    @JvmField
    val WHITESPACES = Patterns.many1(CharPredicates.IS_WHITESPACE).toScanner("whitespaces")

    /**
     * Matches any character in the input. Different from [Parsers.always],
     * it fails on EOF. Also it consumes the current character in the input.
     */
    val ANY_CHAR: Parser<Unit> = object: Parser<Unit>() {
        public override fun apply(ctxt: ParseContext): Boolean {
            if (ctxt.isEof) {
                ctxt.missing("any character")
                return false
            }
            ctxt.next()
            ctxt.result = null
            return true
        }

        override fun toString(): String {
            return "any character"
        }
    }

    /** Scanner for c++/java style line comment.  */
    @JvmField
    val JAVA_LINE_COMMENT = lineComment("//")

    /** Scanner for SQL style line comment.  */
    val SQL_LINE_COMMENT = lineComment("--")

    /** Scanner for haskell style line comment. (`--`)  */
    val HASKELL_LINE_COMMENT = lineComment("--")
    private val JAVA_BLOCK_COMMENTED = notChar2('*', '/').many().toScanner("commented block")

    /** Scanner for c++/java style block comment.  */
    @JvmField
    val JAVA_BLOCK_COMMENT = sequence(string("/*"), JAVA_BLOCK_COMMENTED, string("*/"))

    /** Scanner for SQL style block comment.  */
    val SQL_BLOCK_COMMENT = sequence(string("/*"), JAVA_BLOCK_COMMENTED, string("*/"))

    /** Scanner for haskell style block comment. {- -}  */
    val HASKELL_BLOCK_COMMENT = sequence(
        string("{-"), notChar2('-', '}').many().toScanner("commented block"), string("-}"))

    /**
     * Scanner with a pattern for SQL style string literal. A SQL string literal
     * is a string quoted by single quote, a single quote character is escaped by
     * 2 single quotes.
     */
    @JvmField
    val SINGLE_QUOTE_STRING = quotedBy(
        notString("'").or(Patterns.string("''")).many().toScanner("quoted string"),
        isChar('\'')).source()

    /**
     * Scanner with a pattern for double quoted string literal. Backslash '\' is
     * used as escape character.
     */
    @JvmField
    val DOUBLE_QUOTE_STRING = quotedBy(
        escapedChar('\\').or(Patterns.isChar(CharPredicates.notChar('"'))).many().toScanner("quoted string"),
        isChar('"')).source()

    /** Scanner for a c/c++/java style character literal. such as 'a' or '\\'.  */
    @JvmField
    val SINGLE_QUOTE_CHAR = quotedBy(
        escapedChar('\\').or(Patterns.isChar(CharPredicates.notChar('\''))).toScanner("quoted char"),
        isChar('\'')).source()

    /**
     * Scanner for the c++/java style delimiter of tokens. For example,
     * whitespaces, line comment and block comment.
     */
    @JvmField
    val JAVA_DELIMITER = or(WHITESPACES, JAVA_LINE_COMMENT, JAVA_BLOCK_COMMENT).skipMany()

    /**
     * Scanner for the haskell style delimiter of tokens. For example,
     * whitespaces, line comment and block comment.
     */
    val HASKELL_DELIMITER = or(WHITESPACES, HASKELL_LINE_COMMENT, HASKELL_BLOCK_COMMENT).skipMany()

    /**
     * Scanner for the SQL style delimiter of tokens. For example, whitespaces and
     * line comment.
     */
    @JvmField
    val SQL_DELIMITER = or(WHITESPACES, SQL_LINE_COMMENT, SQL_BLOCK_COMMENT).skipMany()

    /**
     * Scanner for a regular identifier, that starts with either
     * an underscore or an alpha character, followed by 0 or more alphanumeric characters.
     */
    @JvmField
    val IDENTIFIER = Patterns.WORD.toScanner("word").source()

    /** Scanner for an integer.  */
    @JvmField
    val INTEGER = Patterns.INTEGER.toScanner("integer").source()

    /** Scanner for a decimal number.  */
    @JvmField
    val DECIMAL = Patterns.DECIMAL.toScanner("decimal").source()

    /** Scanner for a decimal number. 0 is not allowed as the leading digit.  */
    @JvmField
    val DEC_INTEGER = Patterns.DEC_INTEGER.toScanner("decimal integer").source()

    /** Scanner for a octal number. 0 is the leading digit.  */
    @JvmField
    val OCT_INTEGER = Patterns.OCT_INTEGER.toScanner("octal integer").source()

    /** Scanner for a hexadecimal number. Has to start with `0x` or `0X`.  */
    @JvmField
    val HEX_INTEGER = Patterns.HEX_INTEGER.toScanner("hexadecimal integer").source()

    /** Scanner for a scientific notation.  */
    @JvmField
    val SCIENTIFIC_NOTATION = Patterns.SCIENTIFIC_NOTATION.toScanner("scientific notation").source()

    /**
     * A scanner that scans greedily for 0 or more characters that satisfies the given CharPredicate.
     *
     * @param predicate the predicate object.
     * @return the Parser object.
     */
    fun many(predicate: CharPredicate): Parser<Unit> {
        return Patterns.isChar(predicate).many().toScanner("$predicate*")
    }

    /**
     * A scanner that scans greedily for 1 or more characters that satisfies the given CharPredicate.
     *
     * @param predicate the predicate object.
     * @return the Parser object.
     */
    fun many1(predicate: CharPredicate): Parser<Unit> {
        return Patterns.many1(predicate).toScanner("$predicate+")
    }

    /**
     * A scanner that scans greedily for 0 or more occurrences of the given pattern.
     *
     * @param pattern the pattern object.
     * @param name the name of what's expected logically. Is used in error message.
     * @return the Parser object.
     */
    @Deprecated("Use {@code pattern.many().toScanner(name)}.")
    fun many(pattern: Pattern, name: String): Parser<Unit> {
        return pattern.many().toScanner(name)
    }

    /**
     * A scanner that scans greedily for 1 or more occurrences of the given pattern.
     *
     * @param pattern the pattern object.
     * @param name the name of what's expected logically. Is used in error message.
     * @return the Parser object.
     */
    @Deprecated("Use {@code pattern.many1().toScanner(name)}.")
    fun many1(pattern: Pattern, name: String): Parser<Unit> {
        return pattern.many1().toScanner(name)
    }

    /**
     * Matches the input against the specified string.
     *
     * @param str the string to match
     * @return the scanner.
     */
    fun string(str: String): Parser<Unit> {
        return Patterns.string(str).toScanner(str)
    }

    /**
     * Matches the input against the specified string.
     *
     * @param str the string to match
     * @param name the name of what's expected logically. Is used in error message.
     * @return the scanner.
     */
    @Deprecated("Use {@code Patterns.string(str).toScanner(name)}.")
    fun string(str: String?, name: String): Parser<Unit> {
        return Patterns.string(str!!).toScanner(name)
    }

    /**
     * A scanner that scans the input for an occurrence of a string pattern.
     *
     * @param pattern the pattern object.
     * @param name the name of what's expected logically. Is used in error message.
     * @return the Parser object.
     */
    @Deprecated("Use {@code pattern.toScanner(name)}.")
    fun pattern(pattern: Pattern, name: String): Parser<Unit> {
        return object: Parser<Unit>() {
            public override fun apply(ctxt: ParseContext): Boolean {
                val at = ctxt.at
                val src = ctxt.characters()
                val matchLength = pattern.match(src, at, src.length)
                if (matchLength < 0) {
                    ctxt.missing(name)
                    return false
                }
                ctxt.next(matchLength)
                ctxt.result = null
                return true
            }

            override fun toString(): String {
                return name
            }
        }
    }

    /**
     * A scanner that matches the input against the specified string case insensitively.
     *
     * @param str the string to match
     * @param name the name of what's expected logically. Is used in error message.
     * @return the scanner.
     */
    @Deprecated("Use {@code Patterns.stringCaseInsensitive(str).toScanner(name)}.")
    fun stringCaseInsensitive(str: String?, name: String): Parser<Unit> {
        return Patterns.stringCaseInsensitive(str!!).toScanner(name)
    }

    /**
     * A scanner that matches the input against the specified string case insensitively.
     * @param str the string to match
     * @return the scanner.
     */
    fun stringCaseInsensitive(str: String?): Parser<Unit> {
        return Patterns.stringCaseInsensitive(str!!).toScanner(str)
    }

    /**
     * A scanner that succeeds and consumes the current character if it satisfies the given
     * [CharPredicate].
     *
     * @param predicate the predicate.
     * @return the scanner.
     */
    fun isChar(predicate: CharPredicate): Parser<Unit> {
        return object: Parser<Unit>() {
            val name = predicate.toString()
            public override fun apply(ctxt: ParseContext): Boolean {
                if (ctxt.isEof) {
                    ctxt.missing(name)
                    return false
                }
                val c = ctxt.peekChar()
                if (predicate.isChar(c)) {
                    ctxt.next()
                    ctxt.result = null
                    return true
                }
                ctxt.missing(name)
                return false
            }

            override fun toString(): String {
                return name
            }
        }
    }

    /**
     * A scanner that succeeds and consumes the current character if it satisfies the given
     * [CharPredicate].
     *
     * @param predicate the predicate.
     * @param name the name of what's expected logically. Is used in error message.
     * @return the scanner.
     */
    @Deprecated("""Implement {@link Object#toString} in the {@code CharPredicate},
                or use {@code Patterns.isChar(predicate).toScanner(name)}.""")
    fun isChar(predicate: CharPredicate, name: String): Parser<Unit> {
        return Patterns.isChar(predicate!!).toScanner(name)
    }

    /**
     * A scanner that succeeds and consumes the current character if it is equal to `ch`.
     *
     * @param ch the expected character.
     * @param name the name of what's expected logically. Is used in error message.
     * @return the scanner.
     */
    @Deprecated("""Use {@link #isChar(char)} instead
                or use {@code Patterns.isChar(ch).toScanner(name)}.""")
    fun isChar(ch: Char, name: String): Parser<Unit> {
        return isChar(CharPredicates.isChar(ch), name)
    }

    /**
     * A scanner that succeeds and consumes the current character if it is equal to `ch`.
     *
     * @param ch the expected character.
     * @return the scanner.
     */
    @JvmStatic fun isChar(ch: Char): Parser<Unit> {
        return isChar(CharPredicates.isChar(ch))
    }

    /**
     * A scanner that succeeds and consumes the current character if it is equal to `ch`.
     *
     * @param ch the expected character.
     * @param name the name of what's expected logically. Is used in error message.
     * @return the scanner.
     */
    @Deprecated("Use {@link #notChar(char)}.")
    fun notChar(ch: Char, name: String): Parser<Unit> {
        return isChar(CharPredicates.notChar(ch), name)
    }

    /**
     * A scanner that succeeds and consumes the current character if it is not equal to `ch`.
     *
     * @param ch the expected character.
     * @return the scanner.
     */
    fun notChar(ch: Char): Parser<Unit> {
        return isChar(CharPredicates.notChar(ch))
    }

    /**
     * A scanner that succeeds and consumes the current character if it equals to any character in
     * `chars`.
     *
     * @param chars the characters.
     * @param name the name of what's expected logically. Is used in error message.
     * @return the scanner.
     */
    @Deprecated("Use {@code Patterns.among(chars).toScanner(name)}.")
    fun among(chars: String, name: String): Parser<Unit> {
        return isChar(CharPredicates.among(chars!!), name)
    }

    /**
     * A scanner that succeeds and consumes the current character if it equals to any character in
     * `chars`.
     */
    @JvmStatic fun among(chars: String): Parser<Unit> {
        if (chars.length == 0) return isChar(CharPredicates.NEVER)
        return if (chars.length == 1) isChar(chars[0]) else isChar(CharPredicates.among(chars))
    }

    /**
     * A scanner that succeeds and consumes the current character if it is not equal to any character
     * in `chars`.
     *
     * @param chars the characters.
     * @param name the name of what's expected logically. Is used in error message.
     * @return the scanner.
     */
    @Deprecated("""Use {@code Patterns.among(chars).not().toScanner(name)},
                or {@code isChar(CharPredicates.notAmong(chars), name)}.""")
    fun notAmong(chars: String, name: String): Parser<Unit> {
        return isChar(CharPredicates.notAmong(chars!!), name)
    }

    /**
     * A scanner that succeeds and consumes the current character if it is not equal to any character
     * in `chars`.
     */
    fun notAmong(chars: String): Parser<Unit> {
        if (chars.length == 0) return ANY_CHAR
        return if (chars.length == 1) notChar(chars[0]) else isChar(CharPredicates.notAmong(chars))
    }

    /**
     * A scanner that succeeds and consumes all the characters until the `'\n'` character
     * if the current input starts with the string literal `begin`. The `'\n'` character
     * isn't consumed.
     */
    @JvmStatic fun lineComment(begin: String?): Parser<Unit> {
        return Patterns.lineComment(begin!!).toScanner(begin)
    }

    /**
     * A scanner for non-nested block comment that starts with `begin` and ends with
     * `end`.
     */
    fun blockComment(begin: String?, end: String?): Parser<Unit> {
        val opening = Patterns.string(begin!!).next(notString(end!!).many())
        return opening.toScanner(begin).next(string(end))
    }

    /**
     * A scanner for a non-nestable block comment that starts with `begin` and ends with
     * `end`.
     *
     * @param begin begins a block comment
     * @param end ends a block comment
     * @param commented the commented pattern.
     * @return the Scanner for the block comment.
     */
    fun blockComment(begin: String?, end: String?, commented: Pattern?): Parser<Unit> {
        val opening = Patterns.string(begin!!)
            .next(Patterns.string(end!!).not().next(commented).many())
        return opening.toScanner(begin).next(string(end))
    }

    /**
     * A scanner for a non-nestable block comment that starts with `begin` and ends with
     * `end`.
     *
     * @param begin begins a block comment
     * @param end ends a block comment
     * @param commented the commented pattern.
     * @return the Scanner for the block comment.
     */
    fun blockComment(begin: Parser<Unit>, end: Parser<Unit>, commented: Parser<*>): Parser<Unit> {
        return sequence(begin!!, end.not().next(commented).skipMany(), end)
    }
    /**
     * A scanner for a nestable block comment that starts with `begin` and ends with
     * `end`.
     *
     * @param begin begins a block comment
     * @param end ends a block comment
     * @param commented the commented pattern except for nested comments.
     * @return the block comment scanner.
     */
    /**
     * A scanner for a nestable block comment that starts with `begin` and ends with
     * `end`.
     *
     * @param begin begins a block comment
     * @param end ends a block comment
     * @return the block comment scanner.
     */
    @JvmOverloads
    fun nestableBlockComment(begin: String, end: String, commented: Pattern = Patterns.isChar(CharPredicates.ALWAYS)): Parser<Unit> {
        return nestableBlockComment(
            string(begin), string(end), commented.toScanner("commented"))
    }

    /**
     * A scanner for a nestable block comment that starts with `begin` and ends with
     * `end`.
     *
     * @param begin starts a block comment
     * @param end ends a block comment
     * @param commented the commented pattern except for nested comments.
     * @return the block comment scanner.
     */
    fun nestableBlockComment(
        begin: Parser<*>, end: Parser<*>, commented: Parser<*>): Parser<Unit> {
        return NestableBlockCommentScanner(begin!!, end!!, commented!!)
    }

    /**
     * A scanner for a quoted string that starts with character `begin` and ends with character
     * `end`.
     */
    fun quoted(begin: Char, end: Char): Parser<String> {
        val beforeClosingQuote = Patterns.isChar(begin).next(Patterns.many(CharPredicates.notChar(end)))
        return beforeClosingQuote.toScanner(Character.toString(begin)).next(isChar(end)).source()
    }

    /**
     * A scanner for a quoted string that starts with `begin` and ends with `end`.
     *
     * @param begin begins a quote
     * @param end ends a quote
     * @param quoted the parser that recognizes the quoted pattern.
     * @return the scanner.
     */
    @Deprecated("Use {@code Parsers.sequence(begin, quoted.skipMany(), end).source()}.")
    fun quoted(begin: Parser<Unit>, end: Parser<Unit>, quoted: Parser<*>): Parser<String> {
        return sequence(begin!!, quoted.skipMany(), end).source()
    }

    /**
     * A scanner that after character level `outer` succeeds,
     * subsequently feeds the recognized characters to `inner` for a nested scanning.
     *
     *
     *  Is useful for scenarios like parsing string interpolation grammar, with parsing errors
     * correctly pointing to the right location in the original source.
     */
    fun nestedScanner(outer: Parser<*>, inner: Parser<Unit>): Parser<Unit> {
        return object: Parser<Unit>() {
            public override fun apply(ctxt: ParseContext): Boolean {
                val from = ctxt.at
                if (!outer.apply(ctxt)) return false
                val innerState = ScannerState(
                    ctxt.module, ctxt.characters(), from, ctxt.at, ctxt.locator, ctxt.result)
                ctxt.trace.startFresh(innerState)
                innerState.trace.setStateAs(ctxt.trace)
                return ctxt.applyNested(inner, innerState)
            }

            override fun toString(): String {
                return "nested scanner"
            }
        }
    }

    /**
     * Matches a character if the input has at least 1 character, or if the input has at least 2
     * characters with the first 2 characters not being `c1` and `c2`.
     *
     * @return the Pattern object.
     */
    private fun notChar2(c1: Char, c2: Char): Pattern {
        return object: Pattern() {
            override fun match(src: CharSequence, begin: Int, end: Int): Int {
                if (begin == end - 1) return 1
                if (begin >= end) return MISMATCH
                return if (src[begin] == c1 && src[begin + 1] == c2) MISMATCH else 1
            }
        }
    }

    private fun quotedBy(parser: Parser<Unit>, quote: Parser<*>): Parser<Unit> {
        return parser.between(quote, quote)
    }

    private fun escapedChar(escape: Char): Pattern {
        return Patterns.isChar(escape).next(Patterns.ANY_CHAR)
    }
}