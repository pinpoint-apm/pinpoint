/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.applicationmap.dao.hbase;

import com.navercorp.pinpoint.collector.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.collector.util.AtomicLongUpdateMap;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.Puts;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final HbaseColumnFamily table;

    private final HbaseOperations hbaseTemplate;

    private final TableNameProvider tableNameProvider;

    private final TimeSlot timeSlot;

    private final HostLinkFactory hostLinkFactory;


    // FIXME should modify to save a cachekey at each 30~50 seconds instead of saving at each time
    private final AtomicLongUpdateMap<CacheKey> updater = new AtomicLongUpdateMap<>();


    public HbaseHostApplicationMapDao(HbaseOperations hbaseTemplate,
                                      HbaseColumnFamily table,
                                      TableNameProvider tableNameProvider,
                                      HostLinkFactory hostLinkFactory,
                                      TimeSlot timeSlot) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.table = Objects.requireNonNull(table, "table");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");

        this.hostLinkFactory = Objects.requireNonNull(hostLinkFactory, "hostLinkFactory");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }


    @Override
    public void insert(long requestTime, String parentApplicationName, int parentServiceType, Vertex selfVertex, String host) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(selfVertex, "selfVertex");
        if (logger.isDebugEnabled()) {
            logger.debug("insert HostApplicationMap host:{}, self:{} parent:{}/{}", host, selfVertex, parentApplicationName, parentServiceType);
        }

        final long statisticsRowSlot = timeSlot.getTimeSlot(requestTime);

        final CacheKey cacheKey = new CacheKey(selfVertex.applicationName(), selfVertex.serviceType().getCode(), selfVertex.serviceUid(), host,
                parentApplicationName, parentServiceType, ServiceUid.DEFAULT_SERVICE_UID_CODE);
        final boolean needUpdate = updater.update(cacheKey, statisticsRowSlot);
        if (needUpdate) {
            insertHostVer2(parentApplicationName, parentServiceType, selfVertex, host, statisticsRowSlot);
        }
    }

    private void insertHostVer2(String parentApplicationName, int parentServiceType, Vertex selfVertex, String host, long timestamp) {
        if (logger.isDebugEnabled()) {
            logger.debug("Insert HostApplicationMap Ver2 host={}, self={}, parent={}/{}",
                    host, selfVertex, parentApplicationName, parentServiceType);
        }

        // TODO should consider to add bellow codes again later.
        //String parentAgentId = null;
        final byte[] rowKey = hostLinkFactory.rowkey(parentApplicationName, parentServiceType, ServiceUid.DEFAULT_SERVICE_UID_CODE, timestamp);

        byte[] columnName = hostLinkFactory.columnName(selfVertex, host);

        TableName hostApplicationMapTableName = tableNameProvider.getTableName(table.getTable());

        Put put = Puts.put(rowKey, table.getName(), columnName, null);
        this.hbaseTemplate.put(hostApplicationMapTableName, put);

    }

    private record CacheKey(String applicationName, int serviceType, int serviceUid, String host,
                            String parentApplicationName, int parentServiceType, int parentServiceUid) {

            private CacheKey {
                Objects.requireNonNull(applicationName, "applicationName");
                Objects.requireNonNull(host, "host");
            }
    }
}
