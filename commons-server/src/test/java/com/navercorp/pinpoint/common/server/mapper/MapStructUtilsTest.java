package com.navercorp.pinpoint.common.server.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class MapStructUtilsTest {

    @Test
    void jsonToLongList() {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<Long> longs = mapStructUtils.jsonToLongList("[1,2,3]");
        assertThat(longs).containsExactly(1L, 2L, 3L);
    }

    @Test
    void jsonStrToIntegerList() {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<Integer> integers = mapStructUtils.jsonToIntegerList("[1,2,3]");
        assertThat(integers).containsExactly(1, 2, 3);
    }

    @Test
    void jsonStrToStringList() {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<String> strings = mapStructUtils.jsonToStringList("[1,2,3]");
        assertThat(strings).containsExactly("1", "2", "3");
    }
}