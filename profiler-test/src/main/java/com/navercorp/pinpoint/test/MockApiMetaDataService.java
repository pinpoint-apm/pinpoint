/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockApiMetaDataService implements ApiMetaDataService {
    private static final Comparator<Map.Entry<Integer, MethodInfo>> COMPARATOR = new Comparator<Map.Entry<Integer, MethodInfo>>() {

        @Override
        public int compare(Map.Entry<Integer, MethodInfo> o1, Map.Entry<Integer, MethodInfo> o2) {
            return o1.getKey() > o2.getKey() ? 1 : (o1.getKey() < o2.getKey() ? -1 : 0);
        }

    };

    private final BiMap<Integer, MethodInfo> apiIdMap = HashBiMap.create();

    private static final int INITIAL_ID = 1;
    private int nextId = INITIAL_ID;


    public MockApiMetaDataService() {
    }


    @Override
    public int cacheApi(MethodDescriptor methodDescriptor) {
        int apiId1 = methodDescriptor.getApiId();

        final String apiDescriptor = MethodDescriptionUtils.toJavaMethodDescriptor(methodDescriptor.getApiDescriptor());
        MethodInfo methodInfo = new MethodInfo(apiDescriptor, methodDescriptor);
        synchronized (this.apiIdMap) {
            final MethodInfo exist = this.apiIdMap.get(methodInfo);
            if (exist != null) {
                return exist.getMethodDescriptor().getApiId();
            }

            final int apiId = nextId();
            this.apiIdMap.put(apiId, methodInfo);
            return apiId;
        }
    }

    private class MethodInfo {
        private final String apiDescriptor;
        private final MethodDescriptor methodDescriptor;

        public MethodInfo(String apiDescriptor, MethodDescriptor methodDescriptor) {
            if (apiDescriptor == null) {
                throw new NullPointerException("apiDescriptor must not be null");
            }
            this.apiDescriptor = apiDescriptor;
            this.methodDescriptor = methodDescriptor;
        }

        public String getApiDescriptor() {
            return apiDescriptor;
        }

        public MethodDescriptor getMethodDescriptor() {
            return methodDescriptor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodInfo that = (MethodInfo) o;

            return apiDescriptor != null ? apiDescriptor.equals(that.apiDescriptor) : that.apiDescriptor == null;
        }

        @Override
        public int hashCode() {
            return apiDescriptor != null ? apiDescriptor.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "MethodInfo{" +
                    "apiDescriptor='" + apiDescriptor + '\'' +
                    ", methodDescriptor=" + methodDescriptor +
                    '}';
        }
    }

    private int nextId() {
        synchronized (apiIdMap) {
            return nextId++;
        }
    }

    public int getApiId(String methodDescriptor) {

        MethodInfo key = new MethodInfo(methodDescriptor, null);
        synchronized (this.apiIdMap) {
            BiMap<MethodInfo, Integer> apiIdMap = this.apiIdMap.inverse();
            final Integer id = apiIdMap.get(key);
            if (id == null) {
                throw new NullPointerException("apiMetaDataNotFound " + key);
            }
            System.out.println("getApiId " + methodDescriptor + " id:" + id);
            return id;
        }
    }

    public void clear() {
        synchronized (apiIdMap) {
            this.apiIdMap.clear();
            nextId = INITIAL_ID;
        }
    }

    public void print(PrintStream out) {
        out.println("API(" + apiIdMap.size() + "):");
        printApis(out);

    }

    public void printApis(PrintStream out) {
        synchronized (this.apiIdMap) {
            List<Map.Entry<Integer, MethodInfo>> apis = new ArrayList<Map.Entry<Integer, MethodInfo>>(apiIdMap.entrySet());
            printEntries(out, apis);
        }
    }


    private void printEntries(PrintStream out, List<Map.Entry<Integer, MethodInfo>> entries) {
        Collections.sort(entries, COMPARATOR);

        for (Map.Entry<Integer, MethodInfo> e : entries) {
            MethodInfo methodInfo = e.getValue();
            out.println(e.getKey() + ": " + methodInfo.getApiDescriptor());
        }
    }
}
