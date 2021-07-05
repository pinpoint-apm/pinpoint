/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.mybatis;

import org.apache.pinot.client.PinotConnection;
import org.apache.pinot.client.PinotPreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Hyunjoon Cho
 */
public class WrappedPinotPreparedStatement extends PinotPreparedStatement {

    public WrappedPinotPreparedStatement(PinotConnection connection, String query) {
        super(connection, query);
    }

    @Override
    public boolean execute() throws SQLException {
        ResultSet resultSet = executeQuery();
        if (resultSet.isLast()) {
            return false;
        } else {
            return true;
        }
    }
}
