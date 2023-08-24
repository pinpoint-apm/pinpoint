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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class LogConsumerRepositoryTest {

    @Test
    public void testBasicScenario() throws Exception {
        LogConsumer consumer1 = System.out::println;
        LogConsumer consumer2 = System.out::print;

        LogConsumerRepository repo = new LogConsumerRepository();
        repo.addConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.1"), consumer1);
        repo.addConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.1"), consumer2);
        repo.addConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.2"), consumer1);

        assertThat(repo.getConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.1")))
                .withFailMessage("should return last item")
                .isEqualTo(consumer2);
        assertThat(repo.getConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.2")))
                .withFailMessage("should return last item")
                .isEqualTo(consumer1);

        repo.removeConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.1"), consumer2);
        repo.removeConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.2"), consumer1);

        assertThat(repo.getConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.1")))
                .withFailMessage("should return last item")
                .isEqualTo(consumer1);
        assertThat(repo.getConsumer(FileKey.parse("hostGroup-1:host1.1:file-1.1.2")))
                .withFailMessage("should return last item")
                .isNull();
    }

}
