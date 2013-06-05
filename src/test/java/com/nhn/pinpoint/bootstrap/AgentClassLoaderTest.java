package com.nhn.pinpoint.bootstrap;


import com.nhn.pinpoint.bootstrap.AgentClassLoader;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.logging.LoggerBinder;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class AgentClassLoaderTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void boot() throws IOException, ClassNotFoundException {
        AgentClassLoader agentClassLoader = new AgentClassLoader(new URL[0]);
        agentClassLoader.setBootClass("com.nhn.pinpoint.bootstrap.DummyAgent");
        agentClassLoader.boot("test", new DummyInstrumentation(), new ProfilerConfig());

        LoggerBinder loggerBinder = (LoggerBinder) agentClassLoader.initializeLoggerBinder();
        com.nhn.pinpoint.profiler.logging.Logger test = loggerBinder.getLogger("test");
        test.info("slf4j logger test");

    }

    private String getProjectLibDir() {
        // 필요는 없으나 protectionDomain을 테스트하기 좋아 내비둠.
        ProtectionDomain protectionDomain = AgentClassLoader.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URL location = codeSource.getLocation();

        logger.info("lib location:" + location);
        String path = location.getPath();
        // file:/D:/nhn_source/pinpoint_project/pinpoint-tomcat-profiler/target/classes/
        int dirPath = path.lastIndexOf("target/classes/");
        if (dirPath == -1) {
            throw new RuntimeException("target/classes/ not found");
        }
        String projectDir = path.substring(1, dirPath);
        return projectDir + "src/test/lib";
    }
}
