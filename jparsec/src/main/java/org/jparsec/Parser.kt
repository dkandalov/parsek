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

import org.jparsec.EmptyListParser.Companion.instance
import org.jparsec.InternalFunctors.firstOfTwo
import org.jparsec.ListFactory.Companion.arrayListFactoryWithFirstElement
import org.jparsec.Parser.Mode
import org.jparsec.Parsers.always
import org.jparsec.Parsers.constant
import org.jparsec.Parsers.nested
import org.jparsec.Parsers.or
import org.jparsec.Parsers.sequence
import org.jparsec.Parsers.sequence2
import org.jparsec.Parsers.tokens
import org.jparsec.Parsers.unexpected
import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.Checks.checkMin
import org.jparsec.internal.util.Checks.checkMinMax
import java.io.IOException
import java.nio.CharBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Defines grammar and encapsulates parsing logic. A [Parser] takes as input a
 * [CharSequence] source and parses it when the [.parse] method is called.
 * A value of type `T` will be returned if parsing succeeds, or a [ParserException]
 * is thrown to indicate parsing error. For example: <pre>   `Parser<String> scanner = Scanners.IDENTIFIER;
 * assertEquals("foo", scanner.parse("foo"));
`</pre> *
 *
 *
 *  `Parser`s run either on character level to scan the source, or on token level to parse
 * a list of [Token] objects returned from another parser. This other parser that returns the
 * list of tokens for token level parsing is hooked up via the [.from]
 * or [.from] method.
 *
 *
 * The following are important naming conventions used throughout the library:
 *
 *
 *  * A character level parser object that recognizes a single lexical word is called a scanner.
 *  * A scanner that translates the recognized lexical word into a token is called a tokenizer.
 *  * A character level parser object that does lexical analysis and returns a list of
 * [Token] is called a lexer.
 *  * All `index` parameters are 0-based indexes in the original source.
 *
 *
 * To debug a complex parser that fails in un-obvious way, pass [Mode.DEBUG] mode to
 * [.parse] and inspect the result in
 * [ParserException.getParseTree]. All [labeled][.label] parsers will generate a node
 * in the exception's parse tree, with matched indices in the source.
 *
 * @author Ben Yu
 */
abstract class Parser<T> internal constructor() {
    /**
     * An atomic mutable reference to [Parser] used in recursive grammars.
     *
     *
     * For example, the following is a recursive grammar for a simple calculator: <pre>   `Terminals terms = Terminals.operators("(", ")", "+", "-");
     * Parser.Reference<Integer> ref = Parser.newReference();
     * Parser<Integer> literal = Terminals.IntegerLiteral.PARSER.map(new Function<String, Integer>() {
     * ...
     * return Integer.parseInt(s);
     * });
     * Parser.Reference<Integer> parenthesized =  // recursion in rule E = (E)
     * Parsers.between(terms.token("("), ref.lazy(), terms.token(")"));
     * ref.set(new OperatorTable()
     * .infixl(terms.token("+").retn(plus), 10)
     * .infixl(terms.token("-").retn(minus), 10)
     * .build(literal.or(parenthesized)));
     * return ref.get();
    `</pre> *
     * Note that a left recursive grammar will result in `StackOverflowError`.
     * Use appropriate parser built-in parser combinators to avoid left-recursion.
     * For instance, many left recursive grammar rules can be thought as logically equivalent to
     * postfix operator rules. In such case, either [OperatorTable] or [Parser.postfix]
     * can be used to work around left recursion.
     * The following is a left recursive parser for array types in the form of "T[]" or "T[][]":
     * <pre>   `Terminals terms = Terminals.operators("[", "]");
     * Parser.Reference<Type> ref = Parser.newReference();
     * ref.set(Parsers.or(leafTypeParser,
     * Parsers.sequence(ref.lazy(), terms.phrase("[", "]"), new Unary<Type>() {...})));
     * return ref.get();
    `</pre> *
     * And it will fail. A correct implementation is:  <pre>   `Terminals terms = Terminals.operators("[", "]");
     * return leafTypeParer.postfix(terms.phrase("[", "]").retn(new Unary<Type>() {...}));
    `</pre> *
     * A not-so-obvious example, is to parse the `expr ? a : b` ternary operator. It too is a
     * left recursive grammar. And un-intuitively it can also be thought as a postfix operator.
     * Basically, we can parse "? a : b" as a whole into a unary operator that accepts the condition
     * expression as input and outputs the full ternary expression: <pre>   `Parser<Expr> ternary(Parser<Expr> expr) {
     * return expr.postfix(
     * Parsers.sequence(
     * terms.token("?"), expr, terms.token(":"), expr,
     * (unused, then, unused, orelse) -> cond ->
     * new TernaryExpr(cond, then, orelse)));
     * }
    `</pre> *
     */
    class Reference<T>: AtomicReference<Parser<T>>() {
        private val lazy: Parser<T> = object: Parser<T>() {

            override fun apply(ctxt: ParseContext): Boolean = deref().apply(ctxt)

            private fun deref(): Parser<T> {
                val p: Parser<T>? = get()
                return p ?: error("Uninitialized lazy parser reference. Did you forget to call set() on the reference?")
            }

            override fun toString() = "lazy"
        }

        /**
         * A [Parser] that delegates to the parser object referenced by `this` during parsing time.
         */
        fun lazy(): Parser<T> = lazy
    }

    /**
     * A [Parser] that executes `this`, and returns `value` if succeeds.
     */
    fun <R> retn(value: R): Parser<R> {
        return next(constant(value))
    }

    /**
     * A [Parser] that sequentially executes `this` and then `parser`. The return value of `parser` is preserved.
     */
    fun <R> next(parser: Parser<R>): Parser<R> {
        return sequence(this, parser)
    }

    /**
     * A [Parser] that executes `this`, maps the result using `map` to another `Parser` object
     * to be executed as the next step.
     */
    fun <To> next(
        map: Function<in T, out Parser<out To>?>): Parser<To> {
        return object: Parser<To>() {
            override fun apply(ctxt: ParseContext): Boolean {
                return this@Parser.apply(ctxt) && runNext(ctxt)
            }

            override fun toString(): String {
                return map.toString()
            }

            private fun runNext(state: ParseContext): Boolean {
                val from: T = this@Parser.getReturn(state)
                return map.apply(from)!!.apply(state)
            }
        }
    }

    /**
     * A [Parser] that matches this parser zero or many times
     * until the given parser succeeds. The input that matches the given parser
     * will not be consumed. The input that matches this parser will
     * be collected in a list that will be returned by this function.
     *
     * @since 2.2
     */
    fun until(parser: Parser<*>): Parser<List<T>> {
        return parser.not().next(this).many().followedBy(parser.peek())
    }

    /**
     * A [Parser] that sequentially executes `this` and then `parser`, whose return value is ignored.
     */
    fun followedBy(parser: Parser<*>): Parser<T> {
        return sequence2(this, parser as Parser<T>, firstOfTwo<T, T>())
    }

    /**
     * A [Parser] that succeeds if `this` succeeds and the pattern recognized by `parser` isn't
     * following.
     */
    fun notFollowedBy(parser: Parser<*>): Parser<T> {
        return followedBy(parser.not())
    }

    /**
     * `p.many()` is equivalent to `p*` in EBNF. The return values are collected and returned in a [ ].
     */
    fun many(): Parser<List<T>> {
        return atLeast(0)
    }

    /**
     * `p.skipMany()` is equivalent to `p*` in EBNF. The return values are discarded.
     */
    fun skipMany(): Parser<Unit> {
        return skipAtLeast(0)
    }

    /**
     * `p.many1()` is equivalent to `p+` in EBNF. The return values are collected and returned in a [ ].
     */
    fun many1(): Parser<List<T>> {
        return atLeast(1)
    }

    /**
     * `p.skipMany1()` is equivalent to `p+` in EBNF. The return values are discarded.
     */
    fun skipMany1(): Parser<Unit> {
        return skipAtLeast(1)
    }

    /**
     * A [Parser] that runs `this` parser greedily for at least `min` times. The return values are
     * collected and returned in a [List].
     */
    fun atLeast(min: Int): Parser<List<T>> {
        return RepeatAtLeastParser(this, checkMin(min))
    }

