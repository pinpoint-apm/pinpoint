/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.mongo.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

public class ReactiveMongoCollectionImplConstructorInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public ReactiveMongoCollectionImplConstructorInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (throwable != null) {
            return;
        }

        if (Boolean.FALSE == (target instanceof DatabaseInfoAccessor)) {
            return;
        }

        final DatabaseInfoAccessor databaseInfoAccessor = ArrayArgumentUtils.getArgument(args, 0, DatabaseInfoAccessor.class);
        if (databaseInfoAccessor == null) {
            return;
        }

        final DatabaseInfo databaseInfo = databaseInfoAccessor._$PINPOINT$_getDatabaseInfo();
        if (databaseInfo == null) {
            return;
        }
        ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
        if (isDebug) {
            logger.debug("Set databaseInfo={}", databaseInfo);
        }
    }
}
