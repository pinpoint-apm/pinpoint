/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.collector.DataSourceDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.DataSourceAlarmVO;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class DataSourceConnectionUsageRateChecker extends AgentChecker<List<DataSourceAlarmVO>> {

    public DataSourceConnectionUsageRateChecker(DataSourceDataCollector dataSourceDataCollector, Rule rule) {
        super(rule, "%", dataSourceDataCollector);
    }

    @Override
    protected boolean decideResult(List<DataSourceAlarmVO> dataSourceAlarmVOList) {
        for (DataSourceAlarmVO dataSourceAlarm : dataSourceAlarmVOList) {
            if (decideResult0(dataSourceAlarm)) {
                return true;
            }
        }

        return false;
    }

    private boolean decideResult0(DataSourceAlarmVO dataSourceAlarmVO) {
        if (dataSourceAlarmVO.getConnectionUsedRate() > rule.getThreshold()) {
            return true;
        }

        return false;
    }

    @Override
    protected Map<String, List<DataSourceAlarmVO>> getAgentValues() {
        return ((DataSourceDataCollector) dataCollector).getDataSourceConnectionUsageRate();
    }

    public List<String> getSmsMessage() {
        List<String> messages = new LinkedList<>();

        for (Map.Entry<String, List<DataSourceAlarmVO>> detected : detectedAgents.entrySet()) {
            for (DataSourceAlarmVO dataSourceAlarmVO : detected.getValue()) {
                if (decideResult0(dataSourceAlarmVO)) {
                    messages.add(String.format("[PINPOINT Alarm - %s] DataSource %s connection pool usage %s%s (Threshold : %s%s)", detected.getKey(), dataSourceAlarmVO.getDatabaseName(), dataSourceAlarmVO.getConnectionUsedRate(), unit, rule.getThreshold(), unit));
                }
            }
        }

        return messages;
    }

    @Override
    public String getEmailMessage() {
        StringBuilder message = new StringBuilder();
        for (Map.Entry<String, List<DataSourceAlarmVO>> detected : detectedAgents.entrySet()) {
            for (DataSourceAlarmVO dataSourceAlarmVO : detected.getValue()) {
                if (decideResult0(dataSourceAlarmVO)) {
                    message.append(String.format(" Value of agent(%s) has %s%s(DataSource %s connection pool usage) during the past 5 mins.(Threshold : %s%s)", detected.getKey(), dataSourceAlarmVO.getConnectionUsedRate(), unit, dataSourceAlarmVO.getDatabaseName(), rule.getThreshold(), unit));
                    message.append("<br>");
                }
            }

        }
        return message.toString();
    }

}
