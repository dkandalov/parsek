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

import org.jparsec.util.ObjectTester.assertEqual
import org.jparsec.util.ObjectTester.assertNotEqual
import org.junit.Assert.assertEquals
import org.junit.Test

class TokenTest {
    @Test fun testLength() {
        assertEquals(1, Token(0, 1, null).length().toLong())
    }

    @Test fun testIndex() {
        assertEquals(1, Token(1, 2, null).index().toLong())
    }

    @Test fun testValue() {
        assertEquals("value", Token(1, 2, "value").value())
    }

    @Test fun testToString() {
        assertEquals("value", Token(1, 2, "value").toString())
        assertEquals("null", Token(1, 2, null).toString())
    }

    @Test fun testEquals() {
        assertEqual(Token(1, 2, "value"), Token(1, 2, "value"))
        assertEqual(Token(1, 2, null), Token(1, 2, null))
        assertNotEqual(Token(1, 2, "value"),
                       Token(2, 2, "value"), Token(1, 3, "value"),
                       Token(1, 2, "value2"), Token(1, 2, null))
    }
}