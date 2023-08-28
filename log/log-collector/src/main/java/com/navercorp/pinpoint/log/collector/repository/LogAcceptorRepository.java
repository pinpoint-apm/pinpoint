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

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.navercorp.pinpoint.log.vo.FileKey;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author youngjin.kim2
 */
public class LogAcceptorRepository {

    private static final long CLEAN_PERIOD_MILLIS = Duration.ofSeconds(60).toMillis();

    private final AtomicLong cleanedAtAtom = new AtomicLong(System.currentTimeMillis());
    private final Executor cleaner = Executors.newSingleThreadExecutor(r -> new Thread(r, "log-acceptor-cleaner"));

    private final SetMultimap<FileKey, LogDemandAcceptor> acceptors = Multimaps.synchronizedSetMultimap(
            Multimaps.newSetMultimap(new HashMap<>(1024), () -> new LinkedHashSet<>(2))
    );

    public Set<FileKey> getAcceptableKeys() {
        Set<FileKey> keys = this.acceptors.keySet();
        if (this.isTimeToClean()) {
            this.cleaner.execute(this::cleanDirtyAcceptors);
        }
        return keys;
    }

    private void cleanDirtyAcceptors() {
        List<LogDemandAcceptor> dirtyAcceptors = getDirtyAcceptors();
        for (LogDemandAcceptor acceptor: dirtyAcceptors) {
            this.removeAcceptor(acceptor);
        }
    }

    private boolean isTimeToClean() {
        long cleanedAt = cleanedAtAtom.get();
        long now = System.currentTimeMillis();
        return cleanedAt + CLEAN_PERIOD_MILLIS < now && cleanedAtAtom.compareAndSet(cleanedAt, now);
    }

    private List<LogDemandAcceptor> getDirtyAcceptors() {
        List<LogDemandAcceptor> dirtyAcceptors = new ArrayList<>(4);
        for (Map.Entry<FileKey, LogDemandAcceptor> entry: this.acceptors.entries()) {
            if (!entry.getValue().isAvailable()) {
                dirtyAcceptors.add(entry.getValue());
            }
        }
        return dirtyAcceptors;
    }

    public Set<LogDemandAcceptor> getAcceptors(FileKey key) {
        return this.acceptors.get(key);
    }

    public void addAcceptor(LogDemandAcceptor acceptor) {
        this.acceptors.put(acceptor.getFileKey(), acceptor);
    }

    public void removeAcceptor(LogDemandAcceptor acceptor) {
        this.acceptors.remove(acceptor.getFileKey(), acceptor);
    }

}
