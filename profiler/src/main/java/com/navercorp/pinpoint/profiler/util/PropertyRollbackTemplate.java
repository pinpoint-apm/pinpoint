/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PropertyRollbackTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Properties properties;
    private final List<ExecuteContext<String>> contexts = new ArrayList<ExecuteContext<String>>();

    public PropertyRollbackTemplate(Properties properties) {
        this.properties = Assert.requireNonNull(properties, "properties");
    }

    public void addKey(String key, String value) {
        final ExecuteContext<String> context = new ExecuteContext<String>(key, value);
        contexts.add(context);
    }

    public <V> void execute(Runnable runnable) {
        Assert.requireNonNull(runnable, "runnable");

        // before
        final List<BeforeState<String>> beforeStates = new ArrayList<BeforeState<String>>(contexts.size());
        for (ExecuteContext<String> context : contexts) {
            final BeforeState<String> beforeState = context.prepare(properties);
            beforeStates.add(beforeState);
        }

        runnable.run();

        // after
        final List<ExecuteContext<String>> rollbackContexts = copyAndReverse(contexts);
        Collections.reverse(beforeStates);
        for (int i = 0; i < rollbackContexts.size(); i++) {
            ExecuteContext<String> rollbackContext = rollbackContexts.get(i);
            BeforeState<String> beforeState = beforeStates.get(i);
            rollbackContext.rollback(properties, beforeState);
        }
    }

    private <T> List<T> copyAndReverse(List<T> list) {
        final List<T> rollbackContexts = new ArrayList<T>(list);
        Collections.reverse(rollbackContexts);
        return rollbackContexts;
    }


    private class ExecuteContext<V> {
        private final String key;
        private final V value;


        private ExecuteContext(String key, V value) {
            this.key = Assert.requireNonNull(key, "key");
            this.value = Assert.requireNonNull(value, "value");
        }

        private BeforeState<String> prepare(Properties properties) {
            final boolean hasValue = properties.containsKey(this.key);
            final String backupValue = properties.getProperty(this.key, null);
            logger.debug("prepare put key:{} value:{}", this.key, this.value);
            properties.put(this.key, this.value);
            return new BeforeState<String>(hasValue, backupValue);
        }

        private void rollback(Properties properties, BeforeState<String> beforeState) {
            if (!beforeState.hasValue()) {
                logger.debug("rollback remove key:{}", this.key);
                properties.remove(this.key);
            } else {
                final String backupValue = beforeState.getBackupValue();
                logger.debug("rollback put key:{} value:{}", this.key, backupValue);
                properties.put(this.key, backupValue);
            }
        }
    }

    private static class BeforeState<V> {
        final boolean hasValue;
        private final V backupValue;

        BeforeState(boolean hasValue, V backupValue) {
            this.hasValue = hasValue;
            this.backupValue = backupValue;
        }

        boolean hasValue() {
            return hasValue;
        }

        V getBackupValue() {
            return backupValue;
        }
    }

}
