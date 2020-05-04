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

import org.jparsec.functors.*
import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.Lists.arrayList
import java.util.*
import java.util.function.BiFunction

/**
 * Provides common [Parser] implementations.
 *
 * @author Ben Yu
 */
object Parsers {
    /** [Parser] that succeeds only if EOF is met. Fails otherwise.  */
    @JvmField
    val EOF = eof("EOF")

    /** A [Parser] that consumes a token. The token value is returned from the parser.  */
    val ANY_TOKEN = token(object: TokenMap<Any> {
        override fun map(token: Token): Any {
            return token.value()!!
        }

        override fun toString(): String {
            return "any token"
        }
    })

    /**
     * A [Parser] that retrieves the current index in the source.
     *
     */
    @Deprecated("Use {@link #SOURCE_LOCATION} instead.")
    val INDEX: Parser<Int> = object: Parser<Int>() {
        override fun apply(ctxt: ParseContext): Boolean {
            ctxt.result = ctxt.index
            return true
        }

        override fun toString(): String {
            return "getIndex"
        }
    }

    /**
     * A [Parser] that returns the current location in the source.
     *
     *
     * Because [SourceLocation.getLine] and [SourceLocation.getColumn] take amortized
     * `log(n)` time, it's more efficient to avoid calling them until the entire source has been
     * parsed successfully. In other words, avoid `SOURCE_LOCATION.map(SourceLocation::getLine)` or
     * anything similar.
     *
     *
     * `SourceLocation#getIndex` can be called any time.
     *
     * @since 3.1
     */
    val SOURCE_LOCATION: Parser<SourceLocation> = object: Parser<SourceLocation>() {
        override fun apply(ctxt: ParseContext): Boolean {
            ctxt.result = SourceLocation(ctxt.index, ctxt.locator)
            return true
        }

        override fun toString(): String {
            return "SOURCE_LOCATION"
        }
    }
    private val ALWAYS: Parser<*> = constant<Any?>(null)
    private val NEVER: Parser<*> = object: Parser<Any?>() {
        override fun apply(ctxt: ParseContext): Boolean {
            return false
        }

        override fun toString(): String {
            return "never"
        }
    }
    @JvmField
    val TRUE = constant(true)
    @JvmField
    val FALSE = constant(false)

    /** [Parser] that always succeeds.  */
    @JvmStatic fun <T> always(): Parser<T> {
        return ALWAYS as Parser<T>
    }

    /** [Parser] that always fails.  */
    @JvmStatic fun <T> never(): Parser<T> {
        return NEVER as Parser<T>
    }

    /** A [Parser] that succeeds only if EOF is met. Fails with `message` otherwise.  */
    fun eof(message: String): Parser<Any?> {
        return object: Parser<Any?>() {
            override fun apply(ctxt: ParseContext): Boolean {
                if (ctxt.isEof) return true
                ctxt.missing(message)
                return false
            }

            override fun toString(): String {
                return message
            }
        }
    }

