package com.navercorp.pinpoint.common.dao.pinot;

import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.ibatis.type.TypeHandler;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultiValueTagTypeHandlerTest {

    @Test
    void getResult() throws SQLException {
        String json = """
                [ "name1:1", "name2:2" ]""";
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(any())).thenReturn(json);

        TypeHandler<List<Tag>> handler = new MultiValueTagTypeHandler(Jackson.newMapper());
        List<Tag> columnName = handler.getResult(resultSet, "columnName");
        assertThat(columnName)
                .contains(new Tag("name1", "1"), new Tag("name2", "2"));
    }
}