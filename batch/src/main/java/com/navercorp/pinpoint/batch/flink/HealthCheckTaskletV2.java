/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.batch.common.BatchProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class HealthCheckTaskletV2 implements Tasklet {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final static String URL_FORMAT = "http://%s:%d/jobs/overview";
    private final static String NAME = "name";
    private final static String STATE = "state";
    private final static String RUNNING = "RUNNING";
    private final List<String> jobNameList;

    private final RestTemplate restTemplate;

    private final BatchProperties batchProperties;

    public HealthCheckTaskletV2(BatchProperties batchProperties, RestTemplate restTemplate) {
        this.jobNameList = new ArrayList<>(1);
        jobNameList.add("Aggregation Stat Data");

        this.batchProperties = Objects.requireNonNull(batchProperties, "batchProperties");
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate");
    }

    @Override
    public RepeatStatus execute(@Nonnull StepContribution contribution, @Nonnull ChunkContext chunkContext) throws Exception {
        List<String> urlList = generatedFlinkManagerServerApi();

        if (urlList.isEmpty()) {
            return RepeatStatus.FINISHED;
        }

        Map<String, Boolean> jobExecuteStatus = createjobExecuteStatus();

        for (String url : urlList) {
            try {
                ParameterizedTypeReference<Map<?, ?>> type = new ParameterizedTypeReference<>() {};
                ResponseEntity<Map<?, ?>> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, null, type);

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

    private void checkJobExecuteStatus(ResponseEntity<Map<?, ?>> responseEntity, Map<String, Boolean> jobExecuteStatus) {
        Map<?, ?> responseData = responseEntity.getBody();
        if(responseData != null) {
            List<?> jobs = (List<?>)responseData.get("jobs");

            if (jobs != null) {
                for (Object job : jobs) {
                    Map<?, ?> jobInfo = (Map<?, ?>)job;
                    final String jobName = (String) jobInfo.get(NAME);
                    if (jobExecuteStatus.containsKey(jobName)) {
                        if (RUNNING.equals(jobInfo.get(STATE))) {
                            jobExecuteStatus.put(jobName, true);
                        }
                    }
                }
            }
        }
    }

    // @VisibleForTesting
    List<String> generatedFlinkManagerServerApi() {
        List<String> flinkServerList = batchProperties.getFlinkServerList();
        int flinkRestPort = batchProperties.getFlinkRestPort();
        List<String> urlList = new ArrayList<>(flinkServerList.size());

        for (String flinkServerIp : flinkServerList) {
            urlList.add(String.format(URL_FORMAT, flinkServerIp, flinkRestPort));
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
