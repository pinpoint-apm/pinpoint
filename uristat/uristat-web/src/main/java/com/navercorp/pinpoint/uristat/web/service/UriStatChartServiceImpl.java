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

package com.navercorp.pinpoint.uristat.web.service;

import com.navercorp.pinpoint.uristat.web.chart.UriStatChartType;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UriStatChartServiceImpl implements UriStatChartService {

    @Override
    public List<UriStatChartValue> getUriStatChartDataApplication(UriStatChartType type, UriStatChartQueryParameter queryParameter) {
        return type.getChartDao().getChartDataApplication(queryParameter);
    }

    @Override
    public List<UriStatChartValue> getUriStatChartDataAgent(UriStatChartType type, UriStatChartQueryParameter queryParameter) {
        return type.getChartDao().getChartDataAgent(queryParameter);
    }

}
