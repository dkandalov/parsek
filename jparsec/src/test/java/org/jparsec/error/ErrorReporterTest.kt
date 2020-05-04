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
package org.jparsec.error

import org.easymock.EasyMock
import org.jparsec.easymock.BaseMockTest
import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * Unit test for [ErrorReporter].
 *
 * @author Ben Yu
 */
class ErrorReporterTest: BaseMockTest() {
    @Mock
    var error: ParseErrorDetails? = null
    @Test fun testToString_null() {
        Assert.assertEquals("", ErrorReporter.toString(null, null))
    }

    @Test fun testToString_nullError() {
        Assert.assertEquals("line 3, column 5", ErrorReporter.toString(null, Location(3, 5)))
    }

    @Test fun testToString_failure() {
        EasyMock.expect(error!!.failureMessage).andReturn("failure").atLeastOnce()
        replay()
        Assert.assertEquals("line 3, column 5:\nfailure", ErrorReporter.toString(error, Location(3, 5)))
    }

    @Test fun testToString_expected() {
        EasyMock.expect(error!!.failureMessage).andReturn(null).atLeastOnce()
        EasyMock.expect(error!!.expected).andReturn(Arrays.asList("foo", "bar")).atLeastOnce()
        EasyMock.expect(error!!.encountered).andReturn("baz")
        replay()
        Assert.assertEquals("line 3, column 5:\nfoo or bar expected, baz encountered.", ErrorReporter.toString(error, Location(3, 5)))
    }

    @Test fun testToString_unexpected() {
        EasyMock.expect(error!!.failureMessage).andReturn(null).atLeastOnce()
        EasyMock.expect(error!!.expected).andReturn(Arrays.asList())
        EasyMock.expect(error!!.unexpected).andReturn("foo").atLeastOnce()
        replay()
        Assert.assertEquals("line 3, column 5:\nunexpected foo.", ErrorReporter.toString(error, Location(3, 5)))
    }

    @Test fun testReportList() {
        Assert.assertEquals("", reportList())
        Assert.assertEquals("foo", reportList("foo"))
        Assert.assertEquals("foo or bar", reportList("foo", "bar"))
        Assert.assertEquals("foo, bar or baz", reportList("foo", "bar", "baz"))
        Assert.assertEquals("foo, bar or baz", reportList("foo", "bar", "baz", "baz"))
        Assert.assertEquals("foo or bar", reportList("foo", "foo", "bar"))
    }

    companion object {
        private fun reportList(vararg strings: String): String {
            val builder = StringBuilder()
            ErrorReporter.reportList(builder, Arrays.asList(*strings))
            return builder.toString()
        }
    }
}