/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.mongo.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.MongoDatabaseInfo;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DatabaseInfoUtilsTest {

    @Test
    public void getDatabaseInfo() {
        DatabaseInfoAccessor databaseInfoAccessor = mock(DatabaseInfoAccessor.class);
        DatabaseInfo defaultDatabaseInfo = mock(MongoDatabaseInfo.class);
        when(databaseInfoAccessor._$PINPOINT$_getDatabaseInfo()).thenReturn(defaultDatabaseInfo);

        DatabaseInfo databaseInfo = DatabaseInfoUtils.getDatabaseInfo(databaseInfoAccessor, MongoDatabaseInfo.UNKNOWN_MONGO_DATABASE_INFO);

        Assertions.assertEquals(databaseInfo, defaultDatabaseInfo);
    }

    @Test
    public void getDatabaseInfo_return_null() {
        DatabaseInfoAccessor databaseInfoAccessor = mock(DatabaseInfoAccessor.class);
        when(databaseInfoAccessor._$PINPOINT$_getDatabaseInfo()).thenReturn(null);

        DatabaseInfo databaseInfo = DatabaseInfoUtils.getDatabaseInfo(databaseInfoAccessor, MongoDatabaseInfo.UNKNOWN_MONGO_DATABASE_INFO);
        Assertions.assertEquals(databaseInfo, MongoDatabaseInfo.UNKNOWN_MONGO_DATABASE_INFO);
    }

    @Test
    public void getDatabaseInfo_unknown() {
        DatabaseInfo databaseInfo = DatabaseInfoUtils.getDatabaseInfo(new Object(), MongoDatabaseInfo.UNKNOWN_MONGO_DATABASE_INFO);

        Assertions.assertEquals(databaseInfo, MongoDatabaseInfo.UNKNOWN_MONGO_DATABASE_INFO);
    }

}