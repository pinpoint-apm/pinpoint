package com.navercorp.pinpoint.web.applicationmap.view;

import tools.jackson.databind.json.JsonMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseTimeViewModelTest {

    JsonMapper mapper = Jackson.newMapper();

    @Test
    void timeCount() {
        TimeCount timeCount = new TimeCount(1, 2);

        String json = mapper.writeValueAsString(timeCount);
        String expected = mapper.writeValueAsString(List.of(1, 2));
        assertEquals(expected, json);
    }

}