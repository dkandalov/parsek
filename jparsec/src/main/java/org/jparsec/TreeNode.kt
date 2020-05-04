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

import org.jparsec.internal.util.Checks.checkArgument
import org.jparsec.internal.util.Checks.checkState
import java.util.*

/**
 * A TreeNode remembers it's parent (which corresponds to a parent parser that syntactically
 * encloses this parter), it's previous node (which is the parser at the same syntactical level
 * and had just *succeeded* before this parser started). It also keeps all the children.
 *
 *
 * Once constructed, a node's 'parent' and 'previous' references are immutable.
 * The list of children nodes however can change. When the alternative parsers
 * in an `or` parser are attempted one after another, they each generate new child node of
 * the parent node. These "alternative" nodes all point to the same parent and same "previous" node.
 *
 *
 * When exception is to be thrown, the most relevant error is picked, along with the tree node
 * that was recorded at time of [ParseContext.raise]. That tree node is then
 * [frozen][.freeze] by setting its parent's [.latestChild] to this error
 * node's "previous" successful node, and that of its grandparent's to its parent node, all the way
 * up to the root. This essentially freezes and collapse the "multi universes" into a single error
 * state, with all other "potential" error state destroyed and forgotten.
 */
class TreeNode {
    private val name: String
    private val beginIndex: Int
    private val parent: TreeNode?
    private val previous: TreeNode?
    private var endIndex = 0
    private var result: Any? = null
    @JvmField
    var latestChild: TreeNode? = null

    constructor(name: String, beginIndex: Int) {
        this.name = name
        this.beginIndex = beginIndex
        parent = null
        previous = null
    }

    constructor(name: String, beginIndex: Int, parent: TreeNode?, previous: TreeNode?) {
        this.name = name
        this.beginIndex = beginIndex
        this.parent = parent
        this.previous = previous
    }

    fun setEndIndex(index: Int) {
        checkArgument(index >= beginIndex, "endIndex < beginIndex")
        endIndex = index
    }

    fun setResult(result: Any?) {
        this.result = result
    }

    fun parent(): TreeNode? {
        checkState(parent != null, "Root node has no parent")
        return parent
    }

    fun addChild(childName: String, childIndex: Int): TreeNode {
        val child = TreeNode(childName, childIndex, this, latestChild)
        latestChild = child
        return child
    }

    /**
     * When this leaf node has errors, it didn't complete and shouldn't be part of the parse tree
     * that is the current partial parse result with all successful matches.
     * In that case, return the parent node, by setting its [.latestChild] to [.previous].
     */
    fun orphanize(): TreeNode {
        if (parent == null) {
            // Root node is provided free, without an explicit asNode() call.
            // So there isn't a partially completed node.
            return this
        }
        parent.latestChild = previous
        return parent
    }

    /**
     * Freezes the current tree node to make it the latest child of its parent
     * (discarding nodes that have been tacked on after it in the same hierarchy level); and
     * recursively apply to all of its ancestors.
     *
     *
     * This is because it's only called at time of error. If an ancestor node has a child node that
     * was added during the process of trying other alternatives and then failed, those paths don't
     * matter. So we should restore the tree back to when this most relevant error happened.
     *
     *
     * Returns the root node, which can then be used to [.toParseTree].
     */
    fun freeze(index: Int): TreeNode? {
        var node: TreeNode? = this
        node!!.setEndIndex(index)
        while (node!!.parent != null) {
            node.parent!!.latestChild = node
            node = node.parent
            node!!.setEndIndex(index)
        }
        return node
    }

    /** Converts this node into a [ParseTree] representation.  */
    fun toParseTree(): ParseTree {
        val children: MutableList<ParseTree?> = ArrayList()
        var child = latestChild
        while (child != null) {
            children.add(child.toParseTree())
            child = child.previous
        }
        Collections.reverse(children)
        return ParseTree(name, beginIndex, endIndex, result, children)
    }

    override fun toString(): String {
        return name
    }
}