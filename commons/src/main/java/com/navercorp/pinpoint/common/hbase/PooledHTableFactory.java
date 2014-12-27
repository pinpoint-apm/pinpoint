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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTableInterfaceFactory;
import org.apache.hadoop.hbase.client.HTablePool;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;

/**
 * HTableInterfaceFactory based on HTablePool.
 * @author emeroad
 */
public class PooledHTableFactory implements HTableInterfaceFactory, DisposableBean {

    private HTablePool hTablePool;
    public static final int DEFAULT_POOL_SIZE = 256;

    public PooledHTableFactory(Configuration config) {
        this.hTablePool = new HTablePool(config, DEFAULT_POOL_SIZE);
    }

    public PooledHTableFactory(Configuration config, int poolSize) {
        this.hTablePool = new HTablePool(config, poolSize);
    }


    @Override
    public HTableInterface createHTableInterface(Configuration config, byte[] tableName) {
        return hTablePool.getTable(tableName);
    }

    @Override
    public void releaseHTableInterface(HTableInterface table) throws IOException {
        if (table != null) {
            table.close();
        }
    }


    @Override
    public void destroy() throws Exception {
        if (hTablePool != null) {
            this.hTablePool.close();
        }
    }
}
