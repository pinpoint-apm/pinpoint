/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.service.LinkVisitChecker;
import com.navercorp.pinpoint.web.vo.Application;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class LinkVisitCheckerTest {

    @Test
    public void testVisitCaller() throws Exception {
        LinkVisitChecker checker = new LinkVisitChecker();

        Application testApplication = new Application("test", ServiceType.STAND_ALONE);
        Assert.assertFalse(checker.visitCaller(testApplication));
        Assert.assertTrue(checker.visitCaller(testApplication));

        Application newApp = new Application("newApp", ServiceType.STAND_ALONE);
        Assert.assertFalse(checker.visitCaller(newApp));
        Assert.assertTrue(checker.visitCaller(newApp));
    }

    @Test
    public void testVisitCallee() throws Exception {
        LinkVisitChecker checker = new LinkVisitChecker();

        Application testApplication = new Application("test", ServiceType.STAND_ALONE);
        Assert.assertFalse(checker.visitCallee(testApplication));
        Assert.assertTrue(checker.visitCallee(testApplication));

        Application newApp = new Application("newApp", ServiceType.STAND_ALONE);
        Assert.assertFalse(checker.visitCallee(newApp));
        Assert.assertTrue(checker.visitCallee(newApp));
    }
}
