package com.navercorp.pinpoint.plugin.mongodb;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.extract.DirectoryAndExecutableNaming;
import de.flapdoodle.embed.process.extract.ImmutableDirectoryAndExecutableNaming;
import de.flapdoodle.embed.process.extract.NoopTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.ImmutableExtractedArtifactStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MongodServer {

    private final MongodExecutable mongodExecutable;
    private MongodProcess mongod;

    public MongodServer() throws IOException {
        MongodStarter starter = newStarter();

        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.V4_4)
                .net(new Net(MongoDBITConstants.BIND_ADDRESS, MongoDBITConstants.PORT, Network.localhostIsIPv6()))
                .build();
        this.mongodExecutable = starter.prepare(mongodConfig);
    }

    public void start() throws IOException {
        mongod = mongodExecutable.start();
    }

    private String getTempPath() throws IOException {
        URL resource = MongoDBITBase.class.getProtectionDomain().getCodeSource().getLocation();
        if (resource == null) {
            throw new IOException("build class path not found");
        }
        try {
            Path target = Paths.get(resource.toURI()).getParent();
            Path mongodBinary = target.resolve("mongod_mock");
            return mongodBinary.toString();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private MongodStarter newStarter() throws IOException {
        Command command = Command.MongoD;

        ImmutableDirectoryAndExecutableNaming temp = DirectoryAndExecutableNaming.builder()
                .directory(new FixedPath(getTempPath()))
                .executableNaming(new NoopTempNaming())
                .build();
        ImmutableExtractedArtifactStore artifactStore = Defaults.extractedArtifactStoreFor(command)
                .withTemp(temp);

        RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
                .artifactStore(artifactStore)
                .build();
        return MongodStarter.getInstance(runtimeConfig);

    }

    public boolean isProcessRunning() {
        if (mongod == null) {
            return false;
        }
        return this.mongod.isProcessRunning();
    }

    public void stop() {
        if (mongod != null) {
            mongod.stop();
        }
    }
}
