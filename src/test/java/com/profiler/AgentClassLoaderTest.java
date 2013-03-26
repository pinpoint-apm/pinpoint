package com.profiler;


import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 *
 */
public class AgentClassLoaderTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void boot() throws MalformedURLException, ClassNotFoundException {
        String projectDir = getProjectLibDir();

        logger.info("lib:" + projectDir);

        String testJar = "hippoClassLoaderTest-1.0.jar";
        logger.info("load lib:" + testJar);

        String testJarPath = projectDir + File.separator + testJar;
        logger.info("load testlib:" + testJarPath);
        File file = new File(testJarPath);
        Assert.assertTrue(file.exists());
        AgentClassLoader agentClassLoader = new AgentClassLoader(new URL[]{file.toURI().toURL()});
        agentClassLoader.setBootClass("com.profiler.boot.BootClassTest");
        agentClassLoader.boot();

    }

    private String getProjectLibDir() {
        ProtectionDomain protectionDomain = AgentClassLoader.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URL location = codeSource.getLocation();

        logger.info("lib location:" + location);
        String path = location.getPath();
        // file:/D:/nhn_source/hippo_project/hippo-tomcat-profiler/target/classes/
        int dirPath = path.lastIndexOf("target/classes/");
        if (dirPath == -1) {
            throw new RuntimeException("target/classes/ not found");
        }
        String projectDir = path.substring(0, dirPath);
        return projectDir + "src/test/lib";
    }
}
