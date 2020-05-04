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

import org.jparsec.error.ParseErrorDetails
import org.jparsec.internal.annotations.Private
import org.jparsec.internal.util.Checks.checkState
import org.jparsec.internal.util.Lists.arrayList

/**
 * Represents the context state during parsing.
 *
 * @author Ben Yu
 */
abstract class ParseContext(
    @JvmField val source: CharSequence,
    /** The current parse result.  */
    @JvmField var result: Any?,
    /** The current position of the input. Points to the token array for token level.  */
    @JvmField var at: Int,
    val module: String?,
    val locator: SourceLocator
) {

    /** The current logical step.  */
    @JvmField
    var step = 0

    var trace: ParserTrace = object: ParserTrace {
        override fun push(name: String?) {}
        override fun pop() {}
        override val currentNode: TreeNode?
            get() = null

        override fun setCurrentResult(result: Any?) {}
        override var latestChild: TreeNode?
            get() = null
            set(node) {}

        override fun startFresh(context: ParseContext) {}
        override fun setStateAs(that: ParserTrace) {}
    }
        private set

    enum class ErrorType(val mergeable: Boolean) {
        /** Default value, no error.  */
        NONE(false),

        /** When the error is mostly lenient (as a delimiter of repetitions for example).  */
        DELIMITING(false),

        /** When [Parser.not] is called. Signals that something isn't expected.  */
        UNEXPECTED(false),

        /** When any expected input isn't found.  */
        MISSING(true),

        /** When [Parser.label] is called. Signals that a logical stuff isn't found.  */
        EXPECTING(true),

        /** When [Parsers.fail] is called. Signals a serious problem.  */
        FAILURE(false);

    }

    private var currentErrorType = ErrorType.NONE
    private var currentErrorAt: Int
    private var currentErrorIndex = 0 // TODO: is it necessary to set this to the starting index?
    private val errors = arrayList<Any>(32)
    private var encountered: String? = null // for explicitly setting encountered token into ScannerState.
    private var currentErrorNode: TreeNode? = null

    // explicit suppresses error recording if true.
    private var errorSuppressed = false
    private var overrideErrorType = ErrorType.NONE

    //caller should not change input after it is passed in.
    constructor(source: CharSequence, at: Int, module: String?, locator: SourceLocator): this(source, null, at, module, locator) {}

    /** Runs `parser` with error recording suppressed.  */
    fun withErrorSuppressed(parser: Parser<*>): Boolean {
        val oldValue = errorSuppressed
        errorSuppressed = true
        val ok = parser.apply(this)
        errorSuppressed = oldValue
        return ok
    }

    /** Runs `parser` with error recording suppressed.  */
    fun applyAsDelimiter(parser: Parser<*>): Boolean {
        val oldValue = overrideErrorType
        overrideErrorType = ErrorType.DELIMITING
        val oldStep = step
        val ok = parser.apply(this)
        if (ok) step = oldStep
        overrideErrorType = oldValue
        return ok
    }

    /**
     * Applies `parser` as a new tree node with `name`, and if fails, reports
     * "expecting $name".
     */
    fun applyNewNode(parser: Parser<*>, name: String): Boolean {
        val physical = at
        val logical = step
        val latestChild = trace.latestChild
        trace.push(name)
        if (parser.apply(this)) {
            trace.setCurrentResult(result)
            trace.pop()
            return true
        }
        if (stillThere(physical, logical)) expected(name)
        trace.pop()
        // On failure, the erroneous path shouldn't be counted in the parse tree.
        trace.latestChild = latestChild
        return false
    }

    fun applyNested(parser: Parser<*>, nestedState: ParseContext): Boolean {
        // nested is either the token-level parser, or the inner scanner of a subpattern.
        return try {
            if (parser.apply(nestedState)) {
                set(nestedState.step, at, nestedState.result)
                return true
            }
            // index on token level is the "at" on character level
            set(step, nestedState.index, null)

            // always copy error because there could be false alarms in the character level.
            // For example, a "or" parser nested in a "many" failed in one of its branches.
            copyErrorFrom(nestedState)
            false
        } finally {
            trace.setStateAs(nestedState.trace)
        }
    }

    fun repeat(parser: Parser<*>, n: Int): Boolean {
        for (i in 0 until n) {
            if (!parser.apply(this)) return false
        }
        return true
    }

    fun <T> repeat(
        parser: Parser<out T>, n: Int, collection: MutableCollection<T>): Boolean {
        for (i in 0 until n) {
            if (!parser.apply(this)) return false
            collection.add(parser.getReturn(this))
        }
        return true
    }

    /** The physical index of the current most relevant error, `0` if none.  */
    fun errorIndex(): Int {
        return currentErrorIndex
    }

    fun buildParseTree(): ParseTree? {
        val currentNode = trace.currentNode ?: return null
        return currentNode.freeze(index)!!.toParseTree()
    }

    fun buildErrorParseTree(): ParseTree? {
        // The current node is partially done because there was an error.
        // So orphanize it. But at the same time, all ancestor nodes should have their endIndex set to
        // where we are now.
        return if (currentErrorNode == null) null else currentErrorNode!!.orphanize().freeze(index)!!.toParseTree()
    }

    /** Only called when rendering the error in [ParserException].  */
    fun renderError(): ParseErrorDetails {
        val errorIndex = toIndex(currentErrorAt)
        val encounteredName = getEncountered()
        val errorStrings = arrayList<String>(errors.size)
        for (error in errors) {
            errorStrings.add(error.toString())
        }
        return when (currentErrorType) {
            ErrorType.UNEXPECTED -> object: EmptyParseError(errorIndex, encounteredName) {
                override val unexpected: String
                    get() = errorStrings[0]
            }
            ErrorType.FAILURE -> object: EmptyParseError(errorIndex, encounteredName) {
                override val failureMessage: String
                    get() = errorStrings[0]
            }
            ErrorType.EXPECTING, ErrorType.MISSING, ErrorType.DELIMITING -> object: EmptyParseError(errorIndex, encounteredName) {
                override val expected: List<String>
                    get() = errorStrings
            }
            else -> EmptyParseError(errorIndex, encounteredName)
        }
    }

    private fun getEncountered(): String {
        return if (encountered != null) {
            encountered!!
        } else getInputName(currentErrorAt)
    }

    /** Returns the string representation of the current input (character or token).  */
    abstract fun getInputName(pos: Int): String
    abstract val isEof: Boolean

    /** Returns the current index in the original source.  */
    val index: Int
        get() = toIndex(at)

    /** Returns the current token. Only applicable to token level parser.  */
    abstract val token: Token

    /** Peeks the current character. Only applicable to character level parser.  */
    abstract fun peekChar(): Char

    /** Translates the logical position to physical index in the original source.  */
    abstract fun toIndex(pos: Int): Int
    @Private fun raise(type: ErrorType, subject: Any) {
        var type = type
        if (errorSuppressed) return
        if (at < currentErrorAt) return
        if (overrideErrorType != ErrorType.NONE) type = overrideErrorType
        if (at > currentErrorAt) {
            setErrorState(at, index, type)
            errors.add(subject)
            return
        }
        // now error location is same
        if (type.ordinal < currentErrorType.ordinal) {
            return
        }
        if (type.ordinal > currentErrorType.ordinal) {
            setErrorState(at, index, type)
            errors.add(subject)
            return
        }
        // now even error type is same
        if (type.mergeable) {
            // merge expected error.
            errors.add(subject)
        }
    }

    fun fail(message: String) {
        raise(ErrorType.FAILURE, message)
    }

    fun missing(what: Any) {
        raise(ErrorType.MISSING, what)
    }

    fun expected(what: Any) {
        raise(ErrorType.EXPECTING, what)
    }

    fun unexpected(what: String) {
        raise(ErrorType.UNEXPECTED, what)
    }

    fun stillThere(wasAt: Int, originalStep: Int): Boolean {
        if (step == originalStep) {
            // logical step didn't change, so logically we are still there, undo any physical offset
            setAt(originalStep, wasAt)
            return true
        }
        return false
    }

    operator fun set(step: Int, at: Int, ret: Any?) {
        this.step = step
        this.at = at
        result = ret
    }

    fun setAt(step: Int, at: Int) {
        this.step = step
        this.at = at
    }

    operator fun next() {
        at++
        step++
    }

    fun next(n: Int) {
        at += n
        if (n > 0) step++
    }

    /** Enables parse tree tracing with `rootName` as the name of the root node.  */
    fun enableTrace(rootName: String?) {
        trace = object: ParserTrace {
            override var currentNode: TreeNode? = TreeNode(rootName!!, index)
                private set

            override fun push(name: String?) {
                currentNode = currentNode!!.addChild(name!!, index)
            }

            override fun pop() {
                currentNode!!.setEndIndex(index)
                currentNode = currentNode!!.parent()
            }

            override fun setCurrentResult(result: Any?) {
                currentNode!!.setResult(result)
            }

            override var latestChild: TreeNode?
                get() = currentNode!!.latestChild
                set(latest) {
                    checkState(latest == null || latest.parent() == currentNode,
                               "Trying to set a child node not owned by the parent node")
                    currentNode!!.latestChild = latest
                }

            override fun startFresh(context: ParseContext) {
                context.enableTrace(rootName)
            }

            override fun setStateAs(that: ParserTrace) {
                currentNode = that.currentNode
            }
        }
    }

    /** Allows tracing of parsing progress during error condition, to ease debugging.  */
    interface ParserTrace {
        /**
         * Upon applying a parser with [Parser.label], the label name is used to create a new
         * child node under the current node. The new child node is set to be the current node.
         */
        fun push(name: String?)

        /** When a parser finishes, the current node is popped so we are back to the parent parser.  */
        fun pop()

        /** Returns the current node, that is being parsed (not necessarily finished).  */
        val currentNode: TreeNode?

        /** Whenever a labeled parser succeeds, it calls this method to set its result in the trace.  */
        fun setCurrentResult(result: Any?)

        /**
         * Called by branching parsers, to save the current state of tree, before trying parsers that
         * could modify the tree state.
         */
        /**
         * Called by labeled parser to reset the current child node when the current node failed.
         * Also called by [BestParser] to set the optimum parse tree.
         */
        var latestChild: TreeNode?

        /** Called when tokenizer passes on to token-level parser.  */
        fun startFresh(context: ParseContext)

        /**
         * Set the enclosing parser's tree state into the nested parser's state. Called for both nested
         * token-level parser and nested scanner.
         */
        fun setStateAs(that: ParserTrace)
    }

    private fun setErrorState(
        errorAt: Int, errorIndex: Int, errorType: ErrorType, errors: List<Any>) {
        setErrorState(errorAt, errorIndex, errorType)
        this.errors.addAll(errors)
    }

    private fun setErrorState(errorAt: Int, errorIndex: Int, errorType: ErrorType) {
        currentErrorIndex = errorIndex
        currentErrorAt = errorAt
        currentErrorType = errorType
        currentErrorNode = trace.currentNode
        encountered = null
        errors.clear()
    }

    private fun copyErrorFrom(that: ParseContext) {
        val errorIndex = that.errorIndex()
        setErrorState(errorIndex, errorIndex, that.currentErrorType, that.errors)
        if (!that.isEof) {
            encountered = that.getEncountered()
        }
        currentErrorNode = that.currentErrorNode
    }

    /** Reads the characters as input. Only applicable to character level parsers.  */
    abstract fun characters(): CharSequence

    override fun toString(): String {
        return source.subSequence(index, source.length).toString()
    }

    companion object {
        const val EOF = "EOF"
    }

    init {
        currentErrorAt = at
    }
}