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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.mongodb.Mongo;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
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
@Dependency({ "org.mongodb:mongo-java-driver:[2.10,2.max]", "de.flapdoodle.embed:de.flapdoodle.embed.mongo:1.47.3" })
public class MongoDBIT_2_X_IT extends MongoDBITBase_2_X {

    private Mongo mongo;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        version = 2.9;
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    @Override
    public void setClient() {
        mongo = new Mongo("localhost", 27018);
        database = mongo.getDB("myMongoDbFake");
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
            System.err.println("Read preference is not supported by the driver.");
        } catch (NoSuchMethodException e) {
            System.err.println("Read preference is not supported by the driver.");
        } catch (SecurityException e) {
            System.err.println("Error while inspecting com.mongodb.ReadPreference; Cause - ");
        }

        try {
            // Class available from 2.7.0
            Class<?> dBCollectionClass = Class.forName("com.mongodb.WriteConcern");
            // Method available from 2.9.0
            Field declaredField = dBCollectionClass.getDeclaredField("ACKNOWLEDGED");
            if (declaredField != null) {
                database.setWriteConcern(WriteConcern.ACKNOWLEDGED);
                mongo.setWriteConcern(WriteConcern.ACKNOWLEDGED);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("WriteConcern is not supported by the driver.");
        } catch (NoSuchFieldException e) {
            System.err.println(
                    "WriteConcern of type ACKNOWLEDGED is not supported by the driver. Falling back to the type SAFE");
            database.setWriteConcern(WriteConcern.SAFE);
            mongo.setWriteConcern(WriteConcern.SAFE);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeClient() {
        mongo.close();
    }
}
