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
package org.jparsec.functors

import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [Maps].
 *
 * @author Ben Yu
 */
class MapsTest {
    @Test fun testToLowerCase() {
        Assert.assertEquals("foo", Maps.TO_LOWER_CASE.apply("Foo"))
        Assert.assertEquals("toLowerCase", Maps.TO_LOWER_CASE.toString())
    }

    @Test fun testToUpperCase() {
        Assert.assertEquals("FOO", Maps.TO_UPPER_CASE.apply("Foo"))
        Assert.assertEquals("toUpperCase", Maps.TO_UPPER_CASE.toString())
    }

    private enum class MyEnum {
        FOO, BAR
    }

    @Test fun testToEnum() {
        Assert.assertEquals(MyEnum.FOO, Maps.toEnum(MyEnum::class.java).apply("FOO"))
        Assert.assertEquals("-> " + MyEnum::class.java.name, Maps.toEnum(MyEnum::class.java).toString())
    }

    @Test fun testToPair() {
        Assert.assertEquals(Tuples.pair("one", 1), Maps.toPair<Any, Any>().map("one", 1))
    }

    @Test fun testToTuple3() {
        Assert.assertEquals(Tuples.tuple("12", 1, 2), Maps.toTuple3<Any, Any, Any>().map("12", 1, 2))
    }

    @Test fun testToTuple4() {
        Assert.assertEquals(Tuples.tuple("123", 1, 2, 3), Maps.toTuple4<Any, Any, Any, Any>().map("123", 1, 2, 3))
    }

    @Test fun testToTuple5() {
        Assert.assertEquals(Tuples.tuple("1234", 1, 2, 3, 4), Maps.toTuple5<Any, Any, Any, Any, Any>().map("1234", 1, 2, 3, 4))
    }
}