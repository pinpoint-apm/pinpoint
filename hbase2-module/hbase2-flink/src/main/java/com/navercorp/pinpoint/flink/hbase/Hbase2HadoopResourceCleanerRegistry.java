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
package com.navercorp.pinpoint.flink.hbase;

import com.navercorp.pinpoint.common.hbase.HadoopResourceCleanerRegistry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ipc.AbstractRpcClient;
import org.apache.hadoop.hbase.ipc.NettyRpcClientConfigHelper;
import org.apache.hadoop.hbase.shaded.io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.hbase.thirdparty.io.netty.buffer.PoolArenaCleaner;
import org.apache.hbase.thirdparty.io.netty.channel.EventLoopGroup;
import org.apache.hbase.thirdparty.io.netty.channel.nio.NioEventLoopGroup;
import org.apache.hbase.thirdparty.io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.hbase.thirdparty.io.netty.util.HashedWheelTimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;

/**
 * @author youngjin.kim2
 */
public class Hbase2HadoopResourceCleanerRegistry implements HadoopResourceCleanerRegistry, DisposableBean, InitializingBean {

    private static final Logger logger = LogManager.getLogger(Hbase2HadoopResourceCleanerRegistry.class);

    private boolean cleanSelf = true;
    private EventLoopGroup eventLoopGroup;

    @Override
    public void register(Configuration configuration) {
        logger.info("Modified netty rpc client to use custom eventLoopGroup");
        NettyRpcClientConfigHelper.setEventLoopConfig(
                configuration,
                this.eventLoopGroup,
                NioSocketChannel.class
        );
        this.cleanSelf = false;
    }

    @Override
    public void clean() {
        logger.info("Cleaning hbase2 hadoop resource");
        closeIdleConnSweeper();
        closeWheelTimer();
        closeEventLoopGroup();
        PoolArenaCleaner.finalizeExplicitly();
    }

    @Override
    public void afterPropertiesSet() {
        logger.info("Creating eventLoopGroup");

        final DefaultThreadFactory eventLoopThreadFactory =
                new DefaultThreadFactory("Pinpoint-RPCClient-NioEventLoopGroup", true, Thread.NORM_PRIORITY);

        this.eventLoopGroup = new NioEventLoopGroup(6, eventLoopThreadFactory);

        logger.info("Created eventLoopGroup for netty-rpc-client");
    }

    @Override
    public void destroy() {
        if (this.cleanSelf) {
            logger.info("Closing custom eventLoopGroup for netty-rpc-client");
            this.closeEventLoopGroup();
        } else {
            logger.info("Skipped closing custom eventLoopGroup for netty-rpc-client, any other must clean it");
        }
    }

    private void closeEventLoopGroup() {
        if (this.eventLoopGroup != null) {
            logger.info("Shutdown eventLoopGroup gracefully");
            this.eventLoopGroup.shutdownGracefully();
        } else {
            logger.warn("Failed to shutdown eventLoopGroup: eventLoopGroup is null");
        }
    }

    private static void closeIdleConnSweeper() {
        try {
            logger.info("Shutdown idleConnSweeper");
            getStaticIdleConnSweeper().shutdown();
        } catch (Exception e) {
            logger.warn("Failed to shutdown idleConnSweeper", e);
        }
    }

    private static ExecutorService getStaticIdleConnSweeper() throws Exception {
        final Field sweeperField = AbstractRpcClient.class.getDeclaredField("IDLE_CONN_SWEEPER");
        sweeperField.setAccessible(true);
        final Object sweeperObj = sweeperField.get(null);
        if (sweeperObj instanceof ExecutorService) {
            return ((ExecutorService) sweeperObj);
        } else {
            throw new RuntimeException("idleConnSweeper not found");
        }
    }

    private static void closeWheelTimer() {
        try {
            logger.info("Stopping wheelTimer");
            getStaticWheelTimer().stop();
        } catch (Exception e) {
            logger.warn("Failed to stop wheelTimer", e);
        }
    }

    private static HashedWheelTimer getStaticWheelTimer() throws Exception {
        final Field wheelTimerField = AbstractRpcClient.class.getDeclaredField("WHEEL_TIMER");
        wheelTimerField.setAccessible(true);
        final Object wheelTimerObj = wheelTimerField.get(null);
        if (wheelTimerObj instanceof HashedWheelTimer) {
            return ((HashedWheelTimer) wheelTimerObj);
        } else {
            throw new RuntimeException("wheelTimer not found");
        }
    }

}
