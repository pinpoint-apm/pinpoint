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
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class LiveTailServiceImpl implements LiveTailService {

    private final LiveTailDao dao;

    public LiveTailServiceImpl(LiveTailDao dao) {
        this.dao = Objects.requireNonNull(dao, "dao");
    }

    @Override
    public Flux<LogPile> tail(FileKey fileKey) {
        return this.dao.tail(fileKey);
    }

    @Override
    public Set<String> getHostGroupNames() {
        return this.dao.getHostGroupNames();
    }

    @Override
    public List<FileKey> getFileKeys(String hostGroupName) {
        return this.dao.getFileKeys(hostGroupName);
    }

}
