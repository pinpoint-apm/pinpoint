package com.navercorp.pinpoint.web.dao.mysql;

import com.navercorp.pinpoint.web.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.vo.WebhookSendInfo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class MysqlWebhookSendInfoDao implements WebhookSendInfoDao {
    private static final String NAMESPACE = WebhookSendInfoDao.class.getName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlWebhookSendInfoDao(@Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public String insertWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        sqlSessionTemplate.insert(NAMESPACE + "insertWebhookSendInfo", webhookSendInfo);
        return webhookSendInfo.getWebhookSendInfoId();
    }

    @Override
    public void deleteWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        sqlSessionTemplate.insert(NAMESPACE + "deleteWebhookSendInfo", webhookSendInfo);
    }

    @Override
    public void deleteWebhookSendInfoByWebhookId(String webhookId) {
        sqlSessionTemplate.insert(NAMESPACE + "deleteWebhookSendInfoByWebhookId", webhookId);
    }

    @Override
    public void deleteWebhookSendInfoByRuleId(String ruleId) {
        sqlSessionTemplate.insert(NAMESPACE + "deleteWebhookSendInfoByRuleId", ruleId);
    }

    @Override
    public void updateWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        sqlSessionTemplate.update(NAMESPACE + "updateWebhookSendInfo", webhookSendInfo);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByApplicationId(String applicationId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectWebhookSendInfoByApplicationId", applicationId);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByServiceName(String serviceName) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectWebhookSendInfoByServiceName", serviceName);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByWebhookId(String webhookId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectWebhookSendInfoByWebhookId", webhookId);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByRuleId(String ruleId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectWebhookSendInfoByRuleId", ruleId);
    }

    @Override
    public WebhookSendInfo selectWebhookSendInfo(String webhookSendInfoId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectWebhookSendInfo", webhookSendInfoId);
    }
}
