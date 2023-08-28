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

import com.navercorp.pinpoint.log.dto.LogDemand;
import com.navercorp.pinpoint.log.vo.FileKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class LogAcceptorRepositoryTest {

    @Test
    public void testBasicScenario() throws Exception {
        FileKey fileKey1 = FileKey.parse("hostGroup-1:host1.1:file-1.1.1");
        FileKey fileKey2 = FileKey.parse("hostGroup-1:host1.1:file-1.1.2");

        LogDemandAcceptor acceptor1 = mockAcceptor(System.out::println, fileKey1);
        LogDemandAcceptor acceptor2 = mockAcceptor(System.out::println, fileKey1);
        LogDemandAcceptor acceptor3 = mockAcceptor(System.out::println, fileKey2);

        LogAcceptorRepository repo = new LogAcceptorRepository();
        repo.addAcceptor(acceptor1);
        repo.addAcceptor(acceptor2);
        repo.addAcceptor(acceptor3);

        assertThat(repo.getAcceptors(fileKey1))
                .withFailMessage("should return 2 items")
                .hasSameElementsAs(List.of(acceptor1, acceptor2));

        assertThat(repo.getAcceptableKeys())
                .withFailMessage("should return 2 keys")
                .hasSameElementsAs(List.of(fileKey1, fileKey2));

        repo.removeAcceptor(acceptor3);

        assertThat(repo.getAcceptableKeys())
                .withFailMessage("file-1.1.2 item item should have been removed")
                .hasSameElementsAs(List.of(fileKey1));
    }

    @NotNull
    private static LogDemandAcceptor mockAcceptor(Consumer<LogDemand> delegate, FileKey fileKey) {
        return new LogDemandAcceptor() {
            @Override
            public void accept(LogDemand demand) {
                delegate.accept(demand);
            }

            @Override
            public FileKey getFileKey() {
                return fileKey;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

        };
    }

}
