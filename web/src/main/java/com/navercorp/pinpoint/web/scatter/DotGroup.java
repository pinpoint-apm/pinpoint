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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author Taejin Koo
 */
public class DotGroup {

    private static final DotComparator DOT_COMPARATOR = new DotComparator();

    private final Coordinates coordinates;
    private final List<Dot> dotList = new ArrayList<>();

    public DotGroup(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    void addDot(Dot dot) {
        dotList.add(dot);
    }

    public List<Dot> getSortedDotList() {
        Collections.sort(dotList, DOT_COMPARATOR);
        return dotList;
    }

    public List<Dot> getDotList() {
        return dotList;
    }

    void merge(DotGroup dotGroup) {
        if (dotGroup == null) {
            return;
        }

        dotList.addAll(dotGroup.getDotList());
    }

    private static class DotComparator implements Comparator<Dot> {

        @Override
        public int compare(Dot o1, Dot o2) {
            return Long.compare(o2.getAcceptedTime(), o1.getAcceptedTime());
        }

    }

    @Override
    public String toString() {
        return "DotGroup{" + "coordinates=" + coordinates + ", dotList=" + dotList + '}';
    }
}
