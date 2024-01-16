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
import com.navercorp.pinpoint.log.vo.LogPile;
import com.navercorp.pinpoint.log.web.dao.LiveTailDao;
import com.navercorp.pinpoint.log.web.vo.LiveTailBatch;
import com.navercorp.pinpoint.web.util.ListListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author youngjin.kim2
 */
public class LiveTailServiceImpl implements LiveTailService {

    private final Logger logger = LogManager.getLogger(LiveTailServiceImpl.class);

    private final LiveTailDao dao;

    private final Supplier<Map<String, List<FileKey>>> fileKeyMapSupplier;


    public LiveTailServiceImpl(LiveTailDao dao) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.fileKeyMapSupplier = new LogFileKeyMapSupplier(this.dao);
    }

    @Override
    public Flux<List<LiveTailBatch>> tail(List<FileKey> fileKeys) {
        return this.tail0(fileKeys)
                .window(Duration.ofMillis(200))
                .flatMap(window -> window
                        .groupBy(el -> el.source().toString())
                        .flatMap(group -> group
                                .map(el -> el.pile().getLogs())
                                .collectList()
                                .map(el -> new LiveTailBatch(group.key(), ListListUtils.toList(el)))
                        )
                        .collectList()
                )
                .filter(el -> !el.isEmpty());
    }

    private Flux<LogPileWithSource> tail0(List<FileKey> fileKeys) {
        List<Flux<LogPileWithSource>> fluxes = new ArrayList<>(fileKeys.size());
        for (FileKey augmented: fileKeys) {
            Flux<LogPileWithSource> tail = tail0(augmented);
            if (tail != null) {
                fluxes.add(tail);
            }
        }
        return Flux.merge(fluxes);
    }

    private Flux<LogPileWithSource> tail0(FileKey fileKey) {
        try {
            logger.debug("Requesting tail of {}", fileKey);
            return this.dao.tail(fileKey).map(pile -> new LogPileWithSource(fileKey, pile));
        } catch (Exception e) {
            logger.error("Failed to tail {}", fileKey, e);
            return null;
        }
    }

    @Override
    public Set<String> getHostGroupNames() {
        return this.getFileKeyMap().keySet();
    }

    @Override
    public List<FileKey> getFileKeys(String hostGroupName) {
        return Objects.requireNonNullElse(this.getFileKeyMap().get(hostGroupName), List.of());
    }

    @Override
    public List<FileKey> getFileKeys(String hostGroupName, List<String> hostNames, List<String> fileNames) {
        Objects.requireNonNull(hostGroupName, "hostGroupName");

        List<FileKey> fileKeys = this.getFileKeys(hostGroupName);
        List<FileKey> result = new ArrayList<>(fileKeys.size());
        for (FileKey candidate: fileKeys) {
            if (hostNames != null && !hostNames.contains(candidate.getHostKey().getHostName())) {
                continue;
            }

            if (fileNames != null && !fileNames.contains(candidate.getFileName())) {
                continue;
            }

            result.add(candidate);
        }
        return result;
    }

    private Map<String, List<FileKey>> getFileKeyMap() {
        return this.fileKeyMapSupplier.get();
    }

    private record LogPileWithSource(FileKey source, LogPile pile) {
    }

}
