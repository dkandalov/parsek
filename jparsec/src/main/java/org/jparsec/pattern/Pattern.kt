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
package org.jparsec.pattern

import org.jparsec.Parser
import org.jparsec.Scanners
import org.jparsec.internal.util.Checks.checkMax
import org.jparsec.internal.util.Checks.checkMin
import org.jparsec.internal.util.Checks.checkMinMax
import org.jparsec.internal.util.Checks.checkNonNegative
import org.jparsec.pattern.RepeatPattern.Companion.matchRepeat
import org.jparsec.pattern.UpperBoundedPattern.Companion.matchSome

/**
 * Encapsulates algorithm to recognize certain string pattern. When fed with a character range,
 * a [Pattern] object either fails to match, or matches with the match length returned.
 * There is no error reported on where and what exactly failed.
 *
 * @author Ben Yu
 */
abstract class Pattern {
    /**
     * Matches character range against the pattern. The length of the range is `end - begin`.
     *
     * @param src the source string.
     * @param begin the beginning index in the sequence.
     * @param end the end index of the source string (exclusive).
     * NOTE: the range is `[begin, end)`.
     * @return the number of characters matched. MISMATCH otherwise.
     */
    abstract fun match(src: CharSequence, begin: Int, end: Int): Int

    /**
     * Returns a [Pattern] object that sequentially matches the character range against
     * `this` and then `next`. If both succeeds, the entire match length is returned.
     *
     * @param next the next pattern to match.
     * @return the new Pattern object.
     */
    fun next(next: Pattern?): Pattern {
        return SequencePattern(this, next!!)
    }

    /**
     * Returns a [Pattern] object that matches with 0 length even if `this` mismatches.
     */
    fun optional(): Pattern {
        return OptionalPattern(this)
    }

    /**
     * Returns a [Pattern] object that matches this pattern for 0 or more times.
     * The total match length is returned.
     */
    fun many(): Pattern {
        return ManyPattern(this)
    }

    /**
     * Returns [Pattern] object that matches this pattern for at least `min` times.
     * The total match length is returned.
     *
     * @param min the minimal number of times to match.
     * @return the new Pattern object.
     */
    @Deprecated("Use {@link #atLeast} instead.") fun many(min: Int): Pattern {
        return atLeast(min)
    }

    /**
     * Returns [Pattern] object that matches this pattern for at least `min` times.
     * The total match length is returned.
     *
     * @param min the minimal number of times to match.
     * @return the new Pattern object.
     * @since 2.2
     */
    fun atLeast(min: Int): Pattern {
        return LowerBoundedPattern(checkMin(min), this)
    }

    /**
     * Returns a [Pattern] object that matches this pattern for 1 or more times.
     * The total match length is returned.
     */
    fun many1(): Pattern {
        return atLeast(1)
    }

    /**
     * Returns [Pattern] object that matches this pattern for up to `max` times.
     * The total match length is returned.
     *
     * @param max the maximal number of times to match.
     * @return the new Pattern object.
     */
    @Deprecated("Use {@link #atMost} instead.") fun some(max: Int): Pattern {
        return atMost(max)
    }

    /**
     * Returns [Pattern] object that matches this pattern for up to `max` times.
     * The total match length is returned.
     *
     * @param max the maximal number of times to match.
     * @return the new Pattern object.
     * @since 2.2
     */
    fun atMost(max: Int): Pattern {
        return UpperBoundedPattern(checkMax(max), this)
    }

    /**
     * Returns [Pattern] object that matches this pattern for at least `min` times
     * and up to `max` times. The total match length is returned.
     *
     * @param min the minimal number of times to match.
     * @param max the maximal number of times to match.
     * @return the new Pattern object.
     */
    @Deprecated("Use {@link #times(int, int)} instead.")
    fun some(min: Int, max: Int): Pattern {
        return times(min, max)
    }

    /**
     * Returns [Pattern] object that matches this pattern for at least `min` times
     * and up to `max` times. The total match length is returned.
     *
     * @param min the minimal number of times to match.
     * @param max the maximal number of times to match.
     * @return the new Pattern object.
     * @since 2.2
     */
    fun times(min: Int, max: Int): Pattern {
        return times(this, min, max)
    }

    /**
     * Returns a [Pattern] object that only matches if this pattern mismatches, 0 is returned
     * otherwise.
     */
    operator fun not(): Pattern {
        return NotPattern(this)
    }

    /**
     * Returns [Pattern] object that matches with match length 0 if this Pattern object matches.
     */
    fun peek(): Pattern {
        return PeekPattern(this)
    }

    /**
     * Returns [Pattern] object that, if this pattern matches,
     * matches the remaining input against `consequence` pattern, or otherwise matches against
     * `alternative` pattern.
     */
    fun ifelse(consequence: Pattern, alternative: Pattern): Pattern {
        return ifElse(this, consequence, alternative)
    }

    /**
     * Returns [Pattern] object that matches the input against this pattern for `n` times.
     */
    @Deprecated("Use {@link #times(int)} instead.") fun repeat(n: Int): Pattern {
        return times(n)
    }

    /**
     * Returns [Pattern] object that matches the input against this pattern for `n` times.
     * @since 2.2
     */
    operator fun times(n: Int): Pattern {
        return RepeatPattern(checkNonNegative(n, "n < 0"), this)
    }

    /** Returns [Pattern] object that matches if either `this` or `p2` matches.  */
    fun or(p2: Pattern?): Pattern {
        return OrPattern(this, p2!!)
    }

    /**
     * Returns a scanner parser using `this` pattern.
     * Convenient short-hand for [Scanners.pattern].
     *
     * @since 2.2
     */
    // ideally we want to move Pattern/Patterns into the main package. Too late for that.
    fun toScanner(name: String): Parser<Unit> {
        return Scanners.pattern(this, name)
    }

    companion object {
        /** Returned by [.match] method when match fails.  */
        const val MISMATCH = -1
        private fun ifElse(
            cond: Pattern, consequence: Pattern, alternative: Pattern): Pattern {
            return object: Pattern() {
                override fun match(src: CharSequence, begin: Int, end: Int): Int {
                    val conditionResult = cond.match(src, begin, end)
                    return if (conditionResult == MISMATCH) {
                        alternative.match(src, begin, end)
                    } else {
                        val consequenceResult = consequence.match(src, begin + conditionResult, end)
                        if (consequenceResult == MISMATCH) MISMATCH else conditionResult + consequenceResult
                    }
                }
            }
        }

        private fun times(pp: Pattern, min: Int, max: Int): Pattern {
            checkMinMax(min, max)
            return object: Pattern() {
                override fun match(src: CharSequence, begin: Int, end: Int): Int {
                    val minLen = matchRepeat(min, pp, src, end, begin, 0)
                    return if (MISMATCH == minLen) MISMATCH else matchSome(max - min, pp, src, end, begin + minLen, minLen)
                }
            }
        }
    }
}