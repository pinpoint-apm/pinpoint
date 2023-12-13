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

package com.navercorp.pinpoint.common.hbase;

import com.navercorp.pinpoint.common.hbase.util.Puts;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author emeroad
 * @author minwoo.jung
 */

@Disabled
public class HbaseTemplate2IT {
    private static Connection connection;
    private static HbaseTemplate hbaseTemplate2;

    @BeforeAll
    public static void beforeClass() throws IOException {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-hbase.properties");

        Configuration cfg = HBaseConfiguration.create();
        cfg.set("hbase.zookeeper.quorum", properties.getProperty("hbase.client.host"));
        cfg.set("hbase.zookeeper.property.clientPort", properties.getProperty("hbase.client.port"));

        connection = ConnectionFactory.createConnection(cfg);
        hbaseTemplate2 = new HbaseTemplate();
        hbaseTemplate2.setConfiguration(cfg);
        hbaseTemplate2.setTableFactory(new HbaseTableFactory(connection));
        hbaseTemplate2.afterPropertiesSet();
    }

    @AfterAll
    public static void afterClass() throws Exception {
        if (hbaseTemplate2 != null) {
            hbaseTemplate2.destroy();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void notExist() {
        try {
            Put put = Puts.put(new byte[]{0, 0, 0}, "familyName".getBytes(), "columnName".getBytes(), new byte[]{0, 0, 0});
            hbaseTemplate2.put(TableName.valueOf("NOT_EXIST"), put);
            Assertions.fail("exceptions");
        } catch (HbaseSystemException e) {
            RetriesExhaustedWithDetailsException exception = (RetriesExhaustedWithDetailsException) (e.getCause());
            if (!(exception.getCause(0) instanceof TableNotFoundException)) {
                Assertions.fail("unexpected exception :" + e.getCause());
            }
        }

    }
}
