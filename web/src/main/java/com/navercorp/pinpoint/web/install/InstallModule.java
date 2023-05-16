package com.navercorp.pinpoint.web.install;

import com.navercorp.pinpoint.web.install.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.install.dao.GithubAgentDownloadInfoDao;
import com.navercorp.pinpoint.web.install.dao.MemoryAgentDownloadInfoDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
@ComponentScan({
//        "com.navercorp.pinpoint.web.install.controller",
        "com.navercorp.pinpoint.web.install.service",
})
public class InstallModule {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public InstallModule() {
        logger.info("Install {}", InstallModule.class.getSimpleName());
    }

    @Bean
    @ConditionalOnProperty(value = "pinpoint.modules.web.install.type", havingValue = "url")
    public AgentDownloadInfoDao urlAgentDownloadInfoDao(
            @Value("${web.installation.pinpointVersion:}") String version,
            @Value("${web.installation.downloadUrl:}") String downloadUrl) {
        Assert.hasLength(version, "version");
        Assert.hasLength(downloadUrl, "downloadUrl");
        return new MemoryAgentDownloadInfoDao(version, downloadUrl);

    }

    @Bean
    @ConditionalOnProperty(value = "pinpoint.modules.web.install.type", havingValue = "github", matchIfMissing = true)
    public AgentDownloadInfoDao githubAgentDownloadInfoDao(RestTemplate restTemplate) {
        return new GithubAgentDownloadInfoDao(restTemplate);
    }
}
