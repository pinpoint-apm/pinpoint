/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.util.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;


public class HbaseVersionCheckBean implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HBaseAdminTemplate template;

    private final boolean hbaseVersionCompatibility;

    public HbaseVersionCheckBean(HBaseAdminTemplate template,
                                 @Value("${hbase.client.compatibility-check:true}") boolean hbaseVersionCompatibility) {
        this.template = Objects.requireNonNull(template, "template");
        this.hbaseVersionCompatibility = hbaseVersionCompatibility;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ClusterStatus clusterStatus = template.execute(Admin::getClusterStatus);
        logger.info("hbase-clusterStatus:{}", clusterStatus);
        String hBaseServerVersion = clusterStatus.getHBaseVersion();
        logger.info("hbase-clusterStatus HBaseServerVersion:{}", hBaseServerVersion);
        if (!hbaseVersionCompatibility) {
            return;
        }

        HBaseClientVersion hbaseClientVersion = getHbaseClientVersion();
        if (!hbaseClientVersion.acceptVersion(hBaseServerVersion)) {
            String error = String.format("HBase version compatibility violation HBaseClient:%s, HBaseServer:%s", hbaseClientVersion, hBaseServerVersion);
            logger.error(error);
            throw new HBaseAccessException(error);
        }

    }

    private HBaseClientVersion getHbaseClientVersion() {
        final String version = VersionInfo.getVersion();
        logger.info("HBaseClientVersion:{}", version);

        HBaseClientVersion hBaseClientVersion = HBaseClientVersion.getHBaseVersion(version);
        if (hBaseClientVersion == null) {
            throw new HBaseAccessException("Unknown HbaseClientVersion:" + version);
        }
        return hBaseClientVersion;
    }
}
