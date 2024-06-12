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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
@Repository
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

    private static final int HOST_APPLICATION_MAP_VER2_NUM_PARTITIONS = 4;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseOperations hbaseOperations;

    private final TableNameProvider tableNameProvider;

    private final RowMapper<List<AcceptApplication>> hostApplicationMapper;

    private final TimeSlot timeSlot;

    private final AbstractRowKeyDistributor acceptApplicationRowKeyDistributor;

    public HbaseHostApplicationMapDao(HbaseOperations hbaseOperations,
                                      TableNameProvider tableNameProvider,
                                      @Qualifier("hostApplicationMapper") RowMapper<List<AcceptApplication>> hostApplicationMapper,
                                      TimeSlot timeSlot, @Qualifier("acceptApplicationRowKeyDistributor") AbstractRowKeyDistributor acceptApplicationRowKeyDistributor) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.hostApplicationMapper = Objects.requireNonNull(hostApplicationMapper, "hostApplicationMapper");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
        this.acceptApplicationRowKeyDistributor = Objects.requireNonNull(acceptApplicationRowKeyDistributor, "acceptApplicationRowKeyDistributor");
    }


    @Override
    public Set<AcceptApplication> findAcceptApplicationName(Application fromApplication, Range range) {
        Objects.requireNonNull(fromApplication, "fromApplication");
        final Scan scan = createScan(fromApplication, range);

        TableName hostApplicationMapTableName = tableNameProvider.getTableName(HbaseTable.HOST_APPLICATION_MAP_VER2);
        final List<List<AcceptApplication>> result = hbaseOperations.findParallel(hostApplicationMapTableName, scan, acceptApplicationRowKeyDistributor, hostApplicationMapper, HOST_APPLICATION_MAP_VER2_NUM_PARTITIONS);
        if (CollectionUtils.isNotEmpty(result)) {
            final Set<AcceptApplication> resultSet = new HashSet<>();
            for (List<AcceptApplication> resultList : result) {
                resultSet.addAll(resultList);
            }
            logger.debug("findAcceptApplicationName result:{}", resultSet);
            return resultSet;
        } else {
            return Collections.emptySet();
        }
    }




    private Scan createScan(Application parentApplication, Range range) {
        Objects.requireNonNull(parentApplication, "parentApplication");

        if (logger.isDebugEnabled()) {
            logger.debug("scan parentApplication:{}, range:{}", parentApplication, range);
        }

        // TODO: need common logic for creating scanner
        final long startTime = TimeUtils.reverseTimeMillis(timeSlot.getTimeSlot(range.getFrom()));
        final long endTime = TimeUtils.reverseTimeMillis(timeSlot.getTimeSlot(range.getTo()) + 1);

        // start key is replaced by end key because timestamp has been reversed
        final byte[] startKey = createKey(parentApplication, endTime);
        final byte[] endKey = createKey(parentApplication, startTime);

        Scan scan = new Scan();
        scan.setCaching(10);
        scan.withStartRow(startKey);
        scan.withStopRow(endKey);
        scan.setId("HostApplicationScan_Ver2");

        return scan;
    }

    private byte[] createKey(Application parentApplication, long time) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPadString(parentApplication.name(), HbaseTableConstants.APPLICATION_NAME_MAX_LEN);
        buffer.putShort(parentApplication.getServiceTypeCode());
        buffer.putLong(time);
        return buffer.getBuffer();
    }



}
