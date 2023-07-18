package com.navercorp.pinpoint.web.webhook.dao;

import com.navercorp.pinpoint.web.webhook.model.Webhook;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class MysqlWebhookDao implements WebhookDao {
    private static final String NAMESPACE = WebhookDao.class.getName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlWebhookDao(@Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public String insertWebhook(Webhook webhook) {
        sqlSessionTemplate.insert(NAMESPACE + "insertWebhook", webhook);
        return webhook.getWebhookId();
    }

    @Override
    public void deleteWebhook(Webhook webhook) {
        sqlSessionTemplate.insert(NAMESPACE + "deleteWebhook", webhook);
    }

    @Override
    public void updateWebhook(Webhook webhook) {
        sqlSessionTemplate.update(NAMESPACE + "updateWebhook", webhook);
    }

    @Override
    public void deleteWebhookByApplicationId(String applicationId) {
        sqlSessionTemplate.insert(NAMESPACE + "deleteWebhookByApplicationId", applicationId);
    }

    @Override
    public void deleteWebhookByServiceName(String serviceName) {
        sqlSessionTemplate.insert(NAMESPACE + "deleteWebhookByServiceName", serviceName);
    }

    @Override
    public List<Webhook> selectWebhookByApplicationId(String applicationId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectWebhookByApplicationId", applicationId);
    }

    @Override
    public List<Webhook> selectWebhookByServiceName(String serviceName) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectWebhookByServiceName", serviceName);
    }

    @Override
    public List<Webhook> selectWebhookByRuleId(String ruleId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectWebhookByRuleId", ruleId);
    }

    @Override
    public List<Webhook> selectWebhookByPinotAlarmRuleId(String ruleId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectWebhookByPinotAlarmRuleId", ruleId);
    }

    @Override
    public Webhook selectWebhook(String webhookId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectWebhook", webhookId);
    }
}
