/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.uristat.web.config;

import com.navercorp.pinpoint.uristat.web.chart.DefaultUriStatChartType;
import com.navercorp.pinpoint.uristat.web.chart.UriStatChartType;
import com.navercorp.pinpoint.uristat.web.chart.UriStatChartTypeFactory;
import com.navercorp.pinpoint.uristat.web.dao.UriStatChartDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UriStatChartTypeConfiguration {

    private static final List<String> HISTOGRAM_FIELD = List.of(
            "0 ~ 100ms", "100 ~ 300ms", "300 ~ 500ms", "500 ~ 1000ms", "1000 ~ 3000ms", "3000 ~ 5000ms", "5000 ~ 8000ms", "8000ms ~"
    );

    @Bean
    public UriStatChartType uriStatApdexChart(@Qualifier("pinotApdexChartDao") UriStatChartDao chartDao) {
        List<String> field = List.of("apdex");
        return new DefaultUriStatChartType("apdex", field, chartDao);
    }


    @Bean
    public UriStatChartType uriStatFailureChart(@Qualifier("pinotFailureCountChartDao") UriStatChartDao chartDao) {
        return new DefaultUriStatChartType("failure", HISTOGRAM_FIELD, chartDao);
    }

    @Bean
    public UriStatChartType uriStatLatencyChart(@Qualifier("pinotLatencyChartDao") UriStatChartDao chartDao) {
        List<String> field = List.of("avg", "max");
        return new DefaultUriStatChartType("latency", field, chartDao);
    }


    @Bean
    public UriStatChartType uriStatTotalChart(@Qualifier("pinotTotalCountChartDao") UriStatChartDao chartDao) {
        return new DefaultUriStatChartType("total", HISTOGRAM_FIELD, chartDao);
    }


    @Bean
    public UriStatChartTypeFactory uriStatChartTypeFactory(UriStatChartType... uriStatCharts) {
        return new UriStatChartTypeFactory(uriStatCharts);
    }
}
