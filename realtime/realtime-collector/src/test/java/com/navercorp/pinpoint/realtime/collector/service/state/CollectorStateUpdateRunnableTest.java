/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.collector.service.state;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPoint;
import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPointLocator;
import com.navercorp.pinpoint.realtime.vo.CollectorState;
import com.navercorp.pinpoint.realtime.vo.ProfilerDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class CollectorStateUpdateRunnableTest {

    @Mock ClusterPointLocator locator;
    @Mock CollectorStatePublisherService dao;

    @Test
    public void test() {
        ClusterPoint point0 = mockClusterPoint(ClusterKey.parse("application-name:agent-id:0"));
        ClusterPoint point1 = mockClusterPoint(ClusterKey.parse("application-name:agent-id:1"));
        ClusterPoint point2 = mockClusterPoint(ClusterKey.parse("application-name:agent-id:2"));
        List<ClusterPoint> points = List.of(point0, point1, point2);
        doReturn(points).when(locator).getClusterPointList();

        AtomicReference<CollectorState> stateRef = new AtomicReference<>();
        doAnswer(inv -> {
            stateRef.set(inv.getArgument(0, CollectorState.class));
            return null;
        }).when(dao).publish(any());

        CollectorStateUpdateRunnable r = new CollectorStateUpdateRunnable(locator, dao);
        r.run();

        List<ProfilerDescription> profilers = stateRef.get().getProfilers();
        for (int i = 0; i < 3; i++) {
            assertThat(profilers.get(i).getClusterKey()).isEqualTo(ClusterKey.parse("application-name:agent-id:" + i));
        }
    }

    private static ClusterPoint mockClusterPoint(ClusterKey clusterKey) {
        return new ClusterPoint() {
            @Override
            public ClusterKey getClusterKey() {
                return clusterKey;
            }
        };
    }

}
