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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DotGroup {

    private final Coordinates coordinates;
    private final List<Dot> dotList = new ArrayList<>();

    private Dot dotLeader;

    public DotGroup(Coordinates coordinates) {
        this.coordinates = Objects.requireNonNull(coordinates, "coordinates");
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    void addDot(Dot dot) {
        dotList.add(dot);

        if (dotLeader == null) {
            dotLeader = dot;
        }
    }

    void merge(DotGroup dotGroup) {
        if (dotGroup == null) {
            return;
        }

        this.dotList.addAll(dotGroup.getDotList());
    }

    public List<Dot> getDotList() {
        return dotList;
    }

    public int getDotSize() {
        return dotList.size();
    }

    public Dot getDotLeader() {
        return dotLeader;
    }

    @Override
    public String toString() {
        return "DotGroup{" + "coordinates=" + coordinates + ", dotList=" + dotList + '}';
    }

}