    /** A [Parser] that always fails with `message`.  */
    @JvmStatic fun <T> fail(message: String): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                ctxt.fail(message)
                return false
            }

            override fun toString(): String {
                return message
            }
        }
    }

    /**
     * A [Parser] that always succeeds and invokes `runnable`.
     */
    @Deprecated("") fun runnable(runnable: Runnable): Parser<*> {
        return object: Parser<Any?>() {
            override fun apply(ctxt: ParseContext): Boolean {
                runnable.run()
                return true
            }

            override fun toString(): String {
                return runnable.toString()
            }
        }
    }

    /** Converts a parser of a collection of [Token] to a parser of an array of `Token`. */
    @JvmStatic fun tokens(parser: Parser<out Collection<Token?>>): Parser<Array<Token?>> {
        return parser.map { list -> list.toTypedArray() }
    }

    /**
     * A [Parser] that takes as input the array of [Token] returned from `lexer`,
     * and feeds the tokens as input into `parser`.
     *
     *
     *  It fails if either `lexer` or `parser` fails.
     *
     * @param lexer the lexer object that returns an array of Tok objects.
     * @param parser the token level parser object.
     * @return the new Parser object.
     */
    @JvmStatic fun <T> nested(lexer: Parser<Array<Token?>>, parser: Parser<out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                if (!lexer.apply(ctxt)) return false
                val tokens = lexer.getReturn(ctxt)
                val parserState = ParserState(ctxt.module, ctxt.source, tokens, 0, ctxt.locator, ctxt.index, tokens)
                ctxt.trace.startFresh(parserState)
                return ctxt.applyNested(parser, parserState)
            }

            override fun toString(): String {
                return parser.toString()
            }
        }
    }

    /******************** monadic combinators *******************  */

    /** A [Parser] that always returns `v` regardless of input.  */
    @JvmStatic fun <T> constant(v: T): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                ctxt.result = v
                return true
            }

            override fun toString(): String {
                return v.toString()
            }
        }
    }

    /**
     * A [Parser] that runs 2 parser objects sequentially. `p1` is executed,
     * if it succeeds, `p2` is executed.
     */
    @JvmStatic fun <T> sequence(p1: Parser<*>, p2: Parser<T>): Parser<T> {
        return sequence(p1, p2, InternalFunctors.lastOfTwo())
    }

    /** A [Parser] that runs 3 parser objects sequentially.  */
    @JvmStatic fun <T> sequence(p1: Parser<*>, p2: Parser<*>, p3: Parser<T>): Parser<T> {
        return sequence(p1, p2, p3, InternalFunctors.lastOfThree())
    }

    /** A [Parser] that runs 4 parser objects sequentially.  */
    @JvmStatic fun <T> sequence(
        p1: Parser<*>, p2: Parser<*>, p3: Parser<*>, p4: Parser<T>): Parser<T> {
        return sequence(p1, p2, p3, p4, InternalFunctors.lastOfFour())
    }

    /** A [Parser] that runs 5 parser objects sequentially.  */
    fun <T> sequence(
        p1: Parser<*>, p2: Parser<*>, p3: Parser<*>, p4: Parser<*>, p5: Parser<T>): Parser<T> {
        return sequence(p1, p2, p3, p4, p5,
                        InternalFunctors.lastOfFive())
    }

    /**
     * A [Parser] that sequentially runs `p1` and `p2` and collects the results in a
     * [Pair] object. Is equivalent to [.tuple].
     *
     */
    @JvmStatic @Deprecated("Prefer to converting to your own object with a lambda.")
    fun <A, B> pair(p1: Parser<out A>, p2: Parser<out B>): Parser<org.jparsec.functors.Pair<A, B>> {
        return sequence(p1, p2) { a: A, b: B -> org.jparsec.functors.Pair(a, b) }
    }

    /**
     * A [Parser] that sequentially runs `p1` and `p2` and collects the results in a
     * [Pair] object. Is equivalent to [.pair].
     *
     */
    @Deprecated("Prefer to converting to your own object with a lambda.")
    fun <A, B> tuple(p1: Parser<out A>, p2: Parser<out B>): Parser<org.jparsec.functors.Pair<A, B>> {
        return pair(p1, p2)
    }

    /**
     * A [Parser] that sequentially runs 3 parser objects and collects the results in a
     * [Tuple3] object.
     *
     */
    @Deprecated("Prefer to converting to your own object with a lambda.") fun <A, B, C> tuple(
        p1: Parser<out A>, p2: Parser<out B>, p3: Parser<out C>): Parser<Tuple3<A, B, C>> {
        return sequence(p1, p2, p3) { a: A, b: B, c: C -> Tuple3(a, b, c) }
    }

    /**
     * A [Parser] that sequentially runs 4 parser objects and collects the results in a
     * [Tuple4] object.
     *
     */
    @Deprecated("Prefer to converting to your own object with a lambda.") fun <A, B, C, D> tuple(
        p1: Parser<out A>, p2: Parser<out B>,
        p3: Parser<out C>, p4: Parser<out D>): Parser<Tuple4<A, B, C, D>> {
        return sequence(p1, p2, p3, p4) { a: A, b: B, c: C, d: D -> Tuple4(a, b, c, d) }
    }

    /**
     * A [Parser] that sequentially runs 5 parser objects and collects the results in a
     * [Tuple5] object.
     *
     */
    @Deprecated("Prefer to converting to your own object with a lambda.") fun <A, B, C, D, E> tuple(
        p1: Parser<out A>, p2: Parser<out B>, p3: Parser<out C>,
        p4: Parser<out D>, p5: Parser<out E>): Parser<Tuple5<A, B, C, D, E>> {
        return sequence(p1, p2, p3, p4, p5) { a: A, b: B, c: C, d: D, e: E -> Tuple5(a, b, c, d, e) }
    }

    /**
     * A [Parser] that sequentially runs `parsers` one by one and collects the return
     * values in an array.
     */
    fun array(vararg parsers: Parser<*>): Parser<Array<Any>> {
        return object: Parser<Array<Any>>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val ret = arrayOfNulls<Any>(parsers.size)
                for (i in parsers.indices) {
                    val parser = parsers[i]
                    if (!parser.apply(ctxt)) return false
                    ret[i] = parser.getReturn(ctxt)
                }
                ctxt.result = ret
                return true
            }

            override fun toString(): String {
                return "array"
            }
        }
    }

    /**
     * A [Parser] that sequentially runs `parsers` one by one and collects the return
     * values in a [List].
     */
    @JvmStatic fun <T> list(parsers: Iterable<Parser<out T>>): Parser<List<T>> {
        val array: Array<Parser<T>> = toArray(parsers)
        return object: Parser<List<T>>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val list: ArrayList<T> = arrayList(array.size)
                for (parser in array) {
                    if (!parser.apply(ctxt)) return false
                    list.add(parser.getReturn(ctxt))
                }
                ctxt.result = list
                return true
            }

            override fun toString(): String {
                return "list"
            }
        }
    }

    /**
     * Equivalent to [Parser.between]. Use this to list the parsers in the
     * natural order.
     */
    @JvmStatic fun <T> between(before: Parser<*>, parser: Parser<T>, after: Parser<*>): Parser<T> {
        return parser.between(before, after)
    }

    /**
     * A [Parser] that runs `p1` and `p2` sequentially
     * and transforms the return values using `map`.
     */
    @JvmStatic fun <A, B, T> sequence(p1: Parser<A>, p2: Parser<B>, map: BiFunction<in A, in B, out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r1 = p1.apply(ctxt)
                if (!r1) return false
                val o1 = p1.getReturn(ctxt)
                val r2 = p2.apply(ctxt)
                if (!r2) return false
                val o2 = p2.getReturn(ctxt)
                ctxt.result = map.apply(o1, o2)
                return true
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    fun <A, B, T> sequence2(p1: Parser<A>, p2: Parser<B>, map: BiFunction<in A, in B, out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r1 = p1.apply(ctxt)
                if (!r1) return false
                val o1 = p1.getReturn(ctxt)
                val r2 = p2.apply(ctxt)
                if (!r2) return false
                val o2 = p2.getReturn(ctxt)
                ctxt.result = map.apply(o1, o2)
                return true
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    /**
     * A [Parser] that runs 3 parser objects sequentially and transforms the return values
     * using `map`.
     */
    @JvmStatic fun <A, B, C, T> sequence(
        p1: Parser<A>, p2: Parser<B>, p3: Parser<C>,
        map: Map3<in A, in B, in C, out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r1 = p1.apply(ctxt)
                if (!r1) return false
                val o1 = p1.getReturn(ctxt)
                val r2 = p2.apply(ctxt)
                if (!r2) return false
                val o2 = p2.getReturn(ctxt)
                val r3 = p3.apply(ctxt)
                if (!r3) return false
                val o3 = p3.getReturn(ctxt)
                ctxt.result = map.map(o1, o2, o3)
                return true
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    /**
     * A [Parser] that runs 4 parser objects sequentially and transforms the return values
     * using `map`.
     */
    @JvmStatic fun <A, B, C, D, T> sequence(
        p1: Parser<A>, p2: Parser<B>, p3: Parser<C>, p4: Parser<D>,
        map: Map4<in A, in B, in C, in D, out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r1 = p1.apply(ctxt)
                if (!r1) return false
                val o1 = p1.getReturn(ctxt)
                val r2 = p2.apply(ctxt)
                if (!r2) return false
                val o2 = p2.getReturn(ctxt)
                val r3 = p3.apply(ctxt)
                if (!r3) return false
                val o3 = p3.getReturn(ctxt)
                val r4 = p4.apply(ctxt)
                if (!r4) return false
                val o4 = p4.getReturn(ctxt)
                ctxt.result = map.map(o1, o2, o3, o4)
                return true
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    /**
     * A [Parser] that runs 5 parser objects sequentially and transforms the return values
     * using `map`.
     */
    @JvmStatic fun <A, B, C, D, E, T> sequence(
        p1: Parser<A>, p2: Parser<B>, p3: Parser<C>, p4: Parser<D>, p5: Parser<E>,
        map: Map5<in A, in B, in C, in D, in E, out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r1 = p1.apply(ctxt)
                if (!r1) return false
                val o1 = p1.getReturn(ctxt)
                val r2 = p2.apply(ctxt)
                if (!r2) return false
                val o2 = p2.getReturn(ctxt)
                val r3 = p3.apply(ctxt)
                if (!r3) return false
                val o3 = p3.getReturn(ctxt)
                val r4 = p4.apply(ctxt)
                if (!r4) return false
                val o4 = p4.getReturn(ctxt)
                val r5 = p5.apply(ctxt)
                if (!r5) return false
                val o5 = p5.getReturn(ctxt)
                ctxt.result = map.map(o1, o2, o3, o4, o5)
                return true
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    /**
     * A [Parser] that runs 6 parser objects sequentially and transforms the return values
     * using `map`.
     *
     * @since 3.0
     */
    @JvmStatic fun <A, B, C, D, E, F, T> sequence(
        p1: Parser<A>, p2: Parser<B>, p3: Parser<C>,
        p4: Parser<D>, p5: Parser<E>, p6: Parser<F>,
        map: Map6<in A, in B, in C, in D, in E, in F, out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r1 = p1.apply(ctxt)
                if (!r1) return false
                val o1 = p1.getReturn(ctxt)
                val r2 = p2.apply(ctxt)
                if (!r2) return false
                val o2 = p2.getReturn(ctxt)
                val r3 = p3.apply(ctxt)
                if (!r3) return false
                val o3 = p3.getReturn(ctxt)
                val r4 = p4.apply(ctxt)
                if (!r4) return false
                val o4 = p4.getReturn(ctxt)
                val r5 = p5.apply(ctxt)
                if (!r5) return false
                val o5 = p5.getReturn(ctxt)
                val r6 = p6.apply(ctxt)
                if (!r6) return false
                val o6 = p6.getReturn(ctxt)
                ctxt.result = map.map(o1, o2, o3, o4, o5, o6)
                return true
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    /**
     * A [Parser] that runs 7 parser objects sequentially and transforms the return values
     * using `map`.
     *
     * @since 3.0
     */
    @JvmStatic fun <A, B, C, D, E, F, G, T> sequence(
        p1: Parser<A>, p2: Parser<B>, p3: Parser<C>,
        p4: Parser<D>, p5: Parser<E>, p6: Parser<F>, p7: Parser<G>,
        map: Map7<in A, in B, in C, in D, in E, in F, in G, out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r1 = p1.apply(ctxt)
                if (!r1) return false
                val o1 = p1.getReturn(ctxt)
                val r2 = p2.apply(ctxt)
                if (!r2) return false
                val o2 = p2.getReturn(ctxt)
                val r3 = p3.apply(ctxt)
                if (!r3) return false
                val o3 = p3.getReturn(ctxt)
                val r4 = p4.apply(ctxt)
                if (!r4) return false
                val o4 = p4.getReturn(ctxt)
                val r5 = p5.apply(ctxt)
                if (!r5) return false
                val o5 = p5.getReturn(ctxt)
                val r6 = p6.apply(ctxt)
                if (!r6) return false
                val o6 = p6.getReturn(ctxt)
                val r7 = p7.apply(ctxt)
                if (!r7) return false
                val o7 = p7.getReturn(ctxt)
                ctxt.result = map.map(o1, o2, o3, o4, o5, o6, o7)
                return true
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    /**
     * A [Parser] that runs 7 parser objects sequentially and transforms the return values
     * using `map`.
     *
     * @since 3.0
     */
    @JvmStatic fun <A, B, C, D, E, F, G, H, T> sequence(
        p1: Parser<A>, p2: Parser<B>, p3: Parser<C>, p4: Parser<D>,
        p5: Parser<E>, p6: Parser<F>, p7: Parser<G>, p8: Parser<H>,
        map: Map8<in A, in B, in C, in D, in E, in F, in G, in H, out T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val r1 = p1.apply(ctxt)
                if (!r1) return false
                val o1 = p1.getReturn(ctxt)
                val r2 = p2.apply(ctxt)
                if (!r2) return false
                val o2 = p2.getReturn(ctxt)
                val r3 = p3.apply(ctxt)
                if (!r3) return false
                val o3 = p3.getReturn(ctxt)
                val r4 = p4.apply(ctxt)
                if (!r4) return false
                val o4 = p4.getReturn(ctxt)
                val r5 = p5.apply(ctxt)
                if (!r5) return false
                val o5 = p5.getReturn(ctxt)
                val r6 = p6.apply(ctxt)
                if (!r6) return false
                val o6 = p6.getReturn(ctxt)
                val r7 = p7.apply(ctxt)
                if (!r7) return false
                val o7 = p7.getReturn(ctxt)
                val r8 = p8.apply(ctxt)
                if (!r8) return false
                val o8 = p8.getReturn(ctxt)
                ctxt.result = map.map(o1, o2, o3, o4, o5, o6, o7, o8)
                return true
            }

            override fun toString(): String {
                return map.toString()
            }
        }
    }

    /** A [Parser] that runs `parsers` sequentially and discards the return values.  */
    @JvmStatic fun sequence(vararg parsers: Parser<*>): Parser<Any> {
        return object: Parser<Any>() {
            override fun apply(ctxt: ParseContext): Boolean {
                for (p in parsers) {
                    if (!p.apply(ctxt)) return false
                }
                return true
            }

            override fun toString(): String {
                return "sequence"
            }
        }
    }

    /** A [Parser] that runs `parsers` sequentially and discards the return values.  */
    @JvmStatic fun sequence(parsers: Iterable<Parser<*>>): Parser<Any> {
        return sequence(*toArray(parsers))
    }

    /**
     * A [Parser] that tries 2 alternative parser objects.
     * Fallback happens regardless of partial match.
     */
    @JvmStatic fun <T> or(p1: Parser<out T>?, p2: Parser<out T>?): Parser<T> {
        return alt(p1!!, p2!!).cast()
    }

    /**
     * A [Parser] that tries 3 alternative parser objects.
     * Fallback happens regardless of partial match.
     */
    @JvmStatic fun <T> or(
        p1: Parser<out T>?, p2: Parser<out T>?, p3: Parser<out T>?): Parser<T> {
        return alt(p1!!, p2!!, p3!!).cast()
    }

    /**
     * A [Parser] that tries 4 alternative parser objects.
     * Fallback happens regardless of partial match.
     */
    @JvmStatic fun <T> or(
        p1: Parser<out T>?, p2: Parser<out T>?,
        p3: Parser<out T>?, p4: Parser<out T>?): Parser<T> {
        return alt(p1!!, p2!!, p3!!, p4!!).cast()
    }

    /**
     * A [Parser] that tries 5 alternative parser objects.
     * Fallback happens regardless of partial match.
     */
    @JvmStatic fun <T> or(
        p1: Parser<out T>?, p2: Parser<out T>?, p3: Parser<out T>?,
        p4: Parser<out T>?, p5: Parser<out T>?): Parser<T> {
        return alt(p1!!, p2!!, p3!!, p4!!, p5!!).cast()
    }

    /**
     * A [Parser] that tries 6 alternative parser objects.
     * Fallback happens regardless of partial match.
     */
    @JvmStatic fun <T> or(
        p1: Parser<out T>?, p2: Parser<out T>?, p3: Parser<out T>?,
        p4: Parser<out T>?, p5: Parser<out T>?, p6: Parser<out T>?): Parser<T> {
        return alt(p1!!, p2!!, p3!!, p4!!, p5!!, p6!!).cast()
    }

    /**
     * A [Parser] that tries 7 alternative parser objects.
     * Fallback happens regardless of partial match.
     */
    @JvmStatic fun <T> or(
        p1: Parser<out T>?, p2: Parser<out T>?, p3: Parser<out T>?,
        p4: Parser<out T>?, p5: Parser<out T>?, p6: Parser<out T>?,
        p7: Parser<out T>?): Parser<T> {
        return alt(p1!!, p2!!, p3!!, p4!!, p5!!, p6!!, p7!!).cast()
    }

    /**
     * A [Parser] that tries 8 alternative parser objects.
     * Fallback happens regardless of partial match.
     */
    @JvmStatic fun <T> or(
        p1: Parser<out T>?, p2: Parser<out T>?, p3: Parser<out T>?,
        p4: Parser<out T>?, p5: Parser<out T>?, p6: Parser<out T>?,
        p7: Parser<out T>?, p8: Parser<out T>?): Parser<T> {
        return alt(p1!!, p2!!, p3!!, p4!!, p5!!, p6!!, p7!!, p8!!).cast()
    }

    /**
     * A [Parser] that tries 9 alternative parser objects.
     * Fallback happens regardless of partial match.
     */
    @JvmStatic fun <T> or(
        p1: Parser<out T>?, p2: Parser<out T>?, p3: Parser<out T>?,
        p4: Parser<out T>?, p5: Parser<out T>?, p6: Parser<out T>?,
        p7: Parser<out T>?, p8: Parser<out T>?, p9: Parser<out T>?): Parser<T> {
        return alt(p1!!, p2!!, p3!!, p4!!, p5!!, p6!!, p7!!, p8!!, p9!!).cast()
    }

    /**
     * A [Parser] that tries each alternative parser in `alternatives`.
     *
     *
     *  Different than [.alt], it requires all alternative parsers to have
     * type `T`.
     */
    @JvmStatic fun <T> or(vararg alternatives: Parser<out T>): Parser<T> {
        if (alternatives.isEmpty()) return never()
        return if (alternatives.size == 1) alternatives[0].cast() else object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                val result = ctxt.result
                val at = ctxt.at
                val step = ctxt.step
                for (p in alternatives) {
                    if (p.apply(ctxt)) {
                        return true
                    }
                    ctxt[step, at] = result
                }
                return false
            }

            override fun toString(): String {
                return "or"
            }
        }
    }

    /**
     * A [Parser] that tries each alternative parser in `alternatives`.
     */
    @JvmStatic fun <T> or(alternatives: Iterable<Parser<out T>>): Parser<T> {
        return or(*toArray(alternatives))
    }

    /** Allows the overloads of "or()" to call the varargs version of "or" with no ambiguity.  */
    private fun alt(vararg alternatives: Parser<*>): Parser<Any?> {
        return or(*alternatives)
    }

    /**
     * A [Parser] that runs both `p1` and `p2` and selects the longer match.
     * If both matches the same length, the first one is favored.
     */
    fun <T> longer(p1: Parser<out T>, p2: Parser<out T>): Parser<T> {
        return longest(p1, p2)
    }

    /**
     * A [Parser] that runs every element of `parsers` and selects the longest match.
     * If two matches have the same length, the first one is favored.
     */
    fun <T> longest(vararg parsers: Parser<out T>): Parser<T> {
        if (parsers.isEmpty()) return never()
        return if (parsers.size == 1) parsers[0].cast() else BestParser(parsers.toList().toTypedArray(), IntOrder.GT)
    }

    /**
     * A [Parser] that runs every element of `parsers` and selects the longest match.
     * If two matches have the same length, the first one is favored.
     */
    fun <T> longest(parsers: Iterable<Parser<out T>>): Parser<T> {
        return longest(*toArray(parsers))
    }

    /**
     * A [Parser] that runs both `p1` and `p2` and selects the shorter match.
     * If both matches the same length, the first one is favored.
     */
    fun <T> shorter(p1: Parser<out T>, p2: Parser<out T>): Parser<T> {
        return shortest(p1, p2)
    }

    /**
     * A [Parser] that runs every element of `parsers` and selects the shortest match.
     * If two matches have the same length, the first one is favored.
     */
    fun <T> shortest(vararg parsers: Parser<out T>): Parser<T> {
        if (parsers.size == 0) return never()
        return if (parsers.size == 1) parsers[0].cast() else BestParser(parsers.toList().toTypedArray(), IntOrder.LT)
    }

    /**
     * A [Parser] that runs every element of `parsers` and selects the shortest match.
     * If two matches have the same length, the first one is favored.
     */
    fun <T> shortest(parsers: Iterable<Parser<out T>>): Parser<T> {
        return shortest(*toArray(parsers))
    }

    /** A [Parser] that fails and reports that `name` is logically expected.  */
    @JvmStatic fun <T> expect(name: String): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                ctxt.expected(name)
                return false
            }

            override fun toString(): String {
                return name
            }
        }
    }

    /** A [Parser] that fails and reports that `name` is logically unexpected.  */
    @JvmStatic fun <T> unexpected(name: String): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                ctxt.unexpected(name)
                return false
            }

            override fun toString(): String {
                return name
            }
        }
    }

    /**
     * Checks the current token with the `fromToken` object. If the
     * [TokenMap.map] method returns null, an unexpected token error occurs;
     * if the method returns a non-null value, the value is returned and the parser succeeds.
     *
     * @param fromToken the `FromToken` object.
     * @return the new Parser object.
     */
    @JvmStatic fun <T> token(fromToken: TokenMap<T>): Parser<T> {
        return object: Parser<T>() {
            override fun apply(ctxt: ParseContext): Boolean {
                if (ctxt.isEof) {
                    ctxt.missing(fromToken)
                    return false
                }
                val token = ctxt.token
                val v: Any? = fromToken.map(token)
                if (v == null) {
                    ctxt.missing(fromToken)
                    return false
                }
                ctxt.result = v
                ctxt.next()
                return true
            }

            override fun toString(): String {
                return fromToken.toString()
            }
        }
    }

    /**
     * Checks whether the current token value is of `type`, in which case, the token value is
     * returned and parse succeeds.
     *
     * @param type the expected token value type.
     * @param name the name of what's logically expected.
     * @return the new Parser object.
     */
    @JvmStatic fun <T> tokenType(type: Class<out T>, name: String): Parser<T> {
        return token(object: TokenMap<T> {
            override fun map(token: Token): T {
                return if (type.isInstance(token.value())) {
                    type.cast(token.value())
                } else null as T
            }

            override fun toString(): String {
                return name
            }
        })
    }

    @Private fun <T> toArrayWithIteration(parsers: Iterable<Parser<out T>>): Array<Parser<T>> {
        val list: ArrayList<Parser<out T>> = arrayList()
        for (parser in parsers) {
            list.add(parser)
        }
        return toArray(list)
    }

    /**
     * We always convert [Iterable] to an array to avoid the cost of creating
     * a new {@Link java.util.Iterator} object each time the parser runs.
     */
    @Private fun <T> toArray(parsers: Iterable<Parser<out T>>): Array<Parser<T>> {
        return if (parsers is Collection<*>) (parsers as Collection<Parser<T>>).toTypedArray() else toArrayWithIteration(parsers)
    }

}