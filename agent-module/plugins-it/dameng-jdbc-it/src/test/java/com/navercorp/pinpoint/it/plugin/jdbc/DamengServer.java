package com.navercorp.pinpoint.it.plugin.jdbc;

import com.navercorp.pinpoint.it.plugin.utils.LogOutputStream;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.Properties;

/**
 * @author yjqg6666
 */
public class DamengServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    public static final DockerImageName DM_IMAGE = DockerImageName.parse("yjqg6666/dameng-server:v8-20240715");

    private GenericContainer<?> dm;

    protected static final String USER = "SYSDBA";
    protected static final String PASSWORD = "SYSDBA001";
    private static final int PORT = 5236;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        dm = new GenericContainer(DM_IMAGE)
                .withExposedPorts(PORT)
                .withPrivilegedMode(true)
                .withReuse(false)
                .withEnv("LD_LIBRARY_PATH", "/opt/dmdbms/bin")
                .withEnv("UNICODE_FLAG", "1")
                .withEnv("INSTANCE_NAME", "DM8CI")
                .waitingFor(Wait.forLogMessage(".*SYSTEM IS READY.*\\n", 1));
        dm.withLogConsumer(new LogOutputStream(logger::info));
        dm.start();

        sourceInitSql();

        int port = dm.getMappedPort(PORT);
        String url = "jdbc:dm://" + dm.getHost() + ":" + port;
        System.setProperty(DriverProperties.URL, url);
        System.setProperty(DriverProperties.HOST, dm.getHost());
        System.setProperty(DriverProperties.PORT, String.valueOf(port));
        System.setProperty(DriverProperties.USER, USER);
        System.setProperty(DriverProperties.PASSWARD, PASSWORD);

        Properties properties = new Properties();
        properties.setProperty(DriverProperties.URL, url);
        properties.setProperty(DriverProperties.HOST, dm.getHost());
        properties.setProperty(DriverProperties.PORT, String.valueOf(port));
        properties.setProperty(DriverProperties.USER, USER);
        properties.setProperty(DriverProperties.PASSWARD, PASSWORD);
        return properties;
    }

    @Override
    public void afterAll() {
        if (dm != null) {
            dm.stop();
        }
    }

    private void sourceInitSql() {

        String dst = "/tmp/citest_init.sql";
        dm.copyFileToContainer(MountableFile.forClasspathResource("jdbc/dameng/init.sql"), dst);

        String cmd = "/tmp/citest_init_sql";
        String content = String.format("#!/bin/sh\n/opt/dmdbms/bin/disql %s/%s < %s", USER, PASSWORD, dst);
        int filemode = 000755; //can not be 755 in oct.
        dm.copyFileToContainer(Transferable.of(content, filemode), cmd);

        boolean ok = true;
        String msg = Strings.EMPTY;
        try {
            Container.ExecResult execResult = dm.execInContainer(cmd);
            if (execResult.getExitCode() != 0) {
                ok = false;
                msg = execResult.getStderr();
            }
            System.out.println(execResult.getStdout());
        } catch (Exception e) {
            ok = false;
            msg = e.getMessage();
        }
        if (!ok) {
            System.err.println("Source init.sql failed, cause:" + msg);
        }
    }

}
