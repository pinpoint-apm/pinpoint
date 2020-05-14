package com.navercorp.pinpoint.bootstrap.agentdir;

import com.navercorp.pinpoint.common.Version;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class AgentBuildInfoResolverTest {

    private static final String BOOTSTRAP_JAR = "pinpoint-bootstrap-" + Version.VERSION + ".jar";
    private static final String SEPARATOR = File.separator;

    @Test
    public void testResolve() {
        final String buildInfoDir = AgentBuildInfoResolverTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String agentJarName = BOOTSTRAP_JAR;
        String agentJarFullPath = buildInfoDir + SEPARATOR + agentJarName;
        String agentDirPath = buildInfoDir;
        BootDir bootDir = new BootDir(buildInfoDir, Collections.EMPTY_LIST);
        List<URL> libs = Collections.EMPTY_LIST;
        List<String> plugins = Collections.EMPTY_LIST;
        final AgentDirectory agentDirectory = new AgentDirectory(agentJarName, agentJarFullPath, agentDirPath, bootDir, libs, plugins);
        AgentBuildInfoResolver resolver = new AgentBuildInfoResolver(agentDirectory);
        AgentBuildInfo buildInfo = resolver.resolve();
        Assert.assertTrue("info available", buildInfo.isAvailable());
        Assert.assertEquals("info commit id abbrev", "ec741e3", buildInfo.getGitCommitIdAbbrev());
        Assert.assertEquals("info branch", "cmp-2.0.x", buildInfo.getGitBranch());
    }
}
