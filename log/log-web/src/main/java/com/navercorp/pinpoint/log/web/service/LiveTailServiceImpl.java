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
import java.util.Objects;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class LiveTailServiceImpl implements LiveTailService {

    private final Logger logger = LogManager.getLogger(LiveTailServiceImpl.class);

    private final LiveTailDao dao;

    public LiveTailServiceImpl(LiveTailDao dao) {
        this.dao = Objects.requireNonNull(dao, "dao");
    }

    @Override
    public Flux<List<LiveTailBatch>> tail(FileKey fileKey) {
        return this.tail0(this.getAugmentedFileKeys(fileKey))
                .window(Duration.ofMillis(200))
                .flatMap(window -> window
                        .groupBy(el -> el.getSource().toString())
                        .flatMap(group -> group
                                .map(el -> el.getPile().getLogs())
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
        return this.dao.getHostGroupNames();
    }

    @Override
    public List<FileKey> getFileKeys(String hostGroupName) {
        return this.dao.getFileKeys(hostGroupName);
    }

    private List<FileKey> getAugmentedFileKeys(FileKey fileKey) {
        Objects.requireNonNull(fileKey.getHostKey(), "HostKey is required");

        String hostGroupName = Objects.requireNonNull(fileKey.getHostKey().getHostGroupName(), "HostKey is required");
        String hostName = fileKey.getHostKey().getHostName();
        String fileName = fileKey.getFileName();

        if (hostName != null && fileName != null) {
            return List.of(fileKey);
        }

        List<FileKey> children = getFileKeys(hostGroupName);
        List<FileKey> result = new ArrayList<>(children.size());
        for (FileKey candidate: children) {
            if (hostName != null && !candidate.getHostKey().getHostName().equals(hostName)) {
                continue;
            }

            if (fileName != null && !candidate.getFileName().equals(fileName)) {
                continue;
            }

            result.add(candidate);
        }
        return result;
    }

    private static class LogPileWithSource {
        private final FileKey source;
        private final LogPile pile;

        public LogPileWithSource(FileKey source, LogPile pile) {
            this.source = source;
            this.pile = pile;
        }

        public FileKey getSource() {
            return source;
        }

        public LogPile getPile() {
            return pile;
        }
    }

}
