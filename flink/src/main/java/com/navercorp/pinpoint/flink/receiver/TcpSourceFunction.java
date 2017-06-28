/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.flink.receiver;

import com.navercorp.pinpoint.flink.Bootstrap;
import com.navercorp.pinpoint.flink.cluster.FlinkServerRegister;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author minwoo.jung
 */
public class TcpSourceFunction implements ParallelSourceFunction<TBase> {

    private final Logger logger = LoggerFactory.getLogger(TcpSourceFunction.class);
    private FlinkServerRegister flinkServerRegister;
    private TCPReceiver tcpReceiver;

    @Override
    public void run(SourceContext<TBase> ctx) throws Exception {
        final Bootstrap bootstrap = Bootstrap.getInstance();
        bootstrap.setStatHandlerTcpDispatchHandler(ctx);
        bootstrap.initFlinkServerRegister();
        tcpReceiver = bootstrap.initTcpReceiver();

        Thread.sleep(Long.MAX_VALUE);
    }

    @Override
    public void cancel() {
        logger.info("cancel TcpSourceFunction.");

        if (flinkServerRegister != null) {
            flinkServerRegister.stop();
        }
        if (tcpReceiver != null) {
            tcpReceiver.stop();
        }

        ApplicationContext applicationContext = Bootstrap.getInstance().getApplicationContext();
        if (applicationContext != null) {
            ((ConfigurableApplicationContext) applicationContext).close();
        }
    }
}