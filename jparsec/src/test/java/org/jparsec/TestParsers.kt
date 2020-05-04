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

/**
 * [Parser]s for testing purpose.
 *
 * @author Ben Yu
 */
internal object TestParsers {
    @JvmStatic fun isChar(c: Char): Parser<Char> {
        return Scanners.isChar(c).retn(c)
    }

    @JvmStatic fun areChars(chars: String): Parser<Char?> {
        var parser = Parsers.constant<Char?>(null)
        for (element in chars) {
            parser = parser.next(isChar(element)) as Parser<Char?>
        }
        return parser
    }
}