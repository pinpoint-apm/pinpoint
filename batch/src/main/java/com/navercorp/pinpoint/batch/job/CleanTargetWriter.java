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
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youngjin.kim2
 */
public class CleanTargetWriter implements ItemWriter<CleanTarget> {

    private final ItemWriter<CleanTarget.TypeApplication> applicationRemover;
    private final ItemWriter<CleanTarget.TypeAgents> agentRemover;

    public CleanTargetWriter(ItemWriter<CleanTarget.TypeApplication> applicationRemover, ItemWriter<CleanTarget.TypeAgents> agentRemover) {
        this.applicationRemover = applicationRemover;
        this.agentRemover = agentRemover;
    }

    @Override
    public void write(@Nonnull Chunk<? extends CleanTarget> chunks) throws Exception {
        List<? extends CleanTarget> items = chunks.getItems();
        if (this.applicationRemover != null) {
            this.applicationRemover.write(new Chunk<>(getTargetApplications(items)));
        }

        if (this.agentRemover != null) {
            this.agentRemover.write(new Chunk<>(getTargetAgents(items)));
        }
    }

    private List<CleanTarget.TypeAgents> getTargetAgents(List<? extends CleanTarget> items) {
        List<CleanTarget.TypeAgents> targets = new ArrayList<>();
        for (CleanTarget item : items) {
            if (item instanceof CleanTarget.TypeAgents agent) {
                targets.add(agent);
            }
        }
        return targets;
    }

    private List<CleanTarget.TypeApplication> getTargetApplications(List<? extends CleanTarget> items) {
        List<CleanTarget.TypeApplication> targets = new ArrayList<>();
        for (CleanTarget item : items) {
            if (item instanceof CleanTarget.TypeApplication application) {
                targets.add(application);
            }
        }
        return targets;
    }
}
