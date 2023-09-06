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
package com.navercorp.pinpoint.log.collector.service;

import com.navercorp.pinpoint.log.collector.repository.LogAcceptorRepository;
import com.navercorp.pinpoint.log.collector.repository.LogConsumerRepository;
import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.Log;
import com.navercorp.pinpoint.log.vo.LogPile;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class LogServiceTest {

    @Test
    public void test() throws Exception {
        LogAcceptorRepository acceptorRepository = new LogAcceptorRepository();
        LogConsumerRepository consumerRepository = new LogConsumerRepository();

        LogConsumerService consumerService =
                new LogConsumerServiceImpl(Schedulers.boundedElastic(), acceptorRepository, consumerRepository);
        LogProviderService providerService =
                new LogProviderServiceImpl(acceptorRepository, consumerRepository);

        FileKey fileKey = FileKey.parse("hostGroup-1:host-1:file-1");
        Log log1 = new Log(0, 0, "log1");
        Log log2 = new Log(1, 0, "log2");
        LogPile pile1 = new LogPile(0, List.of(log1, log2));

        Disposable providerDisposable = providerService.getDemands(fileKey).subscribe(demand -> {
            providerService.provide(fileKey, pile1);
        });

        assertThat(consumerService.getFileKeys()).hasSameElementsAs(List.of(fileKey));
        assertThat(consumerService.tail(fileKey, Duration.ofMillis(10))
                .collectList()
                .block()
        ).hasSize(1).hasSameElementsAs(List.of(pile1));

        providerDisposable.dispose();

        assertThat(consumerService.getFileKeys()).isNotNull().isEmpty();
        assertThat(consumerService.tail(fileKey, Duration.ofMillis(10))
                .collectList()
                .block()
        ).isNotNull().isEmpty();
    }

}
