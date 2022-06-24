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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.TestTraceUtils;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ResponseHistogramsTest {

    private final ServiceTypeRegistryService registry = TestTraceUtils.mockServiceTypeRegistryService();

    @Test
    public void empty() {
        // Given
        final Range range = Range.between(1, 200000);
        final String applicationName = "TEST_APP";
        final ServiceType serviceType = registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE);
        final Application application = new Application(applicationName, serviceType);

        ResponseHistograms.Builder builder = new ResponseHistograms.Builder(range);
        ResponseHistograms responseHistograms = builder.build();

        // When
        List<ResponseTime> responseTimeList = responseHistograms.getResponseTimeList(application);

        // Then
        Assertions.assertTrue(responseTimeList.isEmpty());
    }

    @Test
    public void nonExistentApplication() {
        // Given
        final Range range = Range.between(1, 200000);
        final String applicationName = "TEST_APP";
        final ServiceType serviceType = registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE);
        final Application application = new Application(applicationName, serviceType);
        SpanBo fastSpan = new TestTraceUtils.SpanBuilder(applicationName, "test-app").elapsed(500).build();

        ResponseHistograms.Builder builder = new ResponseHistograms.Builder(range);
        Iterator<Long> timeslotIterator = builder.getWindow().iterator();
        long timeslot = timeslotIterator.next();
        builder.addHistogram(application, fastSpan, timeslot);
        ResponseHistograms responseHistograms = builder.build();

        // When
        final Application nonExistentApplication = new Application(applicationName + "_other", serviceType);
        List<ResponseTime> properResponseTimeList = responseHistograms.getResponseTimeList(application);
        List<ResponseTime> responseTimeList = responseHistograms.getResponseTimeList(nonExistentApplication);

        // Then
        Assertions.assertFalse(properResponseTimeList.isEmpty());
        Assertions.assertTrue(responseTimeList.isEmpty());
    }

    @Test
    public void timeslots() {
        // Given
        final Range range = Range.between(1, 200000);
        final String applicationName = "TEST_APP";
        final ServiceType serviceType = registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE);
        final Application application = new Application(applicationName, serviceType);

        SpanBo fastSpan = new TestTraceUtils.SpanBuilder(applicationName, "test-app").elapsed(500).build();
        SpanBo normalSpan = new TestTraceUtils.SpanBuilder(applicationName, "test-app").elapsed(1500).build();
        SpanBo slowSpan = new TestTraceUtils.SpanBuilder(applicationName, "test-app").elapsed(3500).build();
        SpanBo verySlowSpan = new TestTraceUtils.SpanBuilder(applicationName, "test-app").elapsed(5500).build();
        SpanBo errorSpan = new TestTraceUtils.SpanBuilder(applicationName, "test-app").elapsed(500).errorCode(1).build();

        ResponseHistograms.Builder builder = new ResponseHistograms.Builder(range);
        Iterator<Long> timeslotIterator = builder.getWindow().iterator();
        long timeslot1 = timeslotIterator.next();
        long timeslot2 = timeslotIterator.next();
        long timeslot3 = timeslotIterator.next();
        // timeslot1
        builder.addHistogram(application, fastSpan, timeslot1);
        builder.addHistogram(application, normalSpan, timeslot1);
        //  timeslot2
        builder.addHistogram(application, slowSpan, timeslot2);
        builder.addHistogram(application, verySlowSpan, timeslot2);
        // timeslot3
        builder.addHistogram(application, errorSpan, timeslot3);

        // When
        ResponseHistograms responseHistograms = builder.build();

        // Then
        List<ResponseTime> responseTimeList = responseHistograms.getResponseTimeList(application);
        Assertions.assertNotNull(responseTimeList);
        Assertions.assertEquals(3, responseTimeList.size());
        for (ResponseTime responseTime : responseTimeList) {
            Histogram applicationResponseHistogram = responseTime.getApplicationResponseHistogram();
            long timeslotTimestamp = responseTime.getTimeStamp();
            if (timeslotTimestamp == timeslot1) {
                Assertions.assertEquals(1, applicationResponseHistogram.getFastCount());
                Assertions.assertEquals(1, applicationResponseHistogram.getNormalCount());
                Assertions.assertEquals(2, applicationResponseHistogram.getSuccessCount());
                Assertions.assertEquals(0, applicationResponseHistogram.getTotalErrorCount());
                Assertions.assertEquals(2, applicationResponseHistogram.getTotalCount());
            } else if (timeslotTimestamp == timeslot2) {
                Assertions.assertEquals(1, applicationResponseHistogram.getSlowCount());
                Assertions.assertEquals(1, applicationResponseHistogram.getVerySlowCount());
                Assertions.assertEquals(2, applicationResponseHistogram.getSuccessCount());
                Assertions.assertEquals(0, applicationResponseHistogram.getTotalErrorCount());
                Assertions.assertEquals(2, applicationResponseHistogram.getTotalCount());
            } else if (timeslotTimestamp == timeslot3) {
                Assertions.assertEquals(1, applicationResponseHistogram.getFastErrorCount());
                Assertions.assertEquals(1, applicationResponseHistogram.getTotalErrorCount());
                Assertions.assertEquals(0, applicationResponseHistogram.getSuccessCount());
                Assertions.assertEquals(1, applicationResponseHistogram.getTotalCount());
            } else {
                Assertions.fail("unexpected responseTime in timeslot : " + timeslotTimestamp);
            }
        }
    }

    @Test
    public void multipleAgents() {
        // Given
        final Range range = Range.between(1, 200000);
        final String applicationName = "TEST_APP";
        final ServiceType serviceType = registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE);
        final Application application = new Application(applicationName, serviceType);
        final String fastAgentId = "fast-app";
        final String normalAgentId = "normal-app";
        final String slowAgentId = "slow-app";
        final String verySlowAgentId = "verySlow-app";
        final String errorAgentId = "error-app";

        SpanBo fastSpan = new TestTraceUtils.SpanBuilder(applicationName, fastAgentId).elapsed(500).build();
        SpanBo normalSpan = new TestTraceUtils.SpanBuilder(applicationName, normalAgentId).elapsed(1500).build();
        SpanBo slowSpan = new TestTraceUtils.SpanBuilder(applicationName, slowAgentId).elapsed(3500).build();
        SpanBo verySlowSpan = new TestTraceUtils.SpanBuilder(applicationName, verySlowAgentId).elapsed(5500).build();
        SpanBo errorSpan = new TestTraceUtils.SpanBuilder(applicationName, errorAgentId).elapsed(500).errorCode(1).build();

        ResponseHistograms.Builder builder = new ResponseHistograms.Builder(range);
        Iterator<Long> timeslotIterator = builder.getWindow().iterator();
        long timeslot = timeslotIterator.next();
        builder.addHistogram(application, fastSpan, timeslot);
        builder.addHistogram(application, normalSpan, timeslot);
        builder.addHistogram(application, slowSpan, timeslot);
        builder.addHistogram(application, verySlowSpan, timeslot);
        builder.addHistogram(application, errorSpan, timeslot);

        // When
        ResponseHistograms responseHistograms = builder.build();

        // Then
        List<ResponseTime> responseTimeList = responseHistograms.getResponseTimeList(application);
        Assertions.assertNotNull(responseTimeList);
        Assertions.assertEquals(1, responseTimeList.size());
        ResponseTime responseTime = responseTimeList.get(0);
        Assertions.assertEquals(5, responseTime.getAgentResponseHistogramList().size());

        Histogram fastAgentHistogram = responseTime.findHistogram(fastAgentId);
        Assertions.assertEquals(1, fastAgentHistogram.getFastCount());
        Histogram normalAgentHistogram = responseTime.findHistogram(normalAgentId);
        Assertions.assertEquals(1, normalAgentHistogram.getNormalCount());
        Histogram slowAgentHistogram = responseTime.findHistogram(slowAgentId);
        Assertions.assertEquals(1, slowAgentHistogram.getSlowCount());
        Histogram verySlowAgentHistogram = responseTime.findHistogram(verySlowAgentId);
        Assertions.assertEquals(1, verySlowAgentHistogram.getVerySlowCount());
        Histogram errorAgentHistogram = responseTime.findHistogram(errorAgentId);
        Assertions.assertEquals(1, errorAgentHistogram.getFastErrorCount());
    }

    @Test
    public void multipleApplications() {
        // Given
        final Range range = Range.between(1, 200000);
        final String appAName = "APP_A";
        final ServiceType appAServiceType = registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE);
        final Application appA = new Application(appAName, appAServiceType);
        final String appAAgentId = "app-a";
        final String appBName = "APP_B";
        final ServiceType appBServiceType = registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE);
        final Application appB = new Application(appBName, appBServiceType);
        final String appBAgentId = "app-b";

        SpanBo appASpan = new TestTraceUtils.SpanBuilder(appAName, appAAgentId).elapsed(500).build();
        SpanBo appBSpan = new TestTraceUtils.SpanBuilder(appBName, appBAgentId).elapsed(1500).build();

        ResponseHistograms.Builder builder = new ResponseHistograms.Builder(range);
        Iterator<Long> timeslotIterator = builder.getWindow().iterator();
        long timeslot = timeslotIterator.next();

        builder.addHistogram(appA, appASpan, timeslot);
        builder.addHistogram(appA, appASpan, timeslot);

        builder.addHistogram(appB, appBSpan, timeslot);
        builder.addHistogram(appB, appBSpan, timeslot);
        builder.addHistogram(appB, appBSpan, timeslot);

        // When
        ResponseHistograms responseHistograms = builder.build();

        // Then
        List<ResponseTime> appAResponseTimeList = responseHistograms.getResponseTimeList(appA);
        Assertions.assertEquals(1, appAResponseTimeList.size());
        ResponseTime appAResponseTime = appAResponseTimeList.get(0);
        Histogram appAAgentHistogram = appAResponseTime.findHistogram(appAAgentId);
        Assertions.assertEquals(2, appAAgentHistogram.getFastCount());
        Assertions.assertEquals(2, appAAgentHistogram.getTotalCount());

        List<ResponseTime> appBResponseTimeList = responseHistograms.getResponseTimeList(appB);
        Assertions.assertEquals(1, appBResponseTimeList.size());
        ResponseTime appBResponseTime = appBResponseTimeList.get(0);
        Histogram appBAgentHistogram = appBResponseTime.findHistogram(appBAgentId);
        Assertions.assertEquals(3, appBAgentHistogram.getNormalCount());
        Assertions.assertEquals(3, appBAgentHistogram.getTotalCount());
    }
}
