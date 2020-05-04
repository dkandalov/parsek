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
package org.jparsec.easymock

import org.easymock.EasyMock
import org.easymock.IMocksControl
import org.junit.After
import org.junit.Before
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.util.*

/**
 * Provides convenient API for using EasyMock.
 *
 * @author Ben Yu
 */
abstract class BaseMockTest {
    /** Annotates a field as being mocked.  */
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    protected annotation class Mock

    private var control: IMocksControl? = null
    private var replayed = false
    @Before @Throws(Exception::class) fun setUp() {
        replayed = false
        control = EasyMock.createControl()
        for (field in getMockFields(javaClass)) {
            field[this] = mock(field.type)
        }
    }

    @After @Throws(Exception::class) fun tearDown() {
        if (replayed) {
            control!!.verify()
        }
    }

    /** Returns a mock of `type`.  */
    protected fun <T> mock(type: Class<T>?): T {
        return control!!.createMock(type)
    }

    protected fun replay() {
        control!!.replay()
        replayed = true
    }

    companion object {
        private fun getMockFields(type: Class<*>): List<Field> {
            var fields: MutableList<Field> = ArrayList()
            for (field in type.declaredFields) {
                if (field.isAnnotationPresent(Mock::class.java)) {
                    fields.add(field)
                }
            }
            AccessibleObject.setAccessible(fields.toTypedArray(), true)
            val superclass = type.superclass
            if (superclass != null) {
                fields.addAll(getMockFields(superclass))
            }
            fields = Collections.unmodifiableList(fields)
            return fields
        }
    }
}