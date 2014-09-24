package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.Application;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class LinkVisitCheckerTest {

    @Test
    public void testVisitCaller() throws Exception {
        LinkVisitChecker checker = new LinkVisitChecker();

        Application testApplication = new Application("test", ServiceType.TOMCAT);
        Assert.assertFalse(checker.visitCaller(testApplication));
        Assert.assertTrue(checker.visitCaller(testApplication));

        Application newApp = new Application("newApp", ServiceType.TOMCAT);
        Assert.assertFalse(checker.visitCaller(newApp));
        Assert.assertTrue(checker.visitCaller(newApp));
    }

    @Test
    public void testVisitCallee() throws Exception {
        LinkVisitChecker checker = new LinkVisitChecker();

        Application testApplication = new Application("test", ServiceType.TOMCAT);
        Assert.assertFalse(checker.visitCallee(testApplication));
        Assert.assertTrue(checker.visitCallee(testApplication));

        Application newApp = new Application("newApp", ServiceType.TOMCAT);
        Assert.assertFalse(checker.visitCallee(newApp));
        Assert.assertTrue(checker.visitCallee(newApp));
    }
}
