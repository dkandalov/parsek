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

import org.jparsec.internal.util.Lists.arrayList
import java.util.*

/**
 * Creates a {link List}.
 *
 * @author Ben Yu
 */
internal abstract class ListFactory<T> {
    /** Creates a new list.  */
    abstract fun newList(): ArrayList<T>

    companion object {
        /** Returns a [ListFactory] that creates an empty [ArrayList].  */
        @JvmStatic fun <T> arrayListFactory(): ListFactory<T> {
            return ARRAY_LIST_FACTORY as ListFactory<T>
        }

        /**
         * Returns a [ListFactory] that creates an [ArrayList] instance
         * with `first` as the first element.
         */
        @JvmStatic fun <T> arrayListFactoryWithFirstElement(first: T): ListFactory<T> {
            return object: ListFactory<T>() {
                override fun newList(): ArrayList<T> {
                    val list: ArrayList<T> = arrayList()
                    list.add(first)
                    return list
                }
            }
        }

        private val ARRAY_LIST_FACTORY: ListFactory<*> = object: ListFactory<Any>() {
            override fun newList(): ArrayList<Any> = arrayList()
        }
    }
}