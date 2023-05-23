package com.navercorp.pinpoint.common.profiler.message;

import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RouteResultTest {

    @Test
    public void compatibility() {
        Assertions.assertThat(RouteResult.values())
                .hasSize(TRouteResult.values().length);

        List<Integer> thriftCodes = Stream.of(TRouteResult.values())
                .map(TRouteResult::getValue)
                .collect(Collectors.toList());

        Assertions.assertThat(RouteResult.values())
                .extracting(RouteResult::code)
                .containsAll(thriftCodes);
    }
}
