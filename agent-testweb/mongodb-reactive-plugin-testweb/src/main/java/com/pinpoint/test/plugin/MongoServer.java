package com.pinpoint.test.plugin;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class MongoServer {
    private MongodExecutable mongodExecutable;
    private MongodProcess mongod;
    private static final int PORT = 27017;

    public String getUri() {
        return "mongodb://localhost:" + PORT;
    }


    @PostConstruct
    public void init() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();

        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(PORT, Network.localhostIsIPv6()))
                .build();

        this.mongodExecutable = starter.prepare(mongodConfig);
        this.mongod = mongodExecutable.start();
    }
    @PreDestroy
    public void destroy() {
        if (mongod != null) {
            mongod.stop();
        }
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }
}
