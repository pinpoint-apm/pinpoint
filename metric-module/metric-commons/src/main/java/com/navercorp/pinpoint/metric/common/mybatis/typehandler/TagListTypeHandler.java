/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.metric.common.mybatis.typehandler;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@MappedJdbcTypes({JdbcType.VARCHAR})
public class TagListTypeHandler implements TypeHandler<List<Tag>> {

    private final TagListSerializer serializer;

    public TagListTypeHandler(TagListSerializer serializer) {
        this.serializer = Objects.requireNonNull(serializer, "serializer");
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<Tag> parameter, JdbcType jdbcType) throws SQLException {
        String tagListJson = serializer.serialize(parameter);
        ps.setString(i, tagListJson);
    }

    @Override
    public List<Tag> getResult(ResultSet rs, String columnName) throws SQLException {
        String tagListJson = rs.getString(columnName);
        return serializer.deserialize(tagListJson);
    }

    @Override
    public List<Tag> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String tagListJson = rs.getString(columnIndex);
        return serializer.deserialize(tagListJson);
    }

    @Override
    public List<Tag> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String tagListJson = cs.getString(columnIndex);
        return serializer.deserialize(tagListJson);
    }

}
