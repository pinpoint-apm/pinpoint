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

package com.navercorp.pinpoint.bootstrap.plugin.monitor;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Taejin Koo
 */
public class PluginMonitorRegistry<T extends PluginMonitor> implements PluginMonitorLocator<T> {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();

    private final CopyOnWriteArrayList<T> repository;

    private final PluginMonitorFactory<T> pluginMonitorFactory;

    public PluginMonitorRegistry(PluginMonitorFactory<T> pluginMonitorFactory) {
        this(5, pluginMonitorFactory);
    }

    public PluginMonitorRegistry(int capacity, PluginMonitorFactory<T> pluginMonitorFactory) {
        this.capacity = capacity;
        this.repository = new CopyOnWriteArrayList<T>();
        this.pluginMonitorFactory = pluginMonitorFactory;
    }

    public boolean register(T pluginMonitor) {
        T proxyMonitor = pluginMonitorFactory.create(pluginMonitor);

        boolean added = register0(proxyMonitor);
        if (!added) {
            if (logger.isInfoEnabled()) {
                logger.info("drop {}. registry is full.", pluginMonitor);
            }
        }
        return added;
    }

    // for fixed capacity
    private boolean register0(T pluginMonitor) {
        Lock lock = this.lock;
        lock.lock();
        try {
            if (repository.size() == capacity) {
                return false;
            } else {
                return repository.add(pluginMonitor);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean unregister(PluginMonitor monitor) {
        for (T pluginMonitorProxy : repository) {
            if (pluginMonitorProxy instanceof PluginMonitorProxy) {
                PluginMonitor delegate = ((PluginMonitorProxy) pluginMonitorProxy).getDelegate();
                if (delegate == monitor) {
                    return repository.remove(pluginMonitorProxy);
                }
            }
        }
        return false;
    }

    @Override
    public List<T> getPluginMonitorList() {
        List<T> pluginMonitorList = new ArrayList<T>(repository.size());
        List<T> disabledPluginMonitorList = new ArrayList<T>();

        for (T pluginMonitor : repository) {
            if (pluginMonitor.isDisabled()) {
                disabledPluginMonitorList.add(pluginMonitor);
            } else {
                pluginMonitorList.add(pluginMonitor);
            }
        }

        // bulk delete for reduce copy
        if (disabledPluginMonitorList.size() > 0) {
            repository.removeAll(disabledPluginMonitorList);
        }

        return pluginMonitorList;
    }

    public int getCapacity() {
        return capacity;
    }

}
