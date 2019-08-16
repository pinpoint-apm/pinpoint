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
package com.navercorp.pinpoint.web.batch.flink;

import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author minwoo.jung
 */
public class HealthCheckTasklet implements Tasklet {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String URL_FORMAT = "http://%s:8081/joboverview";
    private final List<String> jobNameList;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BatchConfiguration batchConfiguration;

    public HealthCheckTasklet() {
        this.jobNameList = new ArrayList<>(1);
        jobNameList.add("Aggregation Stat Data");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<String> urlList = generatedFlinkManagerServerApi();

        if (urlList.isEmpty()) {
            return RepeatStatus.FINISHED;
        }

        Map<String, Boolean> jobExecuteStatus = createjobExecuteStatus();

        for (String url : urlList) {
            try {
                ResponseEntity<Map> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, null, Map.class);

                if (responseEntity.getStatusCode() != HttpStatus.OK) {
                    continue;
                }

                checkJobExecuteStatus(responseEntity, jobExecuteStatus);
            } catch (Exception e) {
                logger.error("fail call api to flink server.", e);
            }
        }

        List<String> notExecuteJobList = new ArrayList<>(3);
        for (Map.Entry<String, Boolean> entry : jobExecuteStatus.entrySet()) {
            if (entry.getValue().equals(Boolean.FALSE)) {
                notExecuteJobList.add(entry.getKey());
            }
        }

        if (notExecuteJobList.size() > 0) {
            String exceptionMessage = String.format("job fail : %s", notExecuteJobList);
            throw new Exception(exceptionMessage);
        }

        return RepeatStatus.FINISHED;
    }

    private void checkJobExecuteStatus(ResponseEntity<Map> responseEntity, Map<String, Boolean> jobExecuteStatus) {
        Map<?, ?> responseData = responseEntity.getBody();
        List<?> runningJob = (List<?>)responseData.get("running");

        if (runningJob != null) {
            for (Object job : runningJob) {
                Map<?, ?> jobInfo = (Map<?, ?>)job;
                String jobName = (String) jobInfo.get("name");

                Boolean status = jobExecuteStatus.get(jobName);

                if (status != null) {
                    jobExecuteStatus.put(jobName, true);
                }
            }
        }
    }


    private List<String> generatedFlinkManagerServerApi() {
        List<String> flinkServerList = batchConfiguration.getFlinkServerList();
        List<String> urlList = new ArrayList<>(flinkServerList.size());

        for (String flinkServerIp : flinkServerList) {
            urlList.add(String.format(URL_FORMAT, flinkServerIp));
        }

        return urlList;
    }

    public Map<String, Boolean> createjobExecuteStatus() {
        Map<String, Boolean> jobExecuteStatus = new HashMap<>();

        for (String jobName : jobNameList) {
            jobExecuteStatus.put(jobName, false);
        }

        return jobExecuteStatus;
    }

}
