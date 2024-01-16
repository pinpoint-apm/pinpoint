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
package com.navercorp.pinpoint.log.web.service;

import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.Log;
import com.navercorp.pinpoint.log.vo.LogPile;
import com.navercorp.pinpoint.log.web.dao.LiveTailDao;
import com.navercorp.pinpoint.log.web.vo.LiveTailBatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class LiveTailServiceTest {

    @Mock
    LiveTailDao dao;

    @Test
    public void testTail() {
        FileKey fileKey = FileKey.of("hostGroupName", "hostName", "fileName");
        Log log = new Log(0, 0, "hello");
        LogPile pile = new LogPile(0, List.of(log));

        when(dao.tail(eq(fileKey))).thenReturn(Flux.just(pile));

        List<List<LiveTailBatch>> tail = new LiveTailServiceImpl(dao).tail(List.of(fileKey))
                .take(Duration.ofMillis(100))
                .collectList()
                .block(Duration.ofMillis(100));

        assertThat(tail).hasSize(1);
        assertThat(tail.get(0)).hasSize(1);
        assertThat(tail.get(0).get(0).fileKey()).isEqualTo(fileKey.toString());
        assertThat(tail.get(0).get(0).logs()).hasSize(1);
        assertThat(tail.get(0).get(0).logs().get(0)).isEqualTo(log);
    }

    @Test
    public void testGetHostGroupNames() {
        mockDao();
        Set<String> result = new LiveTailServiceImpl(dao).getHostGroupNames();
        assertThat(result).hasSameElementsAs(List.of("a1", "a2", "a3"));
    }

    @Test
    public void testGetFileKeys() {
        mockDao();

        List<FileKey> result = new LiveTailServiceImpl(dao).getFileKeys("a1");
        assertThat(result).hasSameElementsAs(List.of(
                FileKey.of("a1", "b1", "c1"),
                FileKey.of("a1", "b2", "c2")));
    }

    @Test
    public void testGetFileKeys2() {
        mockDao();

        List<FileKey> result = new LiveTailServiceImpl(dao).getFileKeys(
                "a1",
                List.of("b1", "b2"),
                List.of("c1")
        );

        assertThat(result).hasSameElementsAs(List.of(
                FileKey.of("a1", "b1", "c1")
        ));
    }

    private void mockDao() {
        when(dao.getFileKeys()).thenReturn(List.of(
                FileKey.of("a1", "b1", "c1"),
                FileKey.of("a1", "b2", "c2"),
                FileKey.of("a2", "b1", "c1"),
                FileKey.of("a2", "b2", "c2"),
                FileKey.of("a3", "b1", "c1"),
                FileKey.of("a3", "b2", "c2")));
    }

}
