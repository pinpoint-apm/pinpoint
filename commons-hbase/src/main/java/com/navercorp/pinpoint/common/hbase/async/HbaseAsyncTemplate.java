/*
 * Copyright 2019 NAVER Corp.
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
 */

package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.CasResult;
import com.navercorp.pinpoint.common.hbase.CheckAndMax;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.future.FutureDecorator;
import com.navercorp.pinpoint.common.hbase.future.FutureLoggingDecorator;
import com.navercorp.pinpoint.common.hbase.util.HBaseExceptionUtils;
import com.navercorp.pinpoint.common.hbase.util.MutationType;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AdvancedScanResultConsumer;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ScanResultConsumer;
import org.apache.hbase.thirdparty.com.google.common.util.concurrent.MoreExecutors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author emeroad
 */
public class HbaseAsyncTemplate implements DisposableBean, AsyncHbaseOperations {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AsyncTableFactory asyncTableFactory;
    private final ExecutorService executor;

    private final FutureDecorator futureDecorator = new FutureLoggingDecorator(logger);

    public HbaseAsyncTemplate(AsyncTableFactory asyncTableFactory,
                              ExecutorService executor) {
        this.asyncTableFactory = Objects.requireNonNull(asyncTableFactory, "asyncTableFactory");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public <T> T advancedExecute(TableName tableName, AdvancedAsyncTableCallback<T> action) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(action, "action");

        final AsyncTable<AdvancedScanResultConsumer> table = getAdvancedAsyncTable(tableName);
        try {
            return action.doInTable(table);
        } catch (Throwable e) {
            return HBaseExceptionUtils.rethrowHbaseException(e);
        }
    }

    @Override
    public <T> T execute(TableName tableName, AsyncTableCallback<T> action) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(action, "action");

