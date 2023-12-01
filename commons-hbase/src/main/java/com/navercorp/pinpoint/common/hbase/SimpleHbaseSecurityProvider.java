/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.AuthUtil;
import org.apache.hadoop.hbase.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class SimpleHbaseSecurityProvider implements HbaseSecurityProvider {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Configuration configuration;

    public SimpleHbaseSecurityProvider(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
    }

    @Override
    public User login() {
        try {
            User user = AuthUtil.loginClient(configuration);
            logger.info("HbaseSecurity user : {}",  user);
            return user;
        } catch (IOException e) {
            logger.error("SimpleHbaseSecurityInterceptor: {}", e.getMessage(),  e);
            throw new HbaseSystemException(e);
        }
    }

}
