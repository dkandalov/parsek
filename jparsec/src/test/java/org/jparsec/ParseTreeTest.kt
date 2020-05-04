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

import org.junit.Assert
import org.junit.Test
import java.util.*

class ParseTreeTest {
    @Test fun noChildren() {
        Assert.assertEquals("foo: 123", tree("foo", 123).toString())
    }

    @Test fun oneChild() {
        Assert.assertEquals("foo: {\nbar: x\n}", tree("foo", 123, tree("bar", "x")).toString())
    }

    @Test fun oneChildren() {
        Assert.assertEquals("foo: {\nbar: x,\nbaz: y\n}",
                            tree("foo", 123, tree("bar", "x"), tree("baz", "y")).toString())
    }

    companion object {
        private fun tree(name: String, value: Any, vararg children: ParseTree): ParseTree {
            return ParseTree(name, 0, 1000, value, Arrays.asList(*children))
        }
    }
}