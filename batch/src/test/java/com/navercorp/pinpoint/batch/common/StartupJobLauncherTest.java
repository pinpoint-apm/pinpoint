/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.batch.common;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.context.SmartLifecycle;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verifyNoInteractions;

class StartupJobLauncherTest {

    private final BatchJobLauncher batchJobLauncher = mock(BatchJobLauncher.class);

    @Test
    void jobsAreLaunchedByStartInOrder() {
        StartupJobLauncher launcher = new StartupJobLauncher(batchJobLauncher, List.of("job1", "job2"));

        // not launched by the constructor
        verifyNoInteractions(batchJobLauncher);
        assertThat(launcher.isRunning()).isFalse();
        assertThat(launcher.isAutoStartup()).isTrue();
        assertThat(launcher.getPhase()).isEqualTo(SmartLifecycle.DEFAULT_PHASE);

        launcher.start();
        try {
            assertThat(launcher.isRunning()).isTrue();
            InOrder inOrder = inOrder(batchJobLauncher);
            inOrder.verify(batchJobLauncher, timeout(3000)).run(eq("job1"), any(JobParameters.class));
            inOrder.verify(batchJobLauncher, timeout(3000)).run(eq("job2"), any(JobParameters.class));
        } finally {
            launcher.stop();
        }
        assertThat(launcher.isRunning()).isFalse();
    }

    @Test
    void nothingIsStartedWithoutJobs() {
        StartupJobLauncher launcher = new StartupJobLauncher(batchJobLauncher, List.of());

        launcher.start();

        assertThat(launcher.isRunning()).isFalse();
        verifyNoInteractions(batchJobLauncher);
        launcher.stop();
    }
}
