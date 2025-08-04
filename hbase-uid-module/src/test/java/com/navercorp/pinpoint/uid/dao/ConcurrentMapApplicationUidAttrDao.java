package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttrRow;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class ConcurrentMapApplicationUidAttrDao implements ApplicationUidAttrDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConcurrentMap<ApplicationUid, ApplicationUidAttribute> applicationNameMap = new ConcurrentHashMap<>();
    private final Executor executor;
    private final long delay;

    public ConcurrentMapApplicationUidAttrDao(Executor executor, long delay) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.delay = delay;
    }

    public ConcurrentMapApplicationUidAttrDao(Executor executor) {
        this(executor, 0);
    }

    @Override
    public ApplicationUidAttribute getApplicationAttr(ServiceUid serviceUid, ApplicationUid applicationUid) {
        return applicationNameMap.get(applicationUid);
    }

    @Override
    public boolean putApplicationAttrIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute) {
        logger.info("try insert name ({} -> {})", applicationUid, applicationUidAttribute);
        sleep(delay);
        return applicationNameMap.putIfAbsent(applicationUid, applicationUidAttribute) == null;
    }

    @Override
    public CompletableFuture<Boolean> asyncPutApplicationAttrIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("try insert name. ({} -> {})", applicationUid, applicationUidAttribute);
            sleep(delay);
            return applicationNameMap.putIfAbsent(applicationUid, applicationUidAttribute) == null;
        }, executor);
    }

    @Override
    public void deleteApplicationAttr(ServiceUid serviceUid, ApplicationUid applicationUid) {
        logger.info("delete name. ({} -> ?)", applicationUid);
        sleep(delay);
        applicationNameMap.remove(applicationUid);
    }

    @Override
    public CompletableFuture<Void> asyncDeleteApplicationAttr(ServiceUid serviceUid, ApplicationUid applicationUid) {
        return CompletableFuture.runAsync(() -> {
            logger.info("delete name .({} -> ?)", applicationUid);
            sleep(delay);
            applicationNameMap.remove(applicationUid);
        }, executor);
    }

    @Override
    public List<ApplicationUidAttrRow> scanApplicationAttrRow(ServiceUid serviceUid) {
        throw new UnsupportedOperationException("not supported");
    }

    private void sleep(long milliseconds) {
        if (milliseconds <= 0) {
            return;
        }

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
