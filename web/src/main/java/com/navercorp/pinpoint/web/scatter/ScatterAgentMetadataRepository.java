/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.scatter;

import com.navercorp.pinpoint.web.vo.scatter.DotAgentInfo;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ScatterAgentMetadataRepository {

    private final Set<DotAgentInfo> dotAgentInfoSet;

    public ScatterAgentMetadataRepository() {
        this.dotAgentInfoSet = new HashSet<>();
    }

    public ScatterAgentMetadataRepository(Set<DotAgentInfo> dotAgentInfoSet) {
        this.dotAgentInfoSet = Objects.requireNonNull(dotAgentInfoSet, "dotAgentInfoSet");
    }

    void addDotAgentInfo(DotAgentInfo dotAgentInfo) {
        Objects.requireNonNull(dotAgentInfo, "dotAgentInfo");

        dotAgentInfoSet.add(dotAgentInfo);
    }

    void merge(ScatterAgentMetadataRepository scatterAgentMetadataRepository) {
        Objects.requireNonNull(scatterAgentMetadataRepository, "scatterAgentMetadataRepository");
        
        Set<DotAgentInfo> dotAgentInfoSet = scatterAgentMetadataRepository.getDotAgentInfoSet();
        for (DotAgentInfo dotAgentInfo : dotAgentInfoSet) {
            addDotAgentInfo(dotAgentInfo);
        }
    }

    public Set<DotAgentInfo> getDotAgentInfoSet() {
        return dotAgentInfoSet;
    }


}
