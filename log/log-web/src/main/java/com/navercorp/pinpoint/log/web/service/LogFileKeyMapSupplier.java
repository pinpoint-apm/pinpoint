/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.log.web.service;

import com.google.common.base.Suppliers;
import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.web.dao.LiveTailDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author youngjin.kim2
 */
public class LogFileKeyMapSupplier implements Supplier<Map<String, List<FileKey>>> {

    private final LiveTailDao dao;

    private final Supplier<Map<String, List<FileKey>>> fileKeyMapSupplier =
            Suppliers.memoizeWithExpiration(this::buildFileKeyMap, 10, TimeUnit.SECONDS);

    public LogFileKeyMapSupplier(LiveTailDao dao) {
        this.dao = Objects.requireNonNull(dao, "dao");
    }

    @Override
    public Map<String, List<FileKey>> get() {
        return this.fileKeyMapSupplier.get();
    }

    private Map<String, List<FileKey>> buildFileKeyMap() {
        return groupByHostGroupName(this.dao.getFileKeys());
    }

    private Map<String, List<FileKey>> groupByHostGroupName(List<FileKey> fileKeys) {
        Map<String, List<FileKey>> result = new HashMap<>(fileKeys.size() * 3);
        for (FileKey fileKey: fileKeys) {
            result.computeIfAbsent(fileKey.getHostKey().getHostGroupName(), k -> new ArrayList<>(2)).add(fileKey);
        }
        return result;
    }

}
