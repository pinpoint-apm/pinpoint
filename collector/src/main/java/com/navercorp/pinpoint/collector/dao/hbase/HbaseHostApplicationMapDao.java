/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.collector.util.AtomicLongUpdateMap;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.common.util.TimeUtils;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final TableDescriptor<HbaseColumnFamily.HostStatMap> descriptor;

    private final AcceptedTimeService acceptedTimeService;

    private final TimeSlot timeSlot;

    private final AbstractRowKeyDistributor rowKeyDistributor;

    // FIXME should modify to save a cachekey at each 30~50 seconds instead of saving at each time
    private final AtomicLongUpdateMap<CacheKey> updater = new AtomicLongUpdateMap<>();

    public HbaseHostApplicationMapDao(HbaseOperations2 hbaseTemplate,
                                      TableDescriptor<HbaseColumnFamily.HostStatMap> descriptor,
                                      @Qualifier("acceptApplicationRowKeyDistributor") AbstractRowKeyDistributor rowKeyDistributor,
                                      AcceptedTimeService acceptedTimeService,
                                      TimeSlot timeSlot) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }


    @Override
    public void insert(String host, String bindApplicationName, short bindServiceType, String parentApplicationName, short parentServiceType) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(bindApplicationName, "bindApplicationName");
        if (logger.isDebugEnabled()) {
            logger.debug("insert HostApplicationMap, host:{}, app:{},SType:{},parentApp:{},parentAppSType{}", host, bindApplicationName, bindServiceType, parentApplicationName, parentServiceType);
        }

        final long statisticsRowSlot = getSlotTime();

        final CacheKey cacheKey = new CacheKey(host, bindApplicationName, bindServiceType, parentApplicationName, parentServiceType);
        final boolean needUpdate = updater.update(cacheKey, statisticsRowSlot);
        if (needUpdate) {
            insertHostVer2(host, bindApplicationName, bindServiceType, statisticsRowSlot, parentApplicationName, parentServiceType);
        }
    }


    private long getSlotTime() {
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        return timeSlot.getTimeSlot(acceptedTime);
    }


    private void insertHostVer2(String host, String bindApplicationName, short bindServiceType, long statisticsRowSlot, String parentApplicationName, short parentServiceType) {
        if (logger.isDebugEnabled()) {
            logger.debug("Insert host-application map. host={}, bindApplicationName={}, bindServiceType={}, parentApplicationName={}, parentServiceType={}",
                    host, bindApplicationName, bindServiceType, parentApplicationName, parentServiceType);
        }

        // TODO should consider to add bellow codes again later.
        //String parentAgentId = null;
        //final byte[] rowKey = createRowKey(parentApplicationName, parentServiceType, statisticsRowSlot, parentAgentId);
        final byte[] rowKey = createRowKey(parentApplicationName, parentServiceType, statisticsRowSlot, null);

        byte[] columnName = createColumnName(host, bindApplicationName, bindServiceType);

        TableName hostApplicationMapTableName = descriptor.getTableName();
        try {
            hbaseTemplate.put(hostApplicationMapTableName, rowKey, descriptor.getColumnFamilyName(), columnName, null);
        } catch (Exception ex) {
            logger.warn("retry one. Caused:{}", ex.getCause(), ex);
            hbaseTemplate.put(hostApplicationMapTableName, rowKey, descriptor.getColumnFamilyName(), columnName, null);
        }
    }

    private byte[] createColumnName(String host, String bindApplicationName, short bindServiceType) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPrefixedString(host);
        buffer.putPrefixedString(bindApplicationName);
        buffer.putShort(bindServiceType);
        return buffer.getBuffer();
    }


    private byte[] createRowKey(String parentApplicationName, short parentServiceType, long statisticsRowSlot, String parentAgentId) {
        final byte[] rowKey = createRowKey0(parentApplicationName, parentServiceType, statisticsRowSlot, parentAgentId);
        return rowKeyDistributor.getDistributedKey(rowKey);
    }


    @VisibleForTesting
    static byte[] createRowKey0(String parentApplicationName, short parentServiceType, long statisticsRowSlot, String parentAgentId) {

        // even if  a agentId be added for additional specifications, it may be safe to scan rows.
        // But is it needed to add parentAgentServiceType?
        final int SIZE = HbaseTableConstants.APPLICATION_NAME_MAX_LEN + 2 + 8;
        final Buffer rowKeyBuffer = new AutomaticBuffer(SIZE);
        rowKeyBuffer.putPadString(parentApplicationName, HbaseTableConstants.APPLICATION_NAME_MAX_LEN);
        rowKeyBuffer.putShort(parentServiceType);
        rowKeyBuffer.putLong(TimeUtils.reverseTimeMillis(statisticsRowSlot));
        // there is no parentAgentId for now.  if it added later, need to comment out below code for compatibility.
//        rowKeyBuffer.putPadString(parentAgentId, HbaseTableConstants.AGENT_NAME_MAX_LEN);
        return rowKeyBuffer.getBuffer();
    }

    private static final class CacheKey {
        private final String host;
        private final String applicationName;
        private final short serviceType;

        private final String parentApplicationName;
        private final short parentServiceType;

        public CacheKey(String host, String applicationName, short serviceType, String parentApplicationName, short parentServiceType) {
            this.host = Objects.requireNonNull(host, "host");
            this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
            this.serviceType = serviceType;

            // may be null for below two parent values.
            this.parentApplicationName = parentApplicationName;
            this.parentServiceType = parentServiceType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (parentServiceType != cacheKey.parentServiceType) return false;
            if (serviceType != cacheKey.serviceType) return false;
            if (!applicationName.equals(cacheKey.applicationName)) return false;
            if (!host.equals(cacheKey.host)) return false;
            if (parentApplicationName != null ? !parentApplicationName.equals(cacheKey.parentApplicationName) : cacheKey.parentApplicationName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = host.hashCode();
            result = 31 * result + applicationName.hashCode();
            result = 31 * result + (int) serviceType;
            result = 31 * result + (parentApplicationName != null ? parentApplicationName.hashCode() : 0);
            result = 31 * result + (int) parentServiceType;
            return result;
        }
    }
}
