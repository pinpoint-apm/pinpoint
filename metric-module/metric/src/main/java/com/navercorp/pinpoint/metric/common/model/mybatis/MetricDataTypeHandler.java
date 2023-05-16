/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.common.model.mybatis;

import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author minwoo.jung
 */
@MappedJdbcTypes({JdbcType.TINYINT})
public class MetricDataTypeHandler implements TypeHandler<MetricDataType> {

    @Override
    public void setParameter(PreparedStatement ps, int i, MetricDataType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public MetricDataType getResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return MetricDataType.getByCode(code);
    }

    @Override
    public MetricDataType getResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return MetricDataType.getByCode(code);
    }

    @Override
    public MetricDataType getResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return MetricDataType.getByCode(code);
    }
}
