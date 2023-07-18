package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.dao.PinotAlarmDao;
import com.navercorp.pinpoint.batch.alarm.dao.UriStatDao;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.batch.alarm.vo.UriStatQueryParams;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class UriStatAlarmReader implements ItemReader<PinotAlarmKey>, StepExecutionListener {
    private static final long activeDuration = TimeUnit.MINUTES.toMillis(5);
    private static final String alarmCategory = "URISTAT";
    private final String tenantId;
    private final PinotAlarmDao alarmDao;
    private final UriStatDao uriStatDao;
    private Queue<PinotAlarmKey> applicationQueue;

    public UriStatAlarmReader(@Nonnull TenantProvider tenantProvider, @Nonnull PinotAlarmDao alarmDao, @Nonnull UriStatDao uriStatDao) {
        this.tenantId = tenantProvider.getTenantId();
        this.alarmDao = alarmDao;
        this.uriStatDao = uriStatDao;
    }

    @Override
    public void beforeStep(@Nonnull StepExecution stepExecution) {
        long now = System.currentTimeMillis();
        this.applicationQueue = new ConcurrentLinkedQueue<>(fetchUriStatAlarmKeys(now));
    }

    private List<PinotAlarmKey> fetchUriStatAlarmKeys(long now) {
        final Range range = Range.between(now - activeDuration, now);
        List<PinotAlarmKey> registeredKeys = alarmDao.selectAlarmKeys(alarmCategory);

        List<PinotAlarmKey> alarmKeys = new ArrayList<>();
        for (PinotAlarmKey key : registeredKeys) {
            UriStatQueryParams params = new UriStatQueryParams(tenantId, key.getServiceName(), key.getApplicationName(), key.getTarget(), range);
            if (uriStatDao.checkIfKeyExists(params)) {
                alarmKeys.add(key);
            }
        }
        return alarmKeys;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public PinotAlarmKey read() {
        return applicationQueue.poll();
    }
}
