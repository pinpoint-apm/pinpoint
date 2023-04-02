/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.install.dao;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.client.RestTemplate;

/**
 * @author Taejin Koo
 */
public class AgentDownloadInfoDaoFactoryBean implements FactoryBean<AgentDownloadInfoDao> {

    private String version;
    private String downloadUrl;
    private RestTemplate restTemplate;


    public AgentDownloadInfoDaoFactoryBean() {
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public AgentDownloadInfoDao getObject() throws Exception {
        if (StringUtils.hasText(version) && StringUtils.hasText(downloadUrl)) {
            return new MemoryAgentDownloadInfoDao(version, downloadUrl);
        }
        return new GithubAgentDownloadInfoDao(restTemplate);
    }

    @Override
    public Class<?> getObjectType() {
        return AgentDownloadInfoDao.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
