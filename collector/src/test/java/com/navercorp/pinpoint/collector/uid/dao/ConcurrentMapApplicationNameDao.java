package com.navercorp.pinpoint.collector.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class ConcurrentMapApplicationNameDao implements ApplicationNameDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConcurrentMap<ApplicationUid, String> applicationNameMap = new ConcurrentHashMap<>();
    private final Executor executor;
    private final long delay;

    public ConcurrentMapApplicationNameDao(Executor executor, long delay) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.delay = delay;
    }

    public ConcurrentMapApplicationNameDao(Executor executor) {
        this(executor, 0);
    }

    @Override
    public boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        logger.info("try insert name ({} -> name={})", applicationUid, applicationName);
        sleep(delay);
        return applicationNameMap.putIfAbsent(applicationUid, applicationName) == null;
    }

    @Override
    public CompletableFuture<Boolean> asyncInsertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("try insert name ({} -> name:{})", applicationUid, applicationName);
            sleep(delay);
            return applicationNameMap.putIfAbsent(applicationUid, applicationName) == null;
        }, executor);
    }

    @Override
    public void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        logger.info("delete name ({} -> )", applicationUid);
        sleep(delay);
        applicationNameMap.remove(applicationUid);
    }

    @Override
    public CompletableFuture<Void> asyncDeleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        return CompletableFuture.runAsync(() -> {
            logger.info("delete name ({} -> name:?)", applicationUid);
            sleep(delay);
            applicationNameMap.remove(applicationUid);
        }, executor);
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
