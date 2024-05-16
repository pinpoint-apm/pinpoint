package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseTimeViewModelTest {

    ObjectMapper mapper = Jackson.newMapper();

    @Test
    void timeCount() throws JsonProcessingException {
        ResponseTimeViewModel.TimeCount timeCount = new ResponseTimeViewModel.TimeCount(1, 2);

        String json = mapper.writeValueAsString(timeCount);
        String expected = mapper.writeValueAsString(List.of(1, 2));
        assertEquals(expected, json);
    }

}