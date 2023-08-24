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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class LogAcceptorRepositoryTest {

    @Test
    public void testBasicScenario() throws Exception {
        LogDemandAcceptor acceptor1 = System.out::println;
        LogDemandAcceptor acceptor2 = System.out::print;

        LogAcceptorRepository repo = new LogAcceptorRepository();
        repo.addAcceptor(FileKey.parse("hostGroup-1:host1.1:file-1.1.1"), acceptor1);
        repo.addAcceptor(FileKey.parse("hostGroup-1:host1.1:file-1.1.1"), acceptor2);
        repo.addAcceptor(FileKey.parse("hostGroup-1:host1.1:file-1.1.2"), acceptor1);

        assertThat(repo.getAcceptors(FileKey.parse("hostGroup-1:host1.1:file-1.1.1")))
                .withFailMessage("should return 2 items")
                .hasSameElementsAs(List.of(acceptor1, acceptor2));

        assertThat(repo.getAcceptableKeys())
                .withFailMessage("should return 2 keys")
                .hasSameElementsAs(List.of(
                        FileKey.parse("hostGroup-1:host1.1:file-1.1.1"),
                        FileKey.parse("hostGroup-1:host1.1:file-1.1.2")
                ));

        repo.removeAcceptor(FileKey.parse("hostGroup-1:host1.1:file-1.1.2"), acceptor1);

        assertThat(repo.getAcceptableKeys())
                .withFailMessage("file-1.1.2 item item should have been removed")
                .hasSameElementsAs(List.of(
                        FileKey.parse("hostGroup-1:host1.1:file-1.1.1")
                ));
    }

}
