package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.alarm.vo.AlarmStatus;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Creates rule rows together with the {@code alarm_state} row the evaluation batch
 * needs, so the two never come apart.
 * <p>
 * A rule without a state row is invisible to any query that drives off
 * {@code next_check_at}, and the failure is silent -- the alarm simply never fires.
 * Rules are created from three places (a standalone rule, applying a bundle, adding an
 * item to an applied bundle), so the pairing lives here rather than in each of them.
 */
@Component
class AlarmRuleStamper {

    private final AlarmRuleV2Dao ruleDao;
    private final AlarmStateDao stateDao;

    AlarmRuleStamper(AlarmRuleV2Dao ruleDao, AlarmStateDao stateDao) {
        this.ruleDao = Objects.requireNonNull(ruleDao, "ruleDao");
        this.stateDao = Objects.requireNonNull(stateDao, "stateDao");
    }

    /** Inserts a rule the caller built, plus its initial state. */
    void insertWithInitialState(AlarmRuleV2 rule) {
        ruleDao.insertRule(rule);
        upsertInitialState(rule);
    }

    /**
     * Builds and inserts the rule a bundle item produces for one application.
     * <p>
     * Name and description stay null so the rule follows the item; setting them here
     * would freeze a copy that later item edits could not reach.
     */
    AlarmRuleV2 stampFromItem(AlarmTemplateItem item, AlarmApplication application) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setDataSource(item.getDataSource());
        rule.setTemplateItemId(item.getId());
        rule.setServiceName(application.getServiceName());
        rule.setApplicationName(application.getApplicationName());
        rule.setApplicationType(application.getApplicationType());
        rule.setEnabled(true);
        insertWithInitialState(rule);
        return rule;
    }

    /**
     * A disabled rule gets no next check: the batch skips it anyway, and leaving the
     * timestamp behind would make it look due the moment it is enabled again.
     */
    void upsertInitialState(AlarmRuleV2 rule) {
        AlarmState state = new AlarmState(rule.getId());
        state.setStatus(AlarmStatus.NORMAL);
        state.setNextCheckAt(rule.isEnabled() ? LocalDateTime.now(ZoneOffset.UTC) : null);
        stateDao.upsert(state);
    }
}