        final AsyncTable<ScanResultConsumer> table = getAsyncTable(tableName);
        try {
            return action.doInTable(table);
        } catch (Throwable e) {
            return HBaseExceptionUtils.rethrowHbaseException(e);
        }
    }

    @Override
    public CompletableFuture<Void> put(TableName tableName, final Put put) {
        CompletableFuture<Void> future = this.execute(tableName, new AsyncTableCallback<>() {
            @Override
            public CompletableFuture<Void> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                return table.put(put);
            }
        });
        futureDecorator.apply(future, tableName, MutationType.PUT);
        return future;
    }

    @Override
    public List<CompletableFuture<Void>> put(TableName tableName, final List<Put> puts) {
        List<CompletableFuture<Void>> futures = this.execute(tableName, new AsyncTableCallback<>() {
            @Override
            public List<CompletableFuture<Void>> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                return table.put(puts);
            }
        });
        futureDecorator.apply(futures, tableName, MutationType.PUT);
        return futures;
    }

    @Override
    public <T> CompletableFuture<T> get(TableName tableName, final Get get, final RowMapper<T> mapper) {
        CompletableFuture<T> futures = this.execute(tableName, new AsyncTableCallback<>() {
            @Override
            public CompletableFuture<T> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                CompletableFuture<Result> result = table.get(get);
                return result.thenApply(new Function<Result, T>() {
                    @Override
                    public T apply(Result result1) {
                        try {
                            return mapper.mapRow(result1, 0);
                        } catch (Exception e) {
                            return HBaseExceptionUtils.rethrowHbaseException(e);
                        }
                    };
                });
            }
        });
        futureDecorator.apply(futures, tableName, MutationType.GET);
        return futures;
    }


    @Override
    public List<CompletableFuture<Result>> increment(final TableName tableName, final List<Increment> incrementList) {
        List<CompletableFuture<Result>> futures = execute(tableName, new AsyncTableCallback<List<CompletableFuture<Result>>>() {
            @Override
            public List<CompletableFuture<Result>> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                return table.batch(incrementList);
            }
        });
        futureDecorator.apply(futures, tableName, MutationType.INCREMENT);
        return futures;
    }


    @Override
    public CompletableFuture<Result> increment(final TableName tableName, Increment increment) {
        CompletableFuture<Result> future = execute(tableName, new AsyncTableCallback<CompletableFuture<Result>>() {
            @Override
            public CompletableFuture<Result> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                return table.increment(increment);
            }
        });
        futureDecorator.apply(future, tableName, MutationType.INCREMENT);
        return future;
    }


    @Override
    public CompletableFuture<CasResult> maxColumnValue(TableName tableName, CheckAndMax max) {
        CompletableFuture<CasResult> result = this.execute(tableName, new AsyncTableCallback<>() {
            @Override
            public CompletableFuture<CasResult> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                CheckAndMutate checkAndPut = CheckAndMax.initialMax(max);
                CompletableFuture<CheckAndMutateResult> result = table.checkAndMutate(checkAndPut);
                return result.thenCompose(new MaxMutation(() -> {
                    return table.checkAndMutate(CheckAndMax.casMax(checkAndPut));
                }));
            }
        });
        futureDecorator.apply(result, tableName, MutationType.CHECK_AND_MUTATE);
        return result;
    }

    public static class MaxMutation implements Function<CheckAndMutateResult, CompletableFuture<CasResult>> {
        private final Supplier<CompletableFuture<CheckAndMutateResult>> maxMutate;

        public MaxMutation(Supplier<CompletableFuture<CheckAndMutateResult>> maxMutate) {
            this.maxMutate = Objects.requireNonNull(maxMutate, "maxMutate");
        }

        public CompletableFuture<CasResult> apply(CheckAndMutateResult checkAndMutateResult) {
            if (checkAndMutateResult.isSuccess()) {
                return CompletableFuture.completedFuture(CasResult.INITIAL_UPDATE);
            }

            CompletableFuture<CheckAndMutateResult> maxFuture = maxMutate.get();
            return maxFuture.thenApply(CasResult::casResult);
        }

    }

    public List<CompletableFuture<CasResult>> maxColumnValue(TableName tableName, List<CheckAndMax> maxs) {

        List<CheckAndMutate> checkAndMutates = new ArrayList<>(maxs.size());
        for (CheckAndMax max : maxs) {
            CheckAndMutate checkAndPut = CheckAndMax.initialMax(max);
            checkAndMutates.add(checkAndPut);
        }

        Iterator<CheckAndMutate> iterator = checkAndMutates.iterator();
        List<CompletableFuture<CasResult>> casResult = this.execute(tableName, new AsyncTableCallback<>() {
            @Override
            public List<CompletableFuture<CasResult>> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                List<CompletableFuture<CheckAndMutateResult>> results = table.checkAndMutate(checkAndMutates);
                List<CompletableFuture<CasResult>> casResult = new ArrayList<>(results.size());
                for (CompletableFuture<CheckAndMutateResult> future : results) {
                    final CheckAndMutate checkAndMutate = iterator.next();
                    CompletableFuture<CasResult> casFuture = future.thenCompose(new MaxMutation(() -> {
                        return table.checkAndMutate(CheckAndMax.casMax(checkAndMutate));
                    }));
                    casResult.add(casFuture);
                }
                return casResult;
            }
        });
        futureDecorator.apply(casResult, tableName, MutationType.CHECK_AND_MUTATE);
        return casResult;
    }


    @Override
    public List<CompletableFuture<CheckAndMutateResult>> checkAndMutate(TableName tableName, List<CheckAndMutate> checkAndMutates) {
        List<CompletableFuture<CheckAndMutateResult>> futures = execute(tableName, new AsyncTableCallback<>() {
            @Override
            public List<CompletableFuture<CheckAndMutateResult>> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                return table.checkAndMutate(checkAndMutates);
            }
        });
        futureDecorator.apply(futures, tableName, MutationType.CHECK_AND_MUTATE);
        return futures;
    }


    public AsyncTable<ScanResultConsumer> getAsyncTable(TableName tableName) {
        return getAsyncTableFactory().getTable(tableName, executor);
    }

    public AsyncTable<AdvancedScanResultConsumer> getAdvancedAsyncTable(TableName tableName) {
        return getAsyncTableFactory().getTable(tableName);
    }

    public AsyncTableFactory getAsyncTableFactory() {
        return asyncTableFactory;
    }

    @Override
    public void destroy() throws Exception {
        MoreExecutors.shutdownAndAwaitTermination(executor, 3000, TimeUnit.MILLISECONDS);
    }
}
