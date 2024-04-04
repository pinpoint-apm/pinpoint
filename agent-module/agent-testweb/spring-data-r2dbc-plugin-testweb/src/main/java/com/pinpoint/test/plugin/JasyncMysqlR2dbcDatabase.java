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

package com.pinpoint.test.plugin;

import com.github.jasync.r2dbc.mysql.JasyncConnectionFactory;
import com.github.jasync.sql.db.Configuration;
import com.github.jasync.sql.db.SSLConfiguration;
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("jasync-mysql")
public class JasyncMysqlR2dbcDatabase implements R2dbcDatabase {
    private JasyncConnectionFactory cf;

    public ConnectionFactory getConnectionFactory() {
        return cf;
    }

    @PostConstruct
    public void init() throws Exception {
        MySQLConnectionFactory connectionFactory = new MySQLConnectionFactory(new Configuration("test", "localhost", 12722, "test", "test", new SSLConfiguration(), CharsetUtil.UTF_8, 65530, PooledByteBufAllocator.DEFAULT, 5 * 60 * 1000));
        cf = new JasyncConnectionFactory(connectionFactory);
    }

    @PreDestroy
    public void destroy() {
    }
}
