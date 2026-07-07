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
import com.navercorp.pinpoint.collector.util.DedupCache;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.Puts;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
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


    // dedup: write each (parent, self, host) row at most once per time slot.
    private final DedupCache<HostLinkKey> updater;


    public HbaseHostApplicationMapDao(HbaseOperations hbaseTemplate,
                                      HbaseColumnFamily table,
                                      TableNameProvider tableNameProvider,
                                      HostLinkFactory hostLinkFactory,
                                      DedupCache<HostLinkKey> updater,
                                      TimeSlot timeSlot) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.table = Objects.requireNonNull(table, "table");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");

        this.hostLinkFactory = Objects.requireNonNull(hostLinkFactory, "hostLinkFactory");
        this.updater = Objects.requireNonNull(updater, "updater");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }


    @Override
    public void insert(long requestTime, Vertex parentVertex, Vertex selfVertex, String host) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(parentVertex, "parentVertex");
        Objects.requireNonNull(selfVertex, "selfVertex");
        if (logger.isDebugEnabled()) {
            logger.debug("insert HostApplicationMap parent:{} self:{} host:{}", parentVertex, selfVertex, host);
        }

        final long statisticsRowSlot = timeSlot.getTimeSlot(requestTime);

        final HostLinkKey cacheKey = new HostLinkKey(parentVertex, selfVertex, host, statisticsRowSlot);
        final boolean needUpdate = updater.update(cacheKey);
        if (needUpdate) {
            insertHostVer2(statisticsRowSlot, parentVertex, selfVertex, host);
        }
    }

    private void insertHostVer2(long timestamp, Vertex parentVertex, Vertex selfVertex, String host) {
        if (logger.isDebugEnabled()) {
            logger.debug("Insert HostApplicationMap Ver2 parent={} self={} host={}",
                    parentVertex, selfVertex, host);
        }

        // TODO should consider to add bellow codes again later.
        //String parentAgentId = null;
        final byte[] rowKey = hostLinkFactory.rowkey(parentVertex, timestamp);

        byte[] columnName = hostLinkFactory.columnName(selfVertex, host);

        TableName hostApplicationMapTableName = tableNameProvider.getTableName(table.getTable());

        Put put = Puts.put(rowKey, table.getName(), columnName, null);
        this.hbaseTemplate.put(hostApplicationMapTableName, put);

    }

    public record HostLinkKey(Vertex parent, Vertex self, String host, long timeSlot) {

        public HostLinkKey {
            Objects.requireNonNull(parent, "parent");
            Objects.requireNonNull(self, "self");
            Objects.requireNonNull(host, "host");
        }
    }
}
