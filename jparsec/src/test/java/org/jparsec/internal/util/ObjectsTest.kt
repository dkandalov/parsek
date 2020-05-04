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
package org.jparsec.internal.util

import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [Objects].
 *
 * @author Ben Yu
 */
class ObjectsTest {
    @Test fun testEquals() {
        Assert.assertTrue(Objects.equals(null, null))
        Assert.assertFalse(Objects.equals(null, ""))
        Assert.assertFalse(Objects.equals("", null))
        Assert.assertTrue(Objects.equals("", ""))
    }

    @Test fun testHashCode() {
        Assert.assertEquals(0, Objects.hashCode(null).toLong())
        Assert.assertEquals("".hashCode().toLong(), Objects.hashCode("").toLong())
    }

    @Test fun testIn() {
        Assert.assertTrue(Objects.`in`("b", "a", "b", "c"))
        Assert.assertFalse(Objects.`in`("x", "a", "b", "c"))
    }
}