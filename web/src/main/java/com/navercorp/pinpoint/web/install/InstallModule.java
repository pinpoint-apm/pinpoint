package com.navercorp.pinpoint.web.install;

import com.navercorp.pinpoint.web.install.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.install.dao.AgentDownloadInfoDaoFactoryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
    public FactoryBean<AgentDownloadInfoDao> agentDownloadInfoDao(
            @Value("${web.installation.pinpointVersion:}") String version,
            @Value("${web.installation.downloadUrl:}") String downloadUrl,
            RestTemplate restTemplate) {
        AgentDownloadInfoDaoFactoryBean factoryBean = new AgentDownloadInfoDaoFactoryBean();
        factoryBean.setVersion(version);
        factoryBean.setDownloadUrl(downloadUrl);
        factoryBean.setRestTemplate(restTemplate);
        return factoryBean;
    }
}
