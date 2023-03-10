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

package com.navercorp.pinpoint.pinot.datasource;

import org.apache.commons.codec.binary.Hex;
import org.apache.pinot.client.PinotConnection;
import org.apache.pinot.client.PinotPreparedStatement;
import org.apache.pinot.client.utils.DateTimeUtils;
import org.apache.pinot.client.utils.DriverUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * @author Hyunjoon Cho
 */
public class WrappedPinotPreparedStatement extends PinotPreparedStatement {
    // Temporary variables for fillStatementWithParameters(). TODO: remove these when Pinot driver is fixed.
    private String query;
    private final String[] parameters;
    private int maxRows = Integer.MAX_VALUE;

    public WrappedPinotPreparedStatement(PinotConnection connection, String query) {
        super(connection, query);
        this.query = query;
        if (!DriverUtils.queryContainsLimitStatement(this.query)) {
            this.query = this.query + " LIMIT " + this.maxRows;
        }
        this.parameters = new String[this.getQuestionMarkCount(query)];
    }

    /* copied from org.apache.pinot.client.PreparedStatement. TODO: remove when Pinot driver is fixed. */
    private int getQuestionMarkCount(String query) {
        int questionMarkCount = 0;

        for(int index = query.indexOf(63); index != -1; index = query.indexOf(63, index + 1)) {
            ++questionMarkCount;
        }

        return questionMarkCount;
    }


    @Override
    public boolean execute() throws SQLException {
        // WARNING Do not invoke close().
        // Ignore spotbug warning
        ResultSet resultSet = executeQuery(fillStatementWithParameters());
        if (resultSet.isLast()) {
            return false;
        } else {
            return true;
        }
    }

    /* temporary function. TODO: remove these when Pinot driver is fixed */
    private String fillStatementWithParameters() throws SQLException {
        String[] queryParts = query.split("\\?");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < parameters.length) {
            sb.append(queryParts[i]);
            if (parameters[i] == null) {
                throw new SQLException("Prepared Statement parameter is not provided. index = " + i);

            }
            sb.append(parameters[i]);
            i++;
        }

        while (i < queryParts.length) {
            sb.append(queryParts[i]);
            i++;
        }

        return sb.toString();
    }

    /* temporary setter + getter functions to redirect mybatis parameter setting to temporary variables this.query, this.parameters, and this.maxRows
     TODO: remove these when Pinot driver is fixed */
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = "'NULL'";
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] =  "'" + x + "'";
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = String.valueOf(x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = String.valueOf(x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = String.valueOf(x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = String.valueOf(x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = String.valueOf(x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] =  "'" + x.replace("'", "''") + "'";;
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = Hex.encodeHexString(x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        this.parameters[parameterIndex - 1] = DateTimeUtils.dateToString(x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = DateTimeUtils.timeToString(x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = DateTimeUtils.timeStampToString(x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        this.validateState();
        this.parameters[parameterIndex - 1] = x.toString();
    }

    public void clearParameters() throws SQLException {
        this.validateState();
        for (int i = 0; i < this.parameters.length; i++) {
            this.parameters[i] = null;
        }
    }

    public int getFetchSize() throws SQLException {
        return this.maxRows;
    }

    public void setFetchSize(int rows) throws SQLException {
        this.maxRows = rows;
    }

    public int getMaxRows() throws SQLException {
        return this.maxRows;
    }

    public void setMaxRows(int max) throws SQLException {
        this.maxRows = max;
    }
}
