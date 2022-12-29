/*
 * Copyright 2022 NAVER Corp.
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
package com.navercorp.pinpoint.batch.flink;

import com.navercorp.pinpoint.batch.common.BatchConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class HealthCheckTaskletV2Test {

    @Mock
    private BatchConfiguration batchConfiguration;

    @Mock
    RestTemplate restTemplate;

    @Test
    public void testGeneratedFlinkManagerServerApi() {
        when(batchConfiguration.getFlinkServerList()).thenReturn(List.of("123.234.123.234"));
        when(batchConfiguration.getFlinkRestPort()).thenReturn(1919);

        final HealthCheckTaskletV2 tasklet = new HealthCheckTaskletV2(batchConfiguration, restTemplate);
        final List<String> results = tasklet.generatedFlinkManagerServerApi();
        assertThat(results).hasSize(1);
        final String result = results.get(0);
        assertThat(result.indexOf("123.234.123.234:1919")).isGreaterThanOrEqualTo(0);
        assertThat(result.indexOf("DEAD-BEEF")).isLessThan(0);
    }

}
