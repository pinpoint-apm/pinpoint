/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HbasePutWriter {

    /**
     * If asyncOperation is not set, then execute put method instead of asyncPut method.
     */
    CompletableFuture<Void> put(TableName tableName, final Put put);

    /**
     * If asyncOperation is not set, then execute put method instead of asyncPut method.
     *
     * @return the list of puts which could not be queued or put serially. May be null if all put operations were successfully queued.
     */
    List<CompletableFuture<Void>> put(TableName tableName, final List<Put> puts);

}
