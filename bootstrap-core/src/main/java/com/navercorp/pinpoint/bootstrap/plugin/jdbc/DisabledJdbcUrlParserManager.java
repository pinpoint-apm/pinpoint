/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Taejin Koo
 */
public class DisabledJdbcUrlParserManager implements JdbcUrlParserManager {

    public static final DisabledJdbcUrlParserManager INSTANCE = new DisabledJdbcUrlParserManager();
    public static final JdbcUrlParsingResult PARSING_RESULT = new JdbcUrlParsingResult(false, UnKnownDatabaseInfo.INSTANCE);

    @Override
    public boolean addJdbcUrlParser(JdbcUrlParser jdbcUrlParser) {
        return false;
    }

    @Override
    public DatabaseInfo parse(String url) {
        return UnKnownDatabaseInfo.INSTANCE;
    }

    @Override
    public DatabaseInfo parse(ServiceType serviceType, String url) {
        return UnKnownDatabaseInfo.INSTANCE;
    }

    @Override
    public JdbcUrlParsingResult parseWithResult(String url) {
        return PARSING_RESULT;
    }

    @Override
    public JdbcUrlParsingResult parseWithResult(ServiceType serviceType, String url) {
        return PARSING_RESULT;
    }

}
