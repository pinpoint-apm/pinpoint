/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.batch.vo.CleanTarget;
import jakarta.annotation.Nonnull;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youngjin.kim2
 */
public class CleanTargetWriter implements ItemWriter<CleanTarget> {

    private final ItemWriter<String> applicationRemover;
    private final ItemWriter<String> agentRemover;

    public CleanTargetWriter(ItemWriter<String> applicationRemover, ItemWriter<String> agentRemover) {
        this.applicationRemover = applicationRemover;
        this.agentRemover = agentRemover;
    }

    @Override
    public void write(@Nonnull List<? extends CleanTarget> items) throws Exception {
        if (this.applicationRemover != null) {
            this.applicationRemover.write(getApplicationNames(items));
        }

        if (this.agentRemover != null) {
            this.agentRemover.write(getAgents(items));
        }
    }

    private List<String> getAgents(List<? extends CleanTarget> items) {
        return getIdsByType(items, CleanTarget.TYPE_AGENT);
    }

    private List<String> getApplicationNames(List<? extends CleanTarget> items) {
        return getIdsByType(items, CleanTarget.TYPE_APPLICATION);
    }

    private List<String> getIdsByType(List<? extends CleanTarget> items, String type) {
        List<String> applicationNames = new ArrayList<>(items.size());
        for (CleanTarget item : items) {
            if (type.equals(item.getType())) {
                applicationNames.add(item.getId());
            }
        }
        return applicationNames;
    }

}
