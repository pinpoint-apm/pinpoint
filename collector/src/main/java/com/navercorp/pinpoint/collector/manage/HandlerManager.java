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

package com.navercorp.pinpoint.collector.manage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class HandlerManager extends AbstractCollectorManager implements HandlerManagerMBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean enable = true;

    @Override
    public void enableAccess() {
        logger.warn("Enable access to manager.");
        this.enable = true;
    }

    @Override
    public void disableAccess() {
        logger.warn("Disable access to manager.");
        this.enable = false;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

}
