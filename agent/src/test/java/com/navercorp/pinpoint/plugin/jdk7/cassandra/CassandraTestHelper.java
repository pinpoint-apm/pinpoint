/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.jdk7.cassandra;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class CassandraTestHelper {

    private static final String CASSANDRA_HOME = "target/test-classes/cassandra";

    private CassandraTestHelper() {
    }

    public static void init(final String cassandraVersion) throws IOException, ConfigurationException {
        final String cassandraStorageDir = String.format("%s/data_%s", CASSANDRA_HOME, cassandraVersion);
        final String cassandraConfigFile = String.format("cassandra/cassandra_%s.yaml", cassandraVersion);
        System.setProperty("cassandra.storagedir", cassandraStorageDir);
        System.setProperty("cassandra.config", cassandraConfigFile);
        prepareEnvironment();
        EmbeddedCassandraService cassandra = new EmbeddedCassandraService();
        cassandra.start();
    }

    public static String getHost() {
        return DatabaseDescriptor.getListenAddress().getHostAddress();
    }

    public static int getNativeTransportPort() {
        return DatabaseDescriptor.getNativeTransportPort();
    }

    private static void prepareEnvironment() throws IOException {
        try {
            cleanUpFiles();
        } catch (IOException e) {
            // ignore - just let the server start
        }
    }

    private static void cleanUpFiles() throws IOException {
        Set<String> fileLocations = new HashSet<String>();
        for (String dataFileLocation : DatabaseDescriptor.getAllDataFileLocations()) {
            fileLocations.add(dataFileLocation);
        }
        fileLocations.add(DatabaseDescriptor.getCommitLogLocation());
        fileLocations.add(DatabaseDescriptor.getSavedCachesLocation());
        for (String fileLocation : fileLocations) {
            File location = new File(fileLocation);
            if (location.exists() && location.isDirectory()) {
                FileUtils.deleteDirectory(fileLocation);
            }
        }
    }
}
