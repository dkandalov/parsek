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

/**
 * Provides common [CharPredicate] implementations.
 *
 * @author Ben Yu
 */
object CharPredicates {
    /** A [CharPredicate] that always returns false.  */
    @JvmField
    val NEVER: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return false
        }

        override fun toString(): String {
            return "none"
        }
    }

    /** A [CharPredicate] that always returns true.  */
    @JvmField
    val ALWAYS: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return true
        }

        override fun toString(): String {
            return "any character"
        }
    }

    /**
     * A [CharPredicate] that returns true if the character is a digit or within the range
     * of `[a-f]` or `[A-F]`.
     */
    @JvmField
    val IS_HEX_DIGIT: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F'
        }

        override fun toString(): String {
            return "[0-9a-fA-F]"
        }
    }

    /**
     * A [CharPredicate] that returns true if [Character.isUpperCase] returns
     * true.
     */
    val IS_UPPER_CASE: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return Character.isUpperCase(c)
        }

        override fun toString(): String {
            return "uppercase"
        }
    }

    /**
     * A [CharPredicate] that returns true if [Character.isLowerCase] returns
     * true.
     */
    val IS_LOWER_CASE: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return Character.isLowerCase(c)
        }

        override fun toString(): String {
            return "lowercase"
        }
    }

    /**
     * A [CharPredicate] that returns true if [Character.isWhitespace]
     * returns true.
     */
    @JvmField
    val IS_WHITESPACE: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return Character.isWhitespace(c)
        }

        override fun toString(): String {
            return "whitespace"
        }
    }

    /** A [CharPredicate] that returns true if the character is an alpha character.  */
    @JvmField
    val IS_ALPHA: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return c <= 'z' && c >= 'a' || c <= 'Z' && c >= 'A'
        }

        override fun toString(): String {
            return "[a-zA-Z]"
        }
    }

    /**
     * A [CharPredicate] that returns true if it is an alpha character or the underscore
     * character `_`.
     */
    @JvmField
    val IS_ALPHA_: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return c == '_' || c <= 'z' && c >= 'a' || c <= 'Z' && c >= 'A'
        }

        override fun toString(): String {
            return "[a-zA-Z_]"
        }
    }

    /**
     * A [CharPredicate] that returns true if [Character.isLetter] returns
     * true.
     */
    val IS_LETTER: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return Character.isLetter(c)
        }

        override fun toString(): String {
            return "letter"
        }
    }

    /**
     * A [CharPredicate] that returns true if it is an alphanumeric character, or an
     * underscore character.
     */
    val IS_ALPHA_NUMERIC: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9'
        }

        override fun toString(): String {
            return "[0-9a-zA-Z]"
        }
    }

    /**
     * A [CharPredicate] that returns true if it is an alphanumeric character, or an
     * underscore character.
     */
    @JvmField
    val IS_ALPHA_NUMERIC_: CharPredicate = object: CharPredicate {
        override fun isChar(c: Char): Boolean {
            return c == '_' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9'
        }

        override fun toString(): String {
            return "[0-9a-zA-Z_]"
        }
    }

    /** A [CharPredicate] that returns true if the character is equal to `c`.  */
    @JvmStatic fun isChar(c: Char): CharPredicate {
        return object: CharPredicate {
            override fun isChar(x: Char): Boolean {
                return x == c
            }

            override fun toString(): String {
                return Character.toString(c)
            }
        }
    }

    /** A [CharPredicate] that returns true if the character is not equal to `c`.  */
    @JvmStatic fun notChar(c: Char): CharPredicate {
        return object: CharPredicate {
            override fun isChar(x: Char): Boolean {
                return x != c
            }

            override fun toString(): String {
                return "^" + Character.toString(c)
            }
        }
    }

    /**
     * A [CharPredicate] that returns true if the character is within the range of
     * `[a, b]`.
     */
    @JvmStatic fun range(a: Char, b: Char): CharPredicate {
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                return c >= a && c <= b
            }

            override fun toString(): String {
                return "[$a-$b]"
            }
        }
    }

    /** A [CharPredicate] that returns true if the character is a digit.  */
    @JvmField
    val IS_DIGIT = range('0', '9')

    /**
     * A [CharPredicate] that returns true if the character is not within the range of
     * `[a, b]`.
     */
    fun notRange(a: Char, b: Char): CharPredicate {
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                return !(c >= a && c <= b)
            }

            override fun toString(): String {
                return "[^$a-$b]"
            }
        }
    }

    /**
     * A [CharPredicate] that returns true if the character is equal to any character in
     * `chars`.
     */
    @JvmStatic fun among(chars: String): CharPredicate {
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                return chars.indexOf(c) >= 0
            }

            override fun toString(): String {
                return "[$chars]"
            }
        }
    }

    /**
     * A [CharPredicate] that returns true if the character is not equal to any character
     * in `chars`.
     */
    @JvmStatic fun notAmong(chars: String): CharPredicate {
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                return chars.indexOf(c) < 0
            }

            override fun toString(): String {
                return "^[$chars]"
            }
        }
    }

    /** A [CharPredicate] that returns true if `predicate` evaluates to false.  */
    fun not(predicate: CharPredicate): CharPredicate {
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                return !predicate.isChar(c)
            }

            override fun toString(): String {
                return "^$predicate"
            }
        }
    }

    /**
     * A [CharPredicate] that returns true if both `predicate1` and
     * `predicate2` evaluates to true.
     */
    fun and(predicate1: CharPredicate, predicate2: CharPredicate): CharPredicate {
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                return predicate1.isChar(c) && predicate2.isChar(c)
            }

            override fun toString(): String {
                return "$predicate1 and $predicate2"
            }
        }
    }

    /**
     * A [CharPredicate] that returns true if either `predicate1` or
     * `predicate2` evaluates to true.
     */
    fun or(predicate1: CharPredicate, predicate2: CharPredicate): CharPredicate {
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                return predicate1.isChar(c) || predicate2.isChar(c)
            }

            override fun toString(): String {
                return "$predicate1 or $predicate2"
            }
        }
    }

    /**
     * A [CharPredicate] that returns true if all `CharPredicate` in
     * `predicates` evaluate to true.
     */
    fun and(vararg predicates: CharPredicate): CharPredicate {
        if (predicates.isEmpty()) return ALWAYS else if (predicates.size == 1) return predicates[0]
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                for (element in predicates) {
                    if (!element!!.isChar(c)) return false
                }
                return true
            }

            override fun toString(): String {
                return predicates.joinToString(" and ")
            }
        }
    }

    /**
     * A [CharPredicate] that returns true if any `CharPredicate` in
     * `predicates` evaluates to true.
     */
    fun or(vararg predicates: CharPredicate): CharPredicate {
        if (predicates.isEmpty()) return NEVER else if (predicates.size == 1) return predicates[0]
        return object: CharPredicate {
            override fun isChar(c: Char): Boolean {
                for (element in predicates) {
                    if (element!!.isChar(c)) return true
                }
                return false
            }

            override fun toString(): String {
                return predicates.joinToString(" or ")
            }
        }
    }
}