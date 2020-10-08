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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * @author Taejin Koo
 */
public class DotGroups {

    private static final Comparator<Dot> DOT_COMPARATOR = Comparator.comparingLong(Dot::getAcceptedTime);

    private final long xCoordinates;
    private final Map<Key, DotGroup> dotGroupMap;

    public DotGroups(long xCoordinates) {
        this.xCoordinates = xCoordinates;
        this.dotGroupMap = new HashMap<>();
    }


    void addDot(Coordinates coordinates, Dot dot) {
        Objects.requireNonNull(coordinates, "coordinates");
        Objects.requireNonNull(dot, "dot");

        final Key key = new Key(coordinates, dot.getSimpleExceptionCode());

        DotGroup dotGroup = dotGroupMap.get(key);
        if (dotGroup == null) {
            dotGroup = new DotGroup(coordinates);
            dotGroupMap.put(key, dotGroup);
        }

        dotGroup.addDot(dot);
    }


    public long getXCoordinates() {
        return xCoordinates;
    }

    public Map<Key, DotGroup> getDotGroupMap() {
        return dotGroupMap;
    }

    public List<Dot> getSortedDotSet() {
        Collection<DotGroup> dotGroupList = dotGroupMap.values();

        int size = getSize(dotGroupList, DotGroup::getDotSize);

        List<Dot> dotList = new ArrayList<>(size);
        for (DotGroup dotGroup : dotGroupList) {
            dotList.addAll(dotGroup.getDotList());
        }

        dotList.sort(DOT_COMPARATOR);
        return dotList;

    }

    private <T> int getSize(Collection<T> collection, ToIntFunction<? super T> keyExtractor) {
        int size = 0;
        for (T t : collection) {
            size += keyExtractor.applyAsInt(t);
        }
        return size;
    }

    public Map<Dot, DotGroup> getDotGroupLeaders() {
        Map<Dot, DotGroup> dotLeaderMap = new HashMap<>(dotGroupMap.size());

        Collection<DotGroup> dotGroupList = dotGroupMap.values();
        for (DotGroup dotGroup : dotGroupList) {
            if (dotGroup.getDotLeader() != null) {
                dotLeaderMap.put(dotGroup.getDotLeader(), dotGroup);
            }
        }

        return dotLeaderMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DotGroups dotGroups = (DotGroups) o;
        return xCoordinates == dotGroups.getXCoordinates();
    }

    @Override
    public int hashCode() {
        return (int) (xCoordinates ^ (xCoordinates >>> 32));
    }

    @Override
    public String toString() {
        return "DotGroups{" + "xCoordinates=" + xCoordinates + ", dotGroupMap=" + dotGroupMap + '}';
    }

    static class Key {

        private final Coordinates coordinates;
        private final int code;

        private int hashCode = 0;

        public Key(Coordinates coordinates, int code) {
            this.coordinates = Objects.requireNonNull(coordinates, "coordinates");
            this.code = code;

            hashCode();
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Key that = (Key) obj;

            if (!coordinates.equals(that.coordinates)) {
                return false;
            }

            if (code != that.code) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int result = coordinates != null ? coordinates.hashCode() : 0;
                result = 31 * result + code;

                this.hashCode = result;
            }
            return hashCode;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "coordinates=" + coordinates +
                    ", code=" + code +
                    '}';
        }

    }

}
