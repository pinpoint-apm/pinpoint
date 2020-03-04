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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PingStreamContextTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void test() throws InterruptedException, ExecutionException, TimeoutException {
        SettableFuture<Object> future = SettableFuture.create();
        boolean done = future.isDone();
        logger.debug("done:{}", done);

    }


    @Test
    public void test2() throws InterruptedException, ExecutionException, TimeoutException {
        SettableFuture<Object> future = SettableFuture.create();
        boolean done = future.isDone();
        logger.debug("future done:{}", future.isDone());
        SettableFuture<Object> future2 = SettableFuture.create();
        future2.setFuture(future);
        logger.debug("future2 done:{}", future2.isDone());

        boolean timeout = future2.setException(new RuntimeException("timeout"));
        logger.debug("timeout:{}", timeout);

    }

}