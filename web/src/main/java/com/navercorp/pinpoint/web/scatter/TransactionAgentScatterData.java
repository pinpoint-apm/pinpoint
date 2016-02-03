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
import com.navercorp.pinpoint.web.view.TransactionAgentScatterDataSerializer;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author Taejin Koo
 */
@JsonSerialize(using = TransactionAgentScatterDataSerializer.class)
public class TransactionAgentScatterData {

    private final Map<Coordinates, DotGroup> dotGroupMap = new HashMap<>();


    public TransactionAgentScatterData() {
    }

    void addDot(Coordinates coordinates, Dot dot) {
        DotGroup dotGroup = dotGroupMap.get(coordinates);
        if (dotGroup == null) {
            dotGroup = new DotGroup(coordinates);
            dotGroupMap.put(coordinates, dotGroup);
        }

        dotGroup.addDot(dot);
    }

    void merge(TransactionAgentScatterData transactionAgentScatterData) {
        if (transactionAgentScatterData == null) {
            return;
        }

        Map<Coordinates, DotGroup> dotGroupMap = transactionAgentScatterData.getDotGroupMap();
        for (Map.Entry<Coordinates, DotGroup> entry : dotGroupMap.entrySet()) {
            Coordinates key = entry.getKey();

            DotGroup dotGroup = this.dotGroupMap.get(key);
            if (dotGroup == null) {
                this.dotGroupMap.put(key, entry.getValue());
            } else {
                dotGroup.merge(entry.getValue());
            }
        }
    }

    public Map<Coordinates, DotGroup> getSortedDotGroupMap() {
        TreeMap<Coordinates, DotGroup> coordinatesObjectTreeMap = new TreeMap<>(new CoordinatesComparator());
        coordinatesObjectTreeMap.putAll(dotGroupMap);

        return coordinatesObjectTreeMap;
    }

    public Map<Coordinates, DotGroup> getDotGroupMap() {
        return dotGroupMap;
    }

    private class CoordinatesComparator implements Comparator<Coordinates> {

        @Override
        public int compare(Coordinates o1, Coordinates o2) {
            int compare = Long.compare(o2.getX(), o1.getX());
            if (compare != 0) {
                return compare;
            }

            return Long.compare(o1.getY(), o2.getY());
        }

    }

    @Override
    public String toString() {
        return "TransactionAgentScatterData{" + dotGroupMap.entrySet() + '}';
    }
}
