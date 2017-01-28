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

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.navercorp.pinpoint.common.util.PropertyUtils;


/**
 * @author emeroad
 * @author minwoo.jung
 */
public class HbaseTemplate2IT {
    private static HbaseTemplate2 hbaseTemplate2;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-hbase.properties");

        Configuration cfg = HBaseConfiguration.create();
        cfg.set("hbase.zookeeper.quorum", properties.getProperty("hbase.client.host"));
        cfg.set("hbase.zookeeper.property.clientPort", properties.getProperty("hbase.client.port"));
        
        hbaseTemplate2 = new HbaseTemplate2();
        hbaseTemplate2.setConfiguration(cfg);
        hbaseTemplate2.setTableFactory(new PooledHTableFactory(cfg));
        hbaseTemplate2.afterPropertiesSet();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (hbaseTemplate2 != null) {
            hbaseTemplate2.destroy();
        }
    }

    @Test
    @Ignore
    public void notExist() throws Exception {
        try {
            hbaseTemplate2.put(TableName.valueOf("NOT_EXIST"), new byte[] {0, 0, 0}, "familyName".getBytes(), "columnName".getBytes(), new byte[]{0, 0, 0});
            Assert.fail("exceptions");
        } catch (HbaseSystemException e) {
            RetriesExhaustedWithDetailsException exception = (RetriesExhaustedWithDetailsException)(e.getCause());
            if (!(exception.getCause(0) instanceof TableNotFoundException)) {
                Assert.fail("unexpected exception :" + e.getCause()); 
            }
        }

    }
}
