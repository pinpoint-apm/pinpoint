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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class LogAcceptorRepository {

    private final SetMultimap<FileKey, LogDemandAcceptor> acceptors = Multimaps.synchronizedSetMultimap(
            Multimaps.newSetMultimap(new HashMap<>(1024), () -> new LinkedHashSet<>(2))
    );

    public Set<FileKey> getAcceptableKeys() {
        return this.acceptors.keySet();
    }

    public Set<LogDemandAcceptor> getAcceptors(FileKey key) {
        return this.acceptors.get(key);
    }

    public void addAcceptor(FileKey key, LogDemandAcceptor acceptor) {
        this.acceptors.put(key, acceptor);
    }

    public void removeAcceptor(FileKey key, LogDemandAcceptor acceptor) {
        this.acceptors.remove(key, acceptor);
    }

}
