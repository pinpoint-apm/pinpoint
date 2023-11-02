package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.pinot.alarm.DefaultPinotAlarmWriterInterceptor;
import com.navercorp.pinpoint.pinot.alarm.PinotAlarmWriterInterceptor;
import com.navercorp.pinpoint.pinot.alarm.checker.PinotAlarmChecker;
import com.navercorp.pinpoint.pinot.alarm.checker.PinotAlarmCheckers;
import com.navercorp.pinpoint.pinot.alarm.service.PinotAlarmService;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmHistory;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmRule;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class UriStatAlarmWriter implements ItemWriter<PinotAlarmCheckers>, StepExecutionListener {
    private final Logger logger = LogManager.getLogger(UriStatAlarmWriter.class);

    private final PinotAlarmService alarmService;
    private final AlarmMessageSender alarmMessageSender;
    private final PinotAlarmWriterInterceptor interceptor;
    private StepExecution stepExecution;

    public UriStatAlarmWriter(AlarmMessageSender alarmMessageSender,
                              PinotAlarmService alarmService,
                              @Nullable PinotAlarmWriterInterceptor pinotAlarmWriterInterceptor) {
        this.alarmMessageSender = Objects.requireNonNull(alarmMessageSender, "alarmMessageSender");
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
        this.interceptor = Objects.requireNonNullElseGet(pinotAlarmWriterInterceptor, DefaultPinotAlarmWriterInterceptor::new);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(@Nonnull StepExecution stepExecution) {
        return null;
    }

    @Override
    public void write(List<? extends PinotAlarmCheckers> checkersList) throws Exception {
        interceptor.before(checkersList);
        if (CollectionUtils.isEmpty(checkersList)) {
            return;
        }
        for (PinotAlarmCheckers alarmCheckers : checkersList) {
            List<PinotAlarmChecker<? extends Number>> children = alarmCheckers.getChildren();
            if (CollectionUtils.isEmpty(children)) {
                return;
            }
            execute(children);
        }
        interceptor.after(checkersList);
    }

    private void execute(List<PinotAlarmChecker<? extends Number>> alarmCheckers) {
        for (PinotAlarmChecker<? extends Number> alarmChecker : alarmCheckers) {
            boolean[] detected = alarmChecker.getAlarmDetected();
            for (int i = 0; i < detected.length; i++) {
              if (detected[i]) {
                  sendAlarmMessage(alarmChecker, i);
              }
            }
        }
    }

    private void sendAlarmMessage(PinotAlarmChecker<? extends Number> alarmChecker, int index) {
        long now = System.currentTimeMillis();

        if (alarmChecker.isSMSSend(index)) {
            alarmMessageSender.sendSms(alarmChecker, index);
        }
        if (alarmChecker.isEmailSend(index)) {
            alarmMessageSender.sendEmail(alarmChecker, index);
        }
        if (alarmChecker.isWebhookSend(index)) {
            alarmMessageSender.sendWebhook(alarmChecker, index);
        }
        PinotAlarmRule rule = alarmChecker.getRules().get(index);
        PinotAlarmHistory history = new PinotAlarmHistory(rule.getId(), now);
        alarmService.insertAlarmHistory(history);
        logger.info("Alarm triggered for {} {} {}. Collected value: {}",
                alarmChecker.getServiceName(), alarmChecker.getApplicationName(),
                alarmChecker.getTarget(), alarmChecker.getCollectedValue());
    }
}
