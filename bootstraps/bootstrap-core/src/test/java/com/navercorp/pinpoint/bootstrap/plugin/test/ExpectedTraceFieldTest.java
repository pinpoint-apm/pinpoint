/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.test;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class ExpectedTraceFieldTest {

    private final String message = "message";
    private final String message1 = "message1";
    private final String message2 = "message2";
    private final String failMessage = "fail";
    private final String emptyMessage = "";
    private final String nullMessage = null;

    @Test
    public void alwaysTrueTest() {
        ExpectedTraceField expectedTraceField = ExpectedTraceField.create(null);
        assertField(expectedTraceField, true, true, true, true, true, true);
    }

    @Test
    public void alwaysTrueTest2() {
        ExpectedTraceField expectedTraceField = ExpectedTraceField.createAlwaysTrue();
        assertField(expectedTraceField, true, true, true, true, true, true);
    }

    @Test
    public void equalsTest() {
        ExpectedTraceField expectedTraceField = ExpectedTraceField.create(message);
        assertField(expectedTraceField, true, false, false, false, false, false);
    }

    @Test
    public void equalsTest2() {
        ExpectedTraceField expectedTraceField = ExpectedTraceField.createEquals(message);
        assertField(expectedTraceField, true, false, false, false, false, false);
    }

    @Test
    public void containsTest() {
        ExpectedTraceField expectedTraceField = ExpectedTraceField.createContains(message);
        assertField(expectedTraceField, true, true, true, false, false, false);
    }

    @Test
    public void emptyTest() {
        ExpectedTraceField expectedTraceField = ExpectedTraceField.createEmpty();
        assertField(expectedTraceField, false, false, false, false, true, true);
    }

    @Test
    public void notEmptyTest() {
        ExpectedTraceField expectedTraceField = ExpectedTraceField.createNotEmpty();
        assertField(expectedTraceField, true, true, true, true, false, false);
    }

    @Test
    public void startWithTest() {
        ExpectedTraceField expectedTraceField = ExpectedTraceField.createStartWith(message);
        assertField(expectedTraceField, true, true, true, false, false, false);
    }


    private void assertField(ExpectedTraceField expectedTraceField, boolean boolean1, boolean boolean2, boolean boolean3, boolean boolean4, boolean boolean5, boolean boolean6) {
        Assert.assertEquals(boolean1, expectedTraceField.isEquals(message));
        Assert.assertEquals(boolean2, expectedTraceField.isEquals(message1));
        Assert.assertEquals(boolean3, expectedTraceField.isEquals(message2));
        Assert.assertEquals(boolean4, expectedTraceField.isEquals(failMessage));
        Assert.assertEquals(boolean5, expectedTraceField.isEquals(emptyMessage));
        Assert.assertEquals(boolean6, expectedTraceField.isEquals(nullMessage));
    }

}
