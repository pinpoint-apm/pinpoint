/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.metric;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class FieldDescriptors {

    private final List<FieldDescriptor> fieldDescriptorList;

    private FieldDescriptors(List<FieldDescriptor> fieldDescriptorList) {
        Assert.isTrue(CollectionUtils.nullSafeSize(fieldDescriptorList) > 0, "fieldDescriptor may not be empty");

        // Sort in ascending order.
        fieldDescriptorList.sort(new Comparator<FieldDescriptor>() {
            @Override
            public int compare(FieldDescriptor o1, FieldDescriptor o2) {
                return Integer.compare(o1.getIndex(), o2.getIndex());
            }
        });

        assertFieldDescriptors(fieldDescriptorList);

        this.fieldDescriptorList = Collections.unmodifiableList(fieldDescriptorList);
    }

    private void assertFieldDescriptors(List<FieldDescriptor> fieldDescriptorList) {
        final int size = fieldDescriptorList.size();

        Set<String> metricNameSet = new HashSet<String>(size);

        for (int i = 0; i < size; i++) {
            final FieldDescriptor fieldDescriptor = fieldDescriptorList.get(i);

            if (fieldDescriptor == null) {
                throw new NullPointerException("fieldDescriptor");
            }

            final int fieldDescriptorIndex = fieldDescriptor.getIndex();
            if (fieldDescriptorIndex != i) {
                throw new IllegalArgumentException("Index must be in the order of 0,1,2,3,4,...");
            }

            final String fieldName = fieldDescriptor.getName();

            final boolean newValue = metricNameSet.add(fieldName);
            if (!newValue) {
                throw new IllegalArgumentException("Not support duplicated field name.");
            }
        }
    }

    public FieldDescriptor get(int index) {
        return fieldDescriptorList.get(index);
    }

    public List<FieldDescriptor> getAll() {
        return fieldDescriptorList;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FieldDescriptors{");
        sb.append("fieldDescriptorList=").append(fieldDescriptorList);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {

        private final List<FieldDescriptor> fieldDescriptorList = new ArrayList<>();

        public void add(FieldDescriptor fieldDescriptor) {
            Objects.requireNonNull(fieldDescriptor, "fieldDescriptor");
            this.fieldDescriptorList.add(fieldDescriptor);
        }

        public FieldDescriptors build() {
            return new FieldDescriptors(fieldDescriptorList);
        }

    }

}
