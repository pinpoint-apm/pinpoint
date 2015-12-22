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

package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.ClassPathResolver;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author emeroad
 */
@Ignore
public class ClassPathResolverTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testFindAgentJar() throws Exception {
        String path = "D:\\nhn_source\\pinpoint_project\\deploy\\apache-tomcat-6.0.35\\bin\\bootstrap.jar;D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib/lib/javassist-3.16.1.GA.jar;" +
                "D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib/lib/pinpoint-commons-0.0.2.jar;;D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib/pinpoint-bootstrap-0.0.2.jar";
        // D:\nhn_source\pinpoint_project\deploy\agent\agentlib/pinpoint-tomcat-profiler-0.0.2.jar
        ClassPathResolver classPathResolver = new ClassPathResolver(path);
        boolean findAgentJar = classPathResolver.findAgentJar();
        Assert.assertTrue(findAgentJar);

        String agentJar = classPathResolver.getAgentJarName();
        Assert.assertEquals("pinpoint-bootstrap-0.0.2.jar", agentJar);

        String agentPath = classPathResolver.getAgentJarFullPath();
        Assert.assertEquals("D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib/pinpoint-bootstrap-0.0.2.jar", agentPath);

        String agentDirPath = classPathResolver.getAgentDirPath();
        Assert.assertEquals("D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib", agentDirPath );

        String agentLibPath = classPathResolver.getAgentLibPath();
        Assert.assertEquals("D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib"+File.separator+ "lib", agentLibPath);
    }

    @Test
    public void testFindAgentSnapshotJar() throws Exception {
        String path = "D:\\nhn_source\\pinpoint_project\\deploy\\apache-tomcat-6.0.35\\bin\\bootstrap.jar;D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib/lib/javassist-3.16.1.GA.jar;" +
                "D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib/lib/pinpoint-commons-0.0.2.jar;;D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib" +
                "/pinpoint-bootstrap-0.0.2-SNAPSHOT.jar";
        // D:\nhn_source\pinpoint_project\deploy\agent\agentlib/pinpoint-tomcat-profiler-0.0.2.jar
        ClassPathResolver classPathResolver = new ClassPathResolver(path);
        boolean findAgentJar = classPathResolver.findAgentJar();
        Assert.assertTrue(findAgentJar);

        String agentJar = classPathResolver.getAgentJarName();
        Assert.assertEquals("pinpoint-bootstrap-0.0.2-SNAPSHOT.jar", agentJar);

        String agentPath = classPathResolver.getAgentJarFullPath();
        Assert.assertEquals("D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib/pinpoint-bootstrap-0.0.2-SNAPSHOT.jar", agentPath);

        String agentDirPath = classPathResolver.getAgentDirPath();
        Assert.assertEquals("D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib", agentDirPath );

        String agentLibPath = classPathResolver.getAgentLibPath();
        Assert.assertEquals("D:\\nhn_source\\pinpoint_project\\deploy\\agent\\agentlib"+File.separator+ "lib", agentLibPath);
    }

    @Test
    public void findAgentJar() {
        findAgentJar("pinpoint-bootstrap-0.0.2.jar");
        findAgentJar("pinpoint-bootstrap-1.0.0.jar");
        findAgentJar("pinpoint-bootstrap-1.10.20.jar");
        findAgentJar("pinpoint-bootstrap.jar");


        findAgentJarAssertFail("pinpoint-bootstrap-1.a.test.jar");
        findAgentJarAssertFail("pinpointbootstrap-1.a.test.jar");
        findAgentJarAssertFail("pinpointbootstrap.jar");
    }

    private void findAgentJar(String path) {
        ClassPathResolver classPathResolver = new ClassPathResolver(path);
        boolean agentJar = classPathResolver.findAgentJar();
        Assert.assertTrue(agentJar);
    }

    private void findAgentJarAssertFail(String path) {
        ClassPathResolver classPathResolver = new ClassPathResolver(path);
        boolean agentJar = classPathResolver.findAgentJar();
        Assert.assertFalse(agentJar);
    }

    @Test
    public void nullArray() {
        String[] nullArray = null;
        try {
            for (String str : nullArray) {
                logger.warn("null");
            }
            Assert.fail();
        } catch (Exception ignored) {
        }
    }
}

