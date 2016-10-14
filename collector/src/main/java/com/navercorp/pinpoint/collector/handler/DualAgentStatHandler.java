/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HyunGil Jeong
 */
public class DualAgentStatHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Handler master;
    private final Handler slave;

    DualAgentStatHandler(Handler master, Handler slave) {
        if (master == null) {
            throw new NullPointerException("master must not be null");
        }
        if (slave == null) {
            throw new NullPointerException("slave must not be null");
        }
        this.master = master;
        this.slave = slave;
    }

    @Override
    public void handle(TBase<?, ?> tbase) {
        Throwable masterException = null;
        try {
            master.handle(tbase);
        } catch (Throwable t) {
            masterException = t;
        }
        try {
            slave.handle(tbase);
        } catch (Throwable t) {
            logger.warn("slave handle({}) Error:{}", tbase.getClass().getSimpleName(), t.getMessage(), t);
        }
        if (masterException != null) {
            throw new RuntimeException(masterException);
        }
    }
}
