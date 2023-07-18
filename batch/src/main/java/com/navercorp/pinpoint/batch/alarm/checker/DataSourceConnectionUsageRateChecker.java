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

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.DataSourceDataCollector;
import com.navercorp.pinpoint.batch.alarm.vo.DataSourceAlarmVO;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 * @author Jongjin.Bae
 */
public class DataSourceConnectionUsageRateChecker extends DataSourceAlarmListValueAgentChecker {

    private static final String SMS_MESSAGE_FORMAT = "[PINPOINT Alarm - %s] DataSource %s connection pool usage %s%s (Threshold : %s%s, Raw : %s/%s)";
    private static final String EMAIL_MESSAGE_FORMAT = " Value of agent(%s) has %s%s(DataSource %s connection pool usage) during the past 5 mins.(Threshold : %s%s, Raw : %s/%s)";

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
        List<String> messages = new ArrayList<>();


        for (Map.Entry<String, List<DataSourceAlarmVO>> detected : detectedAgents.entrySet()) {
            for (DataSourceAlarmVO eachVo : detected.getValue()) {
                if (decideResult0(eachVo)) {
                    String message = String.format(SMS_MESSAGE_FORMAT, detected.getKey(), eachVo.getDatabaseName(), eachVo.getConnectionUsedRate(), unit, rule.getThreshold(), unit, eachVo.getActiveConnectionAvg(), eachVo.getMaxConnectionAvg());
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    @Override
    public String getEmailMessage(String pinpointUrl, String applicationId, String serviceType, String currentTime) {
        StringBuilder contents = new StringBuilder();
        for (Map.Entry<String, List<DataSourceAlarmVO>> detected : detectedAgents.entrySet()) {
            String agentId = detected.getKey();
            for (DataSourceAlarmVO eachVo : detected.getValue()) {
                if (decideResult0(eachVo)) {
                    String message = String.format(EMAIL_MESSAGE_FORMAT, detected.getKey(), eachVo.getConnectionUsedRate(), unit, eachVo.getDatabaseName(), rule.getThreshold(), unit, eachVo.getActiveConnectionAvg(), eachVo.getMaxConnectionAvg());
                    contents.append(message);
                }
            }
            contents.append(String.format(INSPECTOR_LINK_FORMAT, pinpointUrl, applicationId, serviceType, currentTime, agentId, agentId));
            contents.append(LINE_FEED);
        }
        return contents.toString();
    }

}
