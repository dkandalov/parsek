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

import java.util.function.Function

/**
 * Maps object of type `From` to an object of type `To`.
 *
 * @author Ben Yu
 */
@FunctionalInterface
@Deprecated("""Use {@link java.util.function.Function} instead.""")
fun interface Map<From, To>: Function<From, To> {
    /** Maps `from` to the target object.  */
    fun map(from: From): To
    override fun apply(from: From): To {
        return map(from)
    }
}