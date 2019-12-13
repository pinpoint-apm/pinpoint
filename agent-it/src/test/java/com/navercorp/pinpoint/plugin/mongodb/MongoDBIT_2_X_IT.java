/*
 * Copyright 2019 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance,the License.
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

package com.navercorp.pinpoint.plugin.mongodb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author Community
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@Dependency({ "org.mongodb:mongo-java-driver:[2.13.0,2.max]", "de.flapdoodle.embed:de.flapdoodle.embed.mongo:1.47.3" })
public class MongoDBIT_2_X_IT extends MongoDBITBase_2_X {

    private static final PLogger LOGGER = PLoggerFactory.getLogger(MongoDBIT_2_X_IT.class);

    private MongoClient mongo;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        version = 2.13;
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    @Override
    public void setClient() {
        ServerAddress addresss = new ServerAddress("localhost", 27018);

        mongo = new MongoClient(Arrays.asList(addresss), MongoClientOptions.builder()
                .readPreference(ReadPreference.secondaryPreferred()).writeConcern(WriteConcern.ACKNOWLEDGED).build());
        database = mongo.getDB("myMongoDbFake");
        System.out.println("Database's write concern is " + database.getWriteConcern());
        try {
            // Class available from 2.7.0
            Class<?> readPreferenceClass = Class.forName("com.mongodb.ReadPreference");
            // Method available from 2.9.0
            Method declaredMethod = readPreferenceClass.getDeclaredMethod("secondaryPreferred", null);
            if (declaredMethod != null) {
                database.setReadPreference(ReadPreference.secondaryPreferred());
                mongo.setReadPreference(ReadPreference.secondaryPreferred());
            }
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Read preference is not supported by the driver.");
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Read preference is not supported by the driver.");
        } catch (SecurityException e) {
            LOGGER.warn("Error while inspecting com.mongodb.ReadPreference; Cause - ", e);
        }

        try {
            // Class available from 2.7.0
            Class<?> dBCollectionClass = Class.forName("com.mongodb.WriteConcern");
            // Field available from 2.10.0
            Field declaredField = dBCollectionClass.getDeclaredField("ACKNOWLEDGED");
            if (declaredField != null) {
                database.setWriteConcern(WriteConcern.ACKNOWLEDGED);
                mongo.setWriteConcern(WriteConcern.ACKNOWLEDGED);
                System.out.println("Database wide write concern set to " + WriteConcern.ACKNOWLEDGED);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.warn("WriteConcern is not supported by the driver.");
        } catch (NoSuchFieldException e) {
            LOGGER.warn(
                    "WriteConcern of type ACKNOWLEDGED is not supported by the driver. Falling back to the type SAFE");
            database.setWriteConcern(WriteConcern.SAFE);
            mongo.setWriteConcern(WriteConcern.SAFE);
        } catch (SecurityException e) {
            LOGGER.warn("Failed to inspect write concern. Cause - ", e);
        }
    }

    @Override
    public void closeClient() {
        mongo.close();
    }
}
