package com.nhn.pinpoint;


import com.profiler.bootstrap.AgentClassLoader;
import com.profiler.config.ProfilerConfig;
import com.profiler.logging.LoggerBinder;
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
        String projectDir = getProjectLibDir();

        logger.info("lib:" + projectDir);
        URL[] lib = getLib(projectDir);
        logger.info("lib list:" + Arrays.toString(lib));
//        String testJar = "hippoClassLoaderTest-1.0.jar";
//        logger.info("load lib:" + testJar);
//
//        String testJarPath = projectDir + File.separator + testJar;
//        logger.info("load testlib:" + testJarPath);
//        File file = new File(testJarPath);
//        Assert.assertTrue(file.exists());
//        String log4j = projectDir + File.separator + "log4j.xml";
//        File log4jFile = new File(log4j);

        AgentClassLoader agentClassLoader = new AgentClassLoader(lib);
        agentClassLoader.setBootClass("com.profiler.boot.BootClassTest");
        agentClassLoader.boot("test", new DummyInstrumentation(), new ProfilerConfig());


        LoggerBinder loggerBinder = (LoggerBinder) agentClassLoader.initializeLoggerBinder();
        com.profiler.logging.Logger test = loggerBinder.getLogger("test");
        test.info("slf4j logger test");

    }

    private URL[] getLib(String libDir) throws IOException {
        System.out.println(libDir);
        File libFile = new File(libDir);
        System.out.println("file ab:" + libFile.getAbsolutePath());
        System.out.println("file ca:" + libFile.getCanonicalPath());
        File[] list = libFile.listFiles();
        List<URL> arrayList = new ArrayList<URL>();
        for (File file : list) {
            URL url = file.toURI().toURL();
            System.out.println(url);
            arrayList.add(url);
        }

        return arrayList.toArray(new URL[arrayList.size()]);
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
        String projectDir = path.substring(1, dirPath);
        return projectDir + "src/test/lib";
    }
}
