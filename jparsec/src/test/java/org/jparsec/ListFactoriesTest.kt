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

/**
 * Unit test for [ListFactories].
 *
 * @author Ben Yu
 */
class ListFactoriesTest {
    @Test fun testArrayListFactory() {
        val intListFactory = ListFactory.arrayListFactory<Int>()
        val stringListFactory = ListFactory.arrayListFactory<String>()
        val intList = intListFactory.newList() as ArrayList<Int>
        val stringList = stringListFactory.newList() as ArrayList<String>
        Assert.assertNotSame(intList, stringList)
        Assert.assertEquals(0, intList.size.toLong())
        Assert.assertEquals(0, stringList.size.toLong())
    }

    @Test fun testArrayListFactoryWithFirstElement() {
        val intListFactory = ListFactory.arrayListFactoryWithFirstElement(1)
        val list = intListFactory.newList() as ArrayList<Int>
        Assert.assertEquals(Arrays.asList(1), list)
    }
}