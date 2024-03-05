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

package com.navercorp.pinpoint.common.dao.pinot;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.util.TagUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class MultiValueTagTypeHandler implements TypeHandler<List<Tag>> {

    private final static ObjectMapper OBJECT_MAPPER = getMapper();
    private final TypeReference<List<Tag>> REF_LIST_TAG = new TypeReference<>() {
    };

    private static ObjectMapper getMapper() {
        return Jackson.newBuilder()
                .deserializerByType(List.class, new CustomObjectListDeserializer())
                .build();
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<Tag> parameter, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Tag> getResult(ResultSet rs, String columnName) throws SQLException {
        String jsonString = rs.getString(columnName);

        try {
            return OBJECT_MAPPER.readValue(jsonString, REF_LIST_TAG);
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public List<Tag> getResult(ResultSet rs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Tag> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private List<Tag> createTagList(Object tagValues) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    static class CustomObjectListDeserializer extends JsonDeserializer<List<Tag>> {

        private static final TypeReference<ArrayList<String>> REF = new TypeReference<>() {

        };
        @Override
        public List<Tag> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

            List<String> list = jsonParser.readValueAs(REF);
            List<Tag> tagList = new ArrayList<>();
            for (String tagString : list) {
                Tag tag = TagUtils.parseTag(tagString);
                tagList.add(tag);
            }

            return tagList;
        }
    }
}
