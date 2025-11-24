/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AcceptApplicationSet implements Iterable<AcceptApplication> {

    private final Set<AcceptApplication> set;

    public AcceptApplicationSet() {
        this(ConcurrentHashMap.newKeySet());
    }

    private AcceptApplicationSet(Set<AcceptApplication> set) {
        this.set = Objects.requireNonNull(set, "set");
    }

    public static AcceptApplicationSet copyOf(Set<AcceptApplication> set) {
        Objects.requireNonNull(set, "set");

        if (set.isEmpty()) {
            return new AcceptApplicationSet();
        } else {
            Set<AcceptApplication> copySet = ConcurrentHashMap.newKeySet();
            copySet.addAll(set);
            return new AcceptApplicationSet(copySet);
        }
    }

    public boolean add(AcceptApplication acceptApplication) {
        Objects.requireNonNull(acceptApplication, "acceptApplication");
        return set.add(acceptApplication);
    }


    @Override
    @NonNull
    public Iterator<AcceptApplication> iterator() {
        return set.iterator();
    }

    @Override
    public void forEach(Consumer<? super AcceptApplication> action) {
        Objects.requireNonNull(action, "action");
        set.forEach(action);
    }

    @Nullable
    public AcceptApplication firstElement() {
        if (set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    public int size() {
        return set.size();
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }


    public static boolean isEmpty(AcceptApplicationSet set) {
        return set == null || set.isEmpty();
    }

    public static boolean hasSize(AcceptApplicationSet set) {
        return set != null && !set.isEmpty();
    }

    public static int size(AcceptApplicationSet set) {
        if (set == null) {
            return 0;
        }
        return set.size();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AcceptApplicationSet that = (AcceptApplicationSet) o;
        return set.equals(that.set);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public String toString() {
        return "AcceptApplicationSet{" +
               "set=" + set +
               '}';
    }
}
