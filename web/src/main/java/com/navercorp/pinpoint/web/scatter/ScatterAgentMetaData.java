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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.navercorp.pinpoint.web.view.ScatterAgentMetaDataSerializer;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotAgentInfo;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = ScatterAgentMetaDataSerializer.class)
public class ScatterAgentMetaData {

    private final BiMap<DotAgentInfo, Integer> metaData;

    public ScatterAgentMetaData(Set<DotAgentInfo> dotAgentInfoSet) {
        Objects.requireNonNull(dotAgentInfoSet, "dotAgentInfoSet");
        
        this.metaData = HashBiMap.create(dotAgentInfoSet.size());

        int dotAgentInfoId = 1;
        for (DotAgentInfo dotAgentInfo : dotAgentInfoSet) {
            metaData.put(dotAgentInfo, dotAgentInfoId++);
        }
    }

    public int getId(Dot dot) {
        return getId(new DotAgentInfo(dot));
    }

    public int getId(DotAgentInfo dotAgentInfo) {
        Integer id = metaData.get(dotAgentInfo);
        if (id == null) {
            return -1;
        }

        return id;
    }

    public Set<Map.Entry<Integer, DotAgentInfo>> entrySet() {
        BiMap<Integer, DotAgentInfo> inverse = metaData.inverse();
        return inverse.entrySet();
    }

}
