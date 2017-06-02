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

import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DotGroup {

    private final Coordinates coordinates;
    private final Set<Dot> dotSet = new HashSet<>();

    private Dot dotLeader;

    public DotGroup(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    void addDot(Dot dot) {
        dotSet.add(dot);

        if (dotLeader == null) {
            dotLeader = dot;
        }
    }

    void merge(DotGroup dotGroup) {
        if (dotGroup == null) {
            return;
        }

        dotSet.addAll(dotGroup.getDotSet());
    }

    public Set<Dot> getDotSet() {
        return dotSet;
    }

    public int getDotSize() {
        return dotSet.size();
    }

    public Dot getDotLeader() {
        return dotLeader;
    }

    @Override
    public String toString() {
        return "DotGroup{" + "coordinates=" + coordinates + ", dotSet=" + dotSet + '}';
    }

}