    /**
     * A [Parser] that runs `this` parser greedily for at least `min` times and ignores the return
     * values.
     */
    fun skipAtLeast(min: Int): Parser<Unit> {
        return SkipAtLeastParser(this, checkMin(min)) as Parser<Unit>
    }

    /**
     * A [Parser] that sequentially runs `this` for `n` times and ignores the return values.
     */
    fun skipTimes(n: Int): Parser<Unit> {
        return skipTimes(n, n)
    }

    /**
     * A [Parser] that runs `this` for `n` times and collects the return values in a [List].
     */
    operator fun times(n: Int): Parser<List<T>> {
        return times(n, n)
    }

    /**
     * A [Parser] that runs `this` parser for at least `min` times and up to `max` times. The
     * return values are collected and returned in [List].
     */
    fun times(min: Int, max: Int): Parser<List<T>> {
        checkMinMax(min, max)
        return RepeatTimesParser(this, min, max)
    }

    /**
     * A [Parser] that runs `this` parser for at least `min` times and up to `max` times, with
     * all the return values ignored.
     */
    fun skipTimes(min: Int, max: Int): Parser<Unit> {
        checkMinMax(min, max)
        return SkipTimesParser(this, min, max)
    }

    /**
     * A [Parser] that runs `this` parser and transforms the return value using `map`.
     */
    fun <R> map(map: Function<in T, out R>): Parser<R> {
        return object: Parser<R>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r = this@Parser.apply(ctxt)
                if (r) {
                    ctxt.result = map.apply(this@Parser.getReturn(ctxt))
                }
                return r
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    /**
     * `p1.or(p2)` is equivalent to `p1 | p2` in EBNF.
     *
     * @param alternative the alternative parser to run if this fails.
     */
    fun or(alternative: Parser<out T>?): Parser<T> {
        return or(this, alternative)
    }

    /**
     * `a.otherwise(fallback)` runs `fallback` when `a` matches zero input. This is different
     * from `a.or(alternative)` where `alternative` is run whenever `a` fails to match.
     *
     *
     * One should usually use [.or].
     *
     * @param fallback the parser to run if `this` matches no input.
     * @since 3.1
     */
    fun otherwise(fallback: Parser<out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val result = ctxt.result
                val at = ctxt.at
                val step = ctxt.step
                val errorIndex = ctxt.errorIndex()
                if (this@Parser.apply(ctxt)) return true
                if (ctxt.errorIndex() != errorIndex) return false
                ctxt[step, at] = result
                return fallback.apply(ctxt)
            }

            override fun toString(): String {
                return "otherwise"
            }
        }
    }

    /**
     * `p.optional()` is equivalent to `p?` in EBNF. `null` is the result when
     * `this` fails with no partial match.
     *
     */
    @Deprecated("since 3.0. Use {@link #optional(null)} or {@link #asOptional} instead.")
    fun optional(): Parser<T> {
        return or(this, always())
    }

    /**
     * `p.asOptional()` is equivalent to `p?` in EBNF. `Optional.empty()`
     * is the result when `this` fails with no partial match. Note that [Optional]
     * prohibits nulls so make sure `this` does not result in `null`.
     *
     * @since 3.0
     */
    fun asOptional(): Parser<Optional<T>> {
        return map(Function { value: T -> Optional.of(value) }).optional(Optional.empty())
    }

    /**
     * A [Parser] that returns `defaultValue` if `this` fails with no partial match.
     */
    fun optional(defaultValue: T): Parser<T> {
        return or(this, constant(defaultValue))
    }
    /**
     * A [Parser] that fails if `this` succeeds. Any input consumption is undone.
     *
     * @param unexpected the name of what we don't expect.
     */
    /**
     * A [Parser] that fails if `this` succeeds. Any input consumption is undone.
     */
    @JvmOverloads fun not(unexpected: String? = toString()): Parser<*> {
        return peek().ifelse(unexpected<Any>(unexpected!!), always())
    }

    /**
     * A [Parser] that runs `this` and undoes any input consumption if succeeds.
     */
    fun peek(): Parser<T> {
        return object: Parser<T>() {
            override fun label(name: String): Parser<T> {
                return this@Parser.label(name).peek()
            }

            override fun apply(ctxt: ParseContext): Boolean {
                val step = ctxt.step
                val at = ctxt.at
                val ok = this@Parser.apply(ctxt)
                if (ok) ctxt.setAt(step, at)
                return ok
            }

            override fun toString(): String {
                return "peek"
            }
        }
    }

    /**
     * A [Parser] that undoes any partial match if `this` fails. In other words, the
     * parser either fully matches, or matches none.
     */
    fun atomic(): Parser<T> {
        return object: Parser<T>() {
            override fun label(name: String): Parser<T> {
                return this@Parser.label(name).atomic()
            }

            override fun apply(ctxt: ParseContext): Boolean {
                val at = ctxt.at
                val step = ctxt.step
                val r = this@Parser.apply(ctxt)
                if (r) ctxt.step = step + 1 else ctxt.setAt(step, at)
                return r
            }

            override fun toString(): String {
                return this@Parser.toString()
            }
        }
    }

    /**
     * A [Parser] that returns `true` if `this` succeeds, `false` otherwise.
     */
    fun succeeds(): Parser<Boolean> {
        return ifelse(Parsers.TRUE, Parsers.FALSE)
    }

    /**
     * A [Parser] that returns `true` if `this` fails, `false` otherwise.
     */
    fun fails(): Parser<Boolean> {
        return ifelse(Parsers.FALSE, Parsers.TRUE)
    }

    /**
     * A [Parser] that runs `consequence` if `this` succeeds, or `alternative` otherwise.
     */
    fun <R> ifelse(consequence: Parser<out R?>, alternative: Parser<out R>): Parser<R> {
        return ifelse(Function { consequence }, alternative)
    }

    /**
     * A [Parser] that runs `consequence` if `this` succeeds, or `alternative` otherwise.
     */
    fun <R> ifelse(
        consequence: Function<in T, out Parser<out R?>>,
        alternative: Parser<out R>): Parser<R> {
        return object: Parser<R>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val ret = ctxt.result
                val step = ctxt.step
                val at = ctxt.at
                if (ctxt.withErrorSuppressed(this@Parser)) {
                    val parser = consequence.apply(this@Parser.getReturn(ctxt))
                    return parser.apply(ctxt)
                }
                ctxt[step, at] = ret
                return alternative.apply(ctxt)
            }

            override fun toString(): String {
                return "ifelse"
            }
        }
    }

    /**
     * A [Parser] that reports reports an error about `name` expected, if `this` fails with no partial
     * match.
     */
    open fun label(name: String): Parser<T> {
        return object: Parser<T>() {
            override fun label(overrideName: String): Parser<T> {
                return this@Parser.label(overrideName)
            }

            override fun apply(ctxt: ParseContext): Boolean {
                return ctxt.applyNewNode(this@Parser, name)
            }

            override fun toString(): String {
                return name
            }
        }
    }

    /**
     * Casts `this` to a [Parser] of type `R`. Use it only if you know the parser actually returns
     * value of type `R`.
     */
    fun <R> cast(): Parser<R> {
        return this as Parser<R>
    }

    /**
     * A [Parser] that runs `this` between `before` and `after`. The return value of `this` is preserved.
     *
     *
     * Equivalent to [Parsers.between], which preserves the natural order of the
     * parsers in the argument list, but is a bit more verbose.
     */
    fun between(before: Parser<*>, after: Parser<*>): Parser<T> {
        return before.next(followedBy(after))
    }

    /**
     * A [Parser] that first runs `before` from the input start,
     * then runs `after` from the input's end, and only
     * then runs `this` on what's left from the input.
     * In effect, `this` behaves reluctantly, giving
     * `after` a chance to grab input that would have been consumed by `this`
     * otherwise.
     */
    @Deprecated("""This method probably only works in the simplest cases. And it's a character-level
    parser only. Use it at your own risk. It may be deleted later when we find a better way.""")
    fun reluctantBetween(before: Parser<*>?, after: Parser<*>?): Parser<T> {
        return ReluctantBetweenParser(before!!, this, after!!)
    }

    /**
     * A [Parser] that runs `this` 1 or more times separated by `delim`.
     *
     *
     * The return values are collected in a [List].
     */
    fun sepBy1(delim: Parser<*>): Parser<List<T>> {
        val afterFirst = delim.asDelimiter().next(this)
        return next(Function<T, Parser<List<T>>> { firstValue: T ->
            RepeatAtLeastParser(
                afterFirst, 0, arrayListFactoryWithFirstElement(firstValue))
        })
    }

    /**
     * A [Parser] that runs `this` 0 or more times separated by `delim`.
     *
     *
     * The return values are collected in a [List].
     */
    fun sepBy(delim: Parser<*>): Parser<List<T>> {
        return or(sepBy1(delim), instance())
    }

    /**
     * A [Parser] that runs `this` for 0 or more times delimited and terminated by
     * `delim`.
     *
     *
     * The return values are collected in a [List].
     */
    fun endBy(delim: Parser<*>): Parser<List<T>> {
        return followedBy(delim).many()
    }

    /**
     * A [Parser] that runs `this` for 1 or more times delimited and terminated by `delim`.
     *
     *
     * The return values are collected in a [List].
     */
    fun endBy1(delim: Parser<*>): Parser<List<T>> {
        return followedBy(delim).many1()
    }

    /**
     * A [Parser] that runs `this` for 1 ore more times separated and optionally terminated by `delim`. For example: `"foo;foo;foo"` and `"foo;foo;"` both matches `foo.sepEndBy1(semicolon)`.
     *
     *
     * The return values are collected in a [List].
     */
    fun sepEndBy1(delim: Parser<*>): Parser<List<T>> {
        return next(Function<T, Parser<out List<T>>> { first: T -> DelimitedParser(this, delim, arrayListFactoryWithFirstElement(first)) })
    }

    /**
     * A [Parser] that runs `this` for 0 ore more times separated and optionally terminated by `delim`. For example: `"foo;foo;foo"` and `"foo;foo;"` both matches `foo.sepEndBy(semicolon)`.
     *
     *
     * The return values are collected in a [List].
     */
    fun sepEndBy(delim: Parser<*>): Parser<List<T>> {
        return or(sepEndBy1(delim), instance())
    }

    /**
     * A [Parser] that runs `op` for 0 or more times greedily, then runs `this`.
     * The [Function] objects returned from `op` are applied from right to left to the
     * return value of `p`.
     *
     *
     *  `p.prefix(op)` is equivalent to `op* p` in EBNF.
     */
    fun prefix(op: Parser<out Function<in T, out T>>): Parser<T> {
        return sequence(op.many(), this, BiFunction { ms: List<Function<in T, out T>>?, a: T -> applyPrefixOperators(ms, a) })
    }

    /**
     * A [Parser] that runs `this` and then runs `op` for 0 or more times greedily.
     * The [Function] objects returned from `op` are applied from left to right to the return
     * value of p.
     *
     *
     * This is the preferred API to avoid `StackOverflowError` in left-recursive parsers.
     * For example, to parse array types in the form of "T[]" or "T[][]", the following
     * left recursive grammar will fail: <pre>   `Terminals terms = Terminals.operators("[", "]");
     * Parser.Reference<Type> ref = Parser.newReference();
     * ref.set(Parsers.or(leafTypeParser,
     * Parsers.sequence(ref.lazy(), terms.phrase("[", "]"), new Unary<Type>() {...})));
     * return ref.get();
    `</pre> *
     * A correct implementation is:  <pre>   `Terminals terms = Terminals.operators("[", "]");
     * return leafTypeParer.postfix(terms.phrase("[", "]").retn(new Unary<Type>() {...}));
    `</pre> *
     * A not-so-obvious example, is to parse the `expr ? a : b` ternary operator. It too is a
     * left recursive grammar. And un-intuitively it can also be thought as a postfix operator.
     * Basically, we can parse "? a : b" as a whole into a unary operator that accepts the condition
     * expression as input and outputs the full ternary expression: <pre>   `Parser<Expr> ternary(Parser<Expr> expr) {
     * return expr.postfix(
     * Parsers.sequence(
     * terms.token("?"), expr, terms.token(":"), expr,
     * (unused, then, unused, orelse) -> cond ->
     * new TernaryExpr(cond, then, orelse)));
     * }
    `</pre> *
     * [OperatorTable] also handles left recursion transparently.
     *
     *
     *  `p.postfix(op)` is equivalent to `p op*` in EBNF.
     */
    fun postfix(op: Parser<out Function<in T, out T>>): Parser<T> {
        return sequence2(this, op.many(), BiFunction { a: T, ms: List<Function<in T, out T>> -> applyPostfixOperators(a, ms) }) as Parser<T>
    }

    /**
     * A [Parser] that parses non-associative infix operator.
     * Runs `this` for the left operand, and then
     * runs `op` and `this` for the operator and the right operand optionally.
     * The [BiFunction] objects
     * returned from `op` are applied to the return values of the two operands, if any.
     *
     *
     *  `p.infixn(op)` is equivalent to `p (op p)?` in EBNF.
     */
    fun infixn(op: Parser<out BiFunction<in T, in T, out T>>): Parser<T> {
        return next(Function<T, Parser<out T>?> { a: T ->
            val shift: Parser<T> = sequence(op, this) { m2: BiFunction<in T, in T, out T>, b: T -> m2.apply(a, b) }
            shift.or(constant(a))
        })
    }

    /**
     * A [Parser] for left-associative infix operator. Runs `this` for the left operand, and then runs
     * `operator` and `this` for the operator and the right operand for 0 or more times greedily.
     * The [BiFunction] objects returned from `operator` are applied from left to right to the
     * return values of `this`, if any. For example:
     * `a + b + c + d` is evaluated as `(((a + b)+c)+d)`.
     *
     *
     *  `p.infixl(op)` is equivalent to `p (op p)*` in EBNF.
     */
    fun infixl(
        operator: Parser<out BiFunction<in T, in T, out T>>): Parser<T> {
        val rightToLeft = BiFunction<BiFunction<in T, in T, out T>, T, Function<in T, out T>> { op: BiFunction<in T, in T, out T>, r: T -> Function { l: T -> op.apply(l, r) } }
        return next(Function<T, Parser<out T>?> { first: T ->
            sequence(operator, this, rightToLeft)
                .many()
                .map(Function<List<Function<in T, out T>>?, T> { maps: List<Function<in T, out T>>? -> applyInfixOperators(first, maps) })
        })
    }

    /**
     * A [Parser] for right-associative infix operator. Runs `this` for the left operand,
     * and then runs `op` and `this` for the operator and the right operand for
     * 0 or more times greedily.
     * The [BiFunction] objects returned from `op` are applied from right to left to the
     * return values of `this`, if any. For example: `a + b + c + d` is evaluated as
     * `a + (b + (c + d))`.
     *
     *
     *  `p.infixr(op)` is equivalent to `p (op p)*` in EBNF.
     */
    fun infixr(op: Parser<out BiFunction<in T, in T, out T>>): Parser<T> {
        val rhs: Parser<Rhs<T>> = sequence(op, this) { op: BiFunction<in T, in T, out T>, rhs: T -> Rhs(op, rhs) }
        return sequence(this, rhs.many(), BiFunction { first: T, rhss: List<Rhs<T>> -> applyInfixrOperators(first, rhss) })
    }

    /**
     * A [Parser] that runs `this` and wraps the return value in a [Token].
     *
     *
     * It is normally not necessary to call this method explicitly. [.lexer] and [.from] both do the conversion automatically.
     */
    fun token(): Parser<Token?> {
        return object: Parser<Token?>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val begin = ctxt.index
                if (!this@Parser.apply(ctxt)) {
                    return false
                }
                val len = ctxt.index - begin
                val token = Token(begin, len, ctxt.result)
                ctxt.result = token
                return true
            }

            override fun toString(): String {
                return this@Parser.toString()
            }
        }
    }

    /**
     * A [Parser] that returns the matched string in the original source.
     */
    fun source(): Parser<String> {
        return object: Parser<String>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val begin = ctxt.index
                if (!this@Parser.apply(ctxt)) {
                    return false
                }
                ctxt.result = ctxt.source.subSequence(begin, ctxt.index).toString()
                return true
            }

            override fun toString(): String {
                return "source"
            }
        }
    }

    /**
     * A [Parser] that returns both parsed object and matched string.
     */
    fun withSource(): Parser<WithSource<T>> {
        return object: Parser<WithSource<T>>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val begin = ctxt.index
                if (!this@Parser.apply(ctxt)) {
                    return false
                }
                val source = ctxt.source.subSequence(begin, ctxt.index).toString()
                val withSource = WithSource(ctxt.result as T?, source)
                ctxt.result = withSource
                return true
            }

            override fun toString(): String {
                return this@Parser.toString()
            }
        }
    }

    /**
     * A [Parser] that takes as input the [Token] collection returned by `lexer`,
     * and runs `this` to parse the tokens. Most parsers should use the simpler
     * [.from] instead.
     *
     *
     *  `this` must be a token level parser.
     */
    fun from(lexer: Parser<out Collection<Token?>>): Parser<T> {
        return nested(tokens(lexer), followedBy(Parsers.EOF))
    }

    /**
     * A [Parser] that takes as input the tokens returned by `tokenizer` delimited by
     * `delim`, and runs `this` to parse the tokens. A common misunderstanding is that
     * `tokenizer` has to be a parser of [Token]. It doesn't need to be because
     * `Terminals` already takes care of wrapping your logical token objects into physical
     * `Token` with correct source location information tacked on for free. Your token object
     * can literally be anything, as long as your token level parser can recognize it later.
     *
     *
     * The following example uses `Terminals.tokenizer()`: <pre class="code">
     * Terminals terminals = ...;
     * return parser.from(terminals.tokenizer(), Scanners.WHITESPACES.optional()).parse(str);
    </pre> *
     * And tokens are optionally delimited by whitespaces.
     *
     * Optionally, you can skip comments using an alternative scanner than `WHITESPACES`:
     * <pre class="code">   `Terminals terminals = ...;
     * Parser<?> delim = Parsers.or(
     * Scanners.WHITESPACE,
     * Scanners.JAVA_LINE_COMMENT,
     * Scanners.JAVA_BLOCK_COMMENT).skipMany();
     * return parser.from(terminals.tokenizer(), delim).parse(str);
    `</pre> *
     *
     *
     * In both examples, it's important to make sure the delimiter scanner can accept empty string
     * (either through [.optional] or [.skipMany]), unless adjacent operator
     * characters shouldn't be parsed as separate operators.
     * i.e. "((" as two left parenthesis operators.
     *
     *
     *  `this` must be a token level parser.
     */
    fun from(tokenizer: Parser<*>, delim: Parser<Unit>): Parser<T> {
        return from(tokenizer.lexer(delim))
    }

    /**
     * A [Parser] that greedily runs `this` repeatedly, and ignores the pattern recognized by `delim`
     * before and after each occurrence. The result tokens are wrapped in [Token] and are collected and returned
     * in a [List].
     *
     * It is normally not necessary to call this method explicitly. [.from] is more convenient
     * for simple uses that just need to connect a token level parser with a lexer that produces the tokens. When more
     * flexible control over the token list is needed, for example, to parse indentation sensitive language, a
     * pre-processor of the token list may be needed.
     *
     *  `this` must be a tokenizer that returns a token value.
     */
    fun lexer(delim: Parser<*>): Parser<List<Token>> {
        return (delim as Parser<Any>).optional(unexpected<Any>("<null>")).next(token().sepEndBy(delim)) as Parser<List<Token>>
    }

    /**
     * As a delimiter, the parser's error is considered lenient and will only be reported if no other
     * meaningful error is encountered. The delimiter's logical step is also considered 0, which means
     * it won't ever stop repetition combinators such as [.many].
     */
    fun asDelimiter(): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                return ctxt.applyAsDelimiter(this@Parser)
            }

            override fun toString(): String {
                return this@Parser.toString()
            }
        }
    }

    /**
     * Parses source read from `readable`.
     */
    @Throws(IOException::class) fun parse(readable: Readable): T {
        return parse(read(readable))
    }
    /**
     * Parses `source` under the given `mode`. For example: <pre>
     * try {
     * parser.parse(text, Mode.DEBUG);
     * } catch (ParserException e) {
     * ParseTree parseTree = e.getParseTree();
     * ...
     * }
    </pre> *
     *
     * @since 2.3
     */
    /**
     * Parses `source`.
     */
    @JvmOverloads
    fun parse(source: CharSequence?, mode: Mode = Mode.PRODUCTION): T {
        return mode.run(this, source)
    }

    /**
     * Parses `source` and returns a [ParseTree] corresponding to the syntactical
     * structure of the input. Only [labeled][.label] parser nodes are represented in the parse
     * tree.
     *
     *
     * If parsing failed, [ParserException.getParseTree] can be inspected for the parse
     * tree at error location.
     *
     * @since 2.3
     */
    fun parseTree(source: CharSequence?): ParseTree? {
        val state = ScannerState(source!!)
        state.enableTrace("root")
        state.run(followedBy(Parsers.EOF))
        return state.buildParseTree()
    }

    /**
     * Defines the mode that a parser should be run in.
     *
     * @since 2.3
     */
    enum class Mode {
        /** Default mode. Used for production.  */
        PRODUCTION {
            override fun <T> run(parser: Parser<T>, source: CharSequence?): T {
                return ScannerState(source!!).run(parser.followedBy(Parsers.EOF))
            }
        },

        /**
         * Debug mode. [ParserException.getParseTree] can be used to inspect partial parse result.
         */
        DEBUG {
            override fun <T> run(parser: Parser<T>, source: CharSequence?): T {
                val state = ScannerState(source!!)
                state.enableTrace("root")
                return state.run(parser.followedBy(Parsers.EOF))
            }
        };

        abstract fun <T> run(parser: Parser<T>, source: CharSequence?): T
    }

    /**
     * Parses `source`.
     *
     * @param source     the source string
     * @param moduleName the name of the module, this name appears in error message
     * @return the result
     */
    @Deprecated("Please use {@link #parse(CharSequence)} instead.")
    fun parse(source: CharSequence?, moduleName: String?): T {
        return ScannerState(moduleName, source!!, 0, SourceLocator(source))
            .run(followedBy(Parsers.EOF))
    }

    /**
     * Parses source read from `readable`.
     *
     * @param readable   where the source is read from
     * @param moduleName the name of the module, this name appears in error message
     * @return the result
     */
    @Deprecated("Please use {@link #parse(Readable)} instead.") @Throws(IOException::class)
    fun parse(readable: Readable, moduleName: String?): T {
        return parse(read(readable), moduleName)
    }

    abstract fun apply(ctxt: ParseContext): Boolean

    fun getReturn(ctxt: ParseContext): T {
        return ctxt.result as T
    }

    // 1+ 1+ 1+ ..... 1
    private class Rhs<T>(val op: BiFunction<in T, in T, out T>, val rhs: T) {
        override fun toString(): String {
            return "$op $rhs"
        }

    }

    companion object {
        /**
         * Creates a new instance of [Reference].
         * Used when your grammar is recursive (many grammars are).
         */
        @JvmStatic fun <T> newReference(): Reference<T> {
            return Reference()
        }

        /**
         * Copies all content from `from` to `to`.
         */
        @Private @Throws(IOException::class)
        fun read(from: Readable): StringBuilder {
            val builder = StringBuilder()
            val buf = CharBuffer.allocate(2048)
            while (true) {
                val r = from.read(buf)
                if (r == -1) break
                buf.flip()
                builder.append(buf, 0, r)
            }
            return builder
        }

        private fun <T> applyPrefixOperators(
            ms: List<Function<in T, out T>>?, a: T): T {
            var a = a
            for (i in ms!!.indices.reversed()) {
                val m = ms[i]
                a = m.apply(a)
            }
            return a
        }

        private fun <T> applyPostfixOperators(a: T, ms: Iterable<Function<in T, out T>>): T? {
            var a = a
            for (m in ms) {
                a = m.apply(a)
            }
            return a
        }

        private fun <T> applyInfixOperators(
            initialValue: T, functions: List<Function<in T, out T>>?): T {
            var result = initialValue
            for (function in functions!!) {
                result = function.apply(result)
            }
            return result
        }

        private fun <T> applyInfixrOperators(first: T, rhss: List<Rhs<T>>): T {
            if (rhss.isEmpty()) return first
            val lastIndex = rhss.size - 1
            var o2 = rhss[lastIndex].rhs
            for (i in lastIndex downTo 1) {
                val o1 = rhss[i - 1].rhs
                o2 = rhss[i].op.apply(o1, o2)
            }
            return rhss[0].op.apply(first, o2)
        }
    }
}