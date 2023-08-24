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

package com.navercorp.pinpoint.pluginit.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class DriverManagerUtils {

    public static void deregisterDriver() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        List<Driver> copyList = Collections.list(drivers);
        for (Driver driver : copyList) {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                // TODO log?
                e.printStackTrace();
            }
        }
    }
}
