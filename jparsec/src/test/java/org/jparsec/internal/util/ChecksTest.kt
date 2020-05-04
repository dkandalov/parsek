// Copyright 2008 Google Inc. All rights reserved.
package org.jparsec.internal.util

import org.jparsec.internal.util.Checks.checkArgument
import org.jparsec.internal.util.Checks.checkNotNullState
import org.jparsec.internal.util.Checks.checkState
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class ChecksTest {
    @Rule
    @JvmField
    val exception: ExpectedException = ExpectedException.none()

    @Test fun checkArgument_noThrowIfConditionIsTrue() {
        checkArgument(true, "whatever")
        checkArgument(true, "whatever", 1, 2)
        checkArgument(true, "bad format %s and %s", 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkArgument_throwsIfConditionIsFalse() {
        checkArgument(false, "one = %s", 1)
    }

    @Test fun checkState_noThrowIfConditionIsTrue() {
        checkState(true, "whatever")
        checkState(true, "whatever", 1, 2)
        checkState(true, "bad format %s and %s", 1)
    }

    @Test(expected = IllegalStateException::class)
    fun checkState_throwsIfConditionIsFalse() {
        checkState(false, "one = %s", 1)
    }

    @Test fun checkNotNullState_noThrowIfObjectIsntNull() {
        checkNotNullState("1", "whatever")
        checkNotNullState("1", "whatever", 1, 2)
        checkNotNullState("1", "bad format %s and %s", 1)
    }

    @Test(expected = IllegalStateException::class)
    fun checkNotNullState_throwsIfObjectIsNull() {
        checkNotNullState(null, "object = %s", "null")
    }
}