package test.pinpoint.plugin.kafka;

import java.io.File;
import java.util.Properties;

public abstract class KafkaUnitServer {

    protected String zookeeperString;
    protected String brokerString;
    protected int zkPort;
    protected int brokerPort;
    protected Properties kafkaBrokerConfig;
    protected int zkMaxConnections;
    protected ZookeeperUnitServer zookeeper;
    protected File logDir;

    public KafkaUnitServer(int zkPort, int brokerPort, int zkMaxConnections) {
        this.kafkaBrokerConfig = new Properties();
        this.zkPort = zkPort;
        this.brokerPort = brokerPort;
        this.zookeeperString = "localhost:" + zkPort;
        this.brokerString = "localhost:" + brokerPort;
        this.zkMaxConnections = zkMaxConnections;
    }

    public abstract void startup();
    public abstract void shutdown();

    public int getBrokerPort() {
        return brokerPort;
    }
}
