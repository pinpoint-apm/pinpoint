/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.batch.vo.CleanTarget;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ApplicationRemover implements ItemWriter<CleanTarget.TypeApplication> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;

    public ApplicationRemover(ApplicationIndexDao applicationIndexDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
    }

    @Override
    public void write(Chunk<? extends CleanTarget.TypeApplication> targets) throws Exception {
        if (targets.isEmpty()) {
            return;
        }
        // Unreachable code due to the ApplicationIndex table structure.
        logger.warn("ApplicationIndex table cleanup job doesn't need application deletion: {}", targets);
        for (CleanTarget.TypeApplication target : targets) {
            List<Application> applications = applicationIndexDao.selectApplicationName(target.application().getApplicationName());
            if (applications.size() == 1) {
                logger.info("Deleting applications: {}", applications);
                applicationIndexDao.deleteApplicationName(target.application().getApplicationName());
            } else {
                Application application = target.application();
                List<String> agentIds = applicationIndexDao.selectAgentIds(application.getApplicationName(), application.getServiceTypeCode());
                logger.info("Deleting application: {}", application);
                applicationIndexDao.deleteAgentIds(application.getApplicationName(), agentIds);
            }
        }
    }
}
