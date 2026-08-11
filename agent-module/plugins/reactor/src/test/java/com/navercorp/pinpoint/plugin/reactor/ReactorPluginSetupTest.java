/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorPluginSetupTest {
    private MatchableTransformTemplate transformTemplate;
    private ProfilerPluginSetupContext context;

    @BeforeEach
    public void setUp() {
        transformTemplate = mock(MatchableTransformTemplate.class);
        context = mock(ProfilerPluginSetupContext.class);
    }

    @Test
    public void periodicSchedulerTasksAreNotRegisteredByDefault() {
        setup(new Properties());

        verifyPeriodicTransformsNeverRegistered();
        // Flux.interval has its own long-standing RunnableSubscription path. S2-1 changes only
        // the scheduler carrier policy and must not silently disable that operator support.
        verify(transformTemplate).transform("reactor.core.publisher.FluxInterval$IntervalRunnable", ReactorPlugin.RunnableSubscriptionTransform.class);
    }

    @Test
    public void periodicSchedulerTasksUseIndependentTransactionTransformWhenEnabled() {
        Properties properties = new Properties();
        properties.put("profiler.reactor.trace.scheduler.task.periodic", "true");

        setup(properties);

        verify(transformTemplate).transform("reactor.core.scheduler.PeriodicSchedulerTask", ReactorPlugin.PeriodicSchedulerTaskTransform.class);
        verify(transformTemplate).transform("reactor.core.scheduler.PeriodicWorkerTask", ReactorPlugin.PeriodicSchedulerTaskTransform.class);
        verify(transformTemplate).transform("reactor.core.scheduler.InstantPeriodicWorkerTask", ReactorPlugin.PeriodicSchedulerTaskTransform.class);
        verify(transformTemplate, never()).transform("reactor.core.scheduler.PeriodicSchedulerTask", ReactorPlugin.SchedulerTaskTransform.class);
    }

    @Test
    public void retrySupportTargetsOnlyTheTwoSourceResubscribers() {
        setup(new Properties());

        verify(transformTemplate).transform("reactor.core.publisher.FluxRetry$RetrySubscriber", ReactorPlugin.RetrySubscriberTransform.class);
        verify(transformTemplate).transform("reactor.core.publisher.FluxRetryWhen$RetryWhenMainSubscriber", ReactorPlugin.RetrySubscriberTransform.class);
        verify(transformTemplate, never()).transform("reactor.core.publisher.FluxRetryWhen$RetryWhenOtherSubscriber", ReactorPlugin.RetrySubscriberTransform.class);
    }

    private void setup(Properties properties) {
        when(context.getConfig()).thenReturn(ProfilerConfigLoader.load(properties));
        ReactorPlugin plugin = new ReactorPlugin();
        plugin.setTransformTemplate(transformTemplate);
        plugin.setup(context);
    }

    private void verifyPeriodicTransformsNeverRegistered() {
        verify(transformTemplate, never()).transform("reactor.core.scheduler.PeriodicSchedulerTask", ReactorPlugin.SchedulerTaskTransform.class);
        verify(transformTemplate, never()).transform("reactor.core.scheduler.PeriodicSchedulerTask", ReactorPlugin.PeriodicSchedulerTaskTransform.class);
        verify(transformTemplate, never()).transform("reactor.core.scheduler.PeriodicWorkerTask", ReactorPlugin.SchedulerTaskTransform.class);
        verify(transformTemplate, never()).transform("reactor.core.scheduler.PeriodicWorkerTask", ReactorPlugin.PeriodicSchedulerTaskTransform.class);
        verify(transformTemplate, never()).transform("reactor.core.scheduler.InstantPeriodicWorkerTask", ReactorPlugin.SchedulerTaskTransform.class);
        verify(transformTemplate, never()).transform("reactor.core.scheduler.InstantPeriodicWorkerTask", ReactorPlugin.PeriodicSchedulerTaskTransform.class);
    }
}
