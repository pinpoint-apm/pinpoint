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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.json.Tags;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author minwoo.jung
 */
@MappedJdbcTypes({JdbcType.VARCHAR})
public class TagListTypeHandler implements TypeHandler<List<Tag>> {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final static ObjectMapper OBJECT_MAPPER = getMapper();


    static ObjectMapper getMapper() {
        return Jackson.newBuilder()
                .serializerByType(List.class, new TagMySqlSerializer())
                .build();
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<Tag> parameter, JdbcType jdbcType) throws SQLException {
        String tagListJson = serialize(parameter);
        ps.setString(i, tagListJson);
    }

    @Override
    public List<Tag> getResult(ResultSet rs, String columnName) throws SQLException {
        String tagListJson = rs.getString(columnName);
        return deserialize(tagListJson);
    }

    @Override
    public List<Tag> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String tagListJson = rs.getString(columnIndex);
        return deserialize(tagListJson);
    }

    @Override
    public List<Tag> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String tagListJson = cs.getString(columnIndex);
        return deserialize(tagListJson);
    }

    public String serialize(List<Tag> tagList) {
        try {
            return OBJECT_MAPPER.writeValueAsString(tagList);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing List<Tag> : {}", tagList, e);
            throw new JsonRuntimeException("Error serializing tagList", e);
        }
    }

    public List<Tag> deserialize(String tagListJson) {
        try {
            Tags tags = OBJECT_MAPPER.readValue(tagListJson, Tags.class);
            return tags.getTags();
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing tagList json : {}", tagListJson, e);
            throw new JsonRuntimeException("Error deserializing tagListJson", e);
        }
    }
}
