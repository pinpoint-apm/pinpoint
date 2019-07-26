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

package com.navercorp.pinpoint.grpc;

import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ManagedChannelUtils {
    public static final long DEFAULT_SHUTDOWN_TIMEOUT = 3000;

    private static final Logger logger = LoggerFactory.getLogger(ExecutorUtils.class.getName());

    private ManagedChannelUtils() {
    }

    public static boolean shutdownManagedChannel(String name, ManagedChannel managedChannel) {
        return shutdownManagedChannel(name, managedChannel, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static boolean shutdownManagedChannel(String name, ManagedChannel managedChannel, long timeout, TimeUnit unit) {
        if (managedChannel == null) {
            return false;
        }
        logger.debug("shutdown {}", name);
        managedChannel.shutdown();
        try {
            final boolean success = managedChannel.awaitTermination(timeout, unit);
            if (!success) {
                logger.warn("shutdown timeout {}", name);
            }
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
