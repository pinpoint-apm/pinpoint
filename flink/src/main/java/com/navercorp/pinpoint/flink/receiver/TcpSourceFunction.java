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
import com.navercorp.pinpoint.flink.vo.RawData;
import org.apache.flink.api.common.ExecutionConfig.GlobalJobParameters;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author minwoo.jung
 */
public class TcpSourceFunction extends RichParallelSourceFunction<RawData> {

    private final Logger logger = LoggerFactory.getLogger(TcpSourceFunction.class);
    private transient GlobalJobParameters globalJobParameters;


    @Override
    public void open(Configuration parameters) throws Exception {
        globalJobParameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
    }

    @Override
    public void run(SourceContext<RawData> ctx) throws Exception {
        final Bootstrap bootstrap = Bootstrap.getInstance(globalJobParameters.toMap());
        bootstrap.setStatHandlerTcpDispatchHandler(ctx);
        bootstrap.initFlinkServerRegister();
        bootstrap.initTcpReceiver();

        Thread.sleep(Long.MAX_VALUE);
    }

    @Override
    public void cancel() {
        logger.info("cancel TcpSourceFunction.");

        ApplicationContext applicationContext = Bootstrap.getInstance(globalJobParameters.toMap()).getApplicationContext();
        if (applicationContext != null) {
            ((ConfigurableApplicationContext) applicationContext).close();
        }
    }
}