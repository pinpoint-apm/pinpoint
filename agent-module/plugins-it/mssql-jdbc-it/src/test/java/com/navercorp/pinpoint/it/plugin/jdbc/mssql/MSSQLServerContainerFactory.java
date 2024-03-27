/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.jdbc.mssql;

import com.navercorp.pinpoint.it.plugin.utils.LogOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.MSSQLServerContainer;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class MSSQLServerContainerFactory {
    private MSSQLServerContainerFactory() {
    }

    public static MSSQLServerContainer<?> newMSSQLServerContainer(String loggerName) {
        final MSSQLServerContainer<?> mssqlServerContainer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2017-CU12");
        mssqlServerContainer.acceptLicense();
        mssqlServerContainer.withInitScript("sql/init_mssql.sql");

        final Logger logger = LogManager.getLogger(loggerName);
        mssqlServerContainer.withLogConsumer(new LogOutputStream(logger::info));
        return mssqlServerContainer;
    }
}
