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
package com.navercorp.pinpoint.log.collector.repository;

import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.LogPile;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class LogConsumerRepositoryTest {

    @Test
    public void testBasicScenario() throws Exception {
        FileKey fileKey1 = FileKey.parse("hostGroup-1:host1.1:file-1.1.1");
        FileKey fileKey2 = FileKey.parse("hostGroup-1:host1.1:file-1.1.2");

        LogConsumer consumer1 = mockLogConsumer(System.out::println, fileKey1);
        LogConsumer consumer2 = mockLogConsumer(System.out::print, fileKey1);
        LogConsumer consumer3 = mockLogConsumer(System.out::print, fileKey2);

        LogConsumerRepository repo = new LogConsumerRepository();
        repo.addConsumer(consumer1);
        repo.addConsumer(consumer2);
        repo.addConsumer(consumer3);

        assertThat(repo.getConsumer(fileKey1)).withFailMessage("should return last item").isEqualTo(consumer2);
        assertThat(repo.getConsumer(fileKey2)).withFailMessage("should return last item").isEqualTo(consumer3);

        repo.removeConsumer(consumer2);
        repo.removeConsumer(consumer3);

        assertThat(repo.getConsumer(fileKey1)).withFailMessage("should return last item").isEqualTo(consumer1);
        assertThat(repo.getConsumer(fileKey2)).withFailMessage("should return null").isNull();
    }

    private static LogConsumer mockLogConsumer(Consumer<LogPile> delegate, FileKey fileKey) {
        return new LogConsumer() {
            @Override
            public void consume(LogPile pile) {
                delegate.accept(pile);
            }

            @Override
            public FileKey getFileKey() {
                return fileKey;
            }
        };
    }

}
