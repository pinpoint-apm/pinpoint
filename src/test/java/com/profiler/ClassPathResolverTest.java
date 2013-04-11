package com.profiler;

import com.profiler.bootstrap.ClassPathResolver;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *
 */
public class ClassPathResolverTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testFindAgentJar() throws Exception {
        String path = "D:\\nhn_source\\hippo_project\\deploy\\apache-tomcat-6.0.35\\bin\\bootstrap.jar;D:\\nhn_source\\hippo_project\\deploy\\agent\\agentlib/lib/javassist-3.16.1.GA.jar;D:\\nhn_source\\hippo_project\\deploy\\agent\\agentlib/lib/libthrift-0.8.0.wolog.jar;D:\\nhn_source\\hippo_project\\deploy\\agent\\agentlib/lib/hippo-commons-0.0.2.jar;;D:\\nhn_source\\hippo_project\\deploy\\agent\\agentlib/hippo-tomcat-profiler-0.0.2.jar";
        // D:\nhn_source\hippo_project\deploy\agent\agentlib/hippo-tomcat-profiler-0.0.2.jar
        ClassPathResolver classPathResolver = new ClassPathResolver(path);
        boolean findAgentJar = classPathResolver.findAgentJar();
        Assert.assertTrue(findAgentJar);

        String agentJar = classPathResolver.getAgentJarName();
        Assert.assertEquals("hippo-tomcat-profiler-0.0.2.jar", agentJar);

        String agentPath = classPathResolver.getAgentJarFullPath();
        Assert.assertEquals("D:\\nhn_source\\hippo_project\\deploy\\agent\\agentlib/hippo-tomcat-profiler-0.0.2.jar", agentPath);

        String agentDirPath = classPathResolver.getAgentDirPath();
        Assert.assertEquals("D:\\nhn_source\\hippo_project\\deploy\\agent\\agentlib", agentDirPath );

        String agentLibPath = classPathResolver.getAgentLibPath();
        Assert.assertEquals("D:\\nhn_source\\hippo_project\\deploy\\agent\\agentlib"+File.separator+ "lib", agentLibPath);

    }

    @Test
    public void findAgentJar() {
        findAgentJar("hippo-tomcat-profiler-0.0.2.jar");
        findAgentJar("hippo-tomcat-profiler-1.0.0.jar");
        findAgentJar("hippo-tomcat-profiler-1.10.20.jar");


        findAgentJarAssertFail("hippo-tomcat-profiler-1.a.test.jar");
        findAgentJarAssertFail("hippotomcatprofiler-1.a.test.jar");
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
}

