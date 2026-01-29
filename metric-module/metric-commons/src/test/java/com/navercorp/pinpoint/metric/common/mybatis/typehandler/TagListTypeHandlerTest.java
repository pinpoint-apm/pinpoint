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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TagListTypeHandlerTest {

    ObjectMapper mapper = new ObjectMapper();


    @Test
    void json() throws SQLException {

        TagListSerializer serializer = new TagListSerializer(mapper);
        TagListTypeHandler handler = new TagListTypeHandler(serializer);

        List<Tag> list = List.of(
                new Tag("a", "1"),
                new Tag("b", "2")
        );

        PreparedStatement statement = mock(PreparedStatement.class);
        handler.setParameter(statement, 0, list, null);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(statement).setString(anyInt(), captor.capture());

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(any())).thenReturn(captor.getValue());

        List<Tag> newTags = handler.getResult(resultSet, "columnName");
        Assertions.assertEquals(list, newTags);

    }
}