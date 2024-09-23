package com.navercorp.pinpoint.common.server.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class MapStructUtilsTest {

    @Test
    void jsonStrToLongList() {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<Long> longs = mapStructUtils.jsonStrToLongList("[1,2,3]");
        assertThat(longs).containsExactly(1L, 2L, 3L);
    }
}