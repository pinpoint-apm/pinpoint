/*
 * Copyright 2022 NAVER Corp.
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
 */

package com.navercorp.pinpoint.metric.web;

/**
 * @author minwoo.jung
 */

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@PropertySources({
        @PropertySource(name = "WebMetricPropertySources", value = {WebMetricPropertySources.METRIC}),
        @PropertySource(name = "WebMetricPropertySources-pinot", value = {WebMetricPropertySources.PINOT_JDBC})
})
public class WebMetricPropertySources {
    public static final String METRIC= "classpath:pinot-web/profiles/${pinpoint.profiles.active:release}/pinpoint-web-metric.properties";

    public static final String PINOT_JDBC= "classpath:pinot-web/profiles/${pinpoint.profiles.active:release}/jdbc-pinot.properties";
}
