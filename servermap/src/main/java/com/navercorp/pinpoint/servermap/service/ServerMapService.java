/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.servermap.service;

import com.navercorp.pinpoint.servermap.bo.DirectionalBo;
import com.navercorp.pinpoint.servermap.dao.hbase.HbaseDao;
import com.navercorp.pinpoint.servermap.dao.redis.RedisDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
@Service
public class ServerMapService {

    private Logger logger = LogManager.getLogger(this.getClass());

    RedisDao redisDao;
    HbaseDao[] hbaseDaos;

    public ServerMapService(
            RedisDao redisDao,
            HbaseDao[] hbaseDaos
    ) {
        this.redisDao = Objects.requireNonNull(redisDao, "redisDao");
        this.hbaseDaos = Objects.requireNonNull(hbaseDaos, "hbaseDaos");
    }

    public void updateData() {
        logger.info("Updating server map data");
        List<DirectionalBo> directionalBoList = redisDao.readData();
        for (HbaseDao hbaseDao : hbaseDaos) {
            hbaseDao.insert(directionalBoList);
        }
    }
}
