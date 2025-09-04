/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.uristat.collector.service;

import com.google.common.primitives.Ints;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import com.navercorp.pinpoint.uristat.collector.dao.UriStatDao;
import com.navercorp.pinpoint.uristat.collector.model.UriStat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@ConditionalOnProperty(value = "pinpoint.modules.collector.uristat.enabled", havingValue = "true")
public class PinotAgentUriStatService implements AgentUriStatService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int BUCKET_SIZE = 8;
    private static final List<Integer> EMPTY_BUCKETS
            = Collections.unmodifiableList(Ints.asList(new int[BUCKET_SIZE]));

    private final UriStatDao uriStatDao;
    private final TenantProvider tenantProvider;

    public PinotAgentUriStatService(UriStatDao uriStatDao, TenantProvider tenantProvider) {
        this.uriStatDao = Objects.requireNonNull(uriStatDao, "uriStatDao");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    @Override
    public void save(AgentUriStatBo agentUriStatBo) {
        if (logger.isDebugEnabled()) {
            logger.debug("save {}", agentUriStatBo);
        }
        List<EachUriStatBo> eachUriStatBoList = agentUriStatBo.getEachUriStatBoList();
        List<UriStat> data = new ArrayList<>(eachUriStatBoList.size());

        final String serviceName = agentUriStatBo.getServiceName();
        final String applicationName = agentUriStatBo.getApplicationName();
        final String agentId = agentUriStatBo.getAgentId();
        final int version = agentUriStatBo.getBucketVersion();
        final String tenantId = tenantProvider.getTenantId();

        for (EachUriStatBo eachUriStatBo : eachUriStatBoList) {
            final String uri = eachUriStatBo.getUri();
            final long timestamp = eachUriStatBo.getTimestamp();
            final UriStatHistogram totalHistogram = eachUriStatBo.getTotalHistogram();
            final UriStatHistogram failureHistogram = eachUriStatBo.getFailedHistogram();
            UriStat uriStat = new UriStat(timestamp, tenantId, serviceName, applicationName, agentId, uri,
                    totalHistogram.getMax(),
                    totalHistogram.getTotal(),
                    getOrDefaultHistogram(totalHistogram),
                    getOrDefaultHistogram(failureHistogram),
                    version);
            data.add(uriStat);
        }
        uriStatDao.insert(data);
    }

    public List<Integer> getOrDefaultHistogram(UriStatHistogram histogram) {
        if (histogram != null) {
            return histogram.getTimestampHistogram();
        }
        return EMPTY_BUCKETS;
    }
}
