/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.common.mybatis.typehandler;

import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.util.TagUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TagTypeHandler implements TypeHandler<Tag> {

    @Override
    public void setParameter(PreparedStatement ps, int i, Tag parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public Tag getResult(ResultSet rs, String columnName) throws SQLException {
        return TagUtils.parseTag(rs.getString(columnName));
    }

    @Override
    public Tag getResult(ResultSet rs, int columnIndex) throws SQLException {
        return TagUtils.parseTag(rs.getString(columnIndex));
    }

    @Override
    public Tag getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return TagUtils.parseTag(cs.getString(columnIndex));
    }
}
