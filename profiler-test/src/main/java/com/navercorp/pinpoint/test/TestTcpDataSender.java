/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaData;
import com.navercorp.pinpoint.profiler.metadata.StringMetaData;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;
import com.navercorp.pinpoint.test.util.ObjectUtils;
import com.navercorp.pinpoint.test.util.Pair;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class TestTcpDataSender implements EnhancedDataSender<Object> {

    private final List<Object> datas = Collections.synchronizedList(new ArrayList<>());

    private final Map<Integer, String> apiIdMap = newSynchronizedMap();

    private final Map<Integer, String> sqlIdMap = newSynchronizedMap();

    private final Map<Integer, String> stringIdMap = newSynchronizedMap();

    private static final Comparator<Pair<Integer, String>> COMPARATOR = new Comparator<Pair<Integer, String>>() {
        @Override
        public int compare(Pair<Integer, String> o1, Pair<Integer, String> o2) {
            final int key1 = o1.getKey();
            final int key2 = o2.getKey();
            return Integer.compare(key1, key2);
        }
    };

    private <K, V> Map<K, V> newSynchronizedMap() {
        return Collections.synchronizedMap(new HashMap<K, V>());
    }

    @Override
    public boolean send(Object data) {
        addData(data);
        return false;
    }

    private void addData(Object data) {
        if (data instanceof ApiMetaData) {
            ApiMetaData md = (ApiMetaData)data;

            final String javaMethodDescriptor = toJavaMethodDescriptor(md);
            apiIdMap.put(md.getApiId(), javaMethodDescriptor);
        } else if (data instanceof SqlMetaData) {
            SqlMetaData md = (SqlMetaData)data;

            int id = md.getSqlId();
            String sql = md.getSql();

            sqlIdMap.put(id, sql);
        } else if (data instanceof StringMetaData) {
            StringMetaData md = (StringMetaData)data;

            int id = md.getStringId();
            String string = md.getStringValue();

            stringIdMap.put(id, string);
        }

        datas.add(data);
    }

    private String toJavaMethodDescriptor(ApiMetaData apiMetaData) {
//        1st method type check
//        int type = apiMetaData.getType();
//        if (type != MethodType.DEFAULT) {
//            return apiMetaData.getApiInfo();
//        }

//       2st Descriptor check
        String apiInfo = apiMetaData.getApiInfo();
        if (apiInfo.indexOf('(') == -1) {
            // exceptional case
            // eg : async or internal tag api
            return apiInfo;
        }
        return MethodDescriptionUtils.toJavaMethodDescriptor(apiInfo);
    }

    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public boolean request(Object data) {
        addData(data);
        return true;
    }

    @Override
    public boolean request(Object data, int retry) {
        addData(data);
        return true;
    }

    @Override
    public boolean request(Object data, FutureListener<ResponseMessage> listener) {
        addData(data);
        return true;
    }

    @Override
    public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return false;
    }

    @Override
    public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return false;
    }

    public String getApiDescription(int id) {
        return apiIdMap.get(id);
    }

    public int getApiId(String description) {
        return findIdByValue(apiIdMap, description);
    }

    private Integer findIdByValue(Map<Integer, String> map, String value) {
        final Set<Entry<Integer, String>> entries = map.entrySet();
        synchronized (map) {
            for (Entry<Integer, String> entry : entries) {
                if (ObjectUtils.equals(entry.getValue(), value)) {
                    return entry.getKey();
                }
            }
        }
        throw new NoSuchElementException(value);
    }

    public String getString(int id) {
        return stringIdMap.get(id);
    }

    public int getStringId(String string) {
        return findIdByValue(stringIdMap, string);
    }

    public String getSql(int id) {
        return sqlIdMap.get(id);
    }

    public int getSqlId(String sql) {
        return findIdByValue(stringIdMap, sql);
    }

    public List<Object> getDatas() {
        return datas;
    }

    public void clear() {
        datas.clear();
    }

    public void printDatas(PrintStream out) {
        out.println("API(" + apiIdMap.size() + "):");
        printApis(out);
        out.println("SQL(" + sqlIdMap.size() + "):");
        printSqls(out);
        out.println("STRING(" + stringIdMap.size() + "):");
        printStrings(out);
    }
    
    public void printApis(PrintStream out) {
        List<Pair<Integer, String>> apis = syncCopy(apiIdMap);
        printEntries(out, apis);
    }
    
    public void printStrings(PrintStream out) {
        List<Pair<Integer, String>> strings = syncCopy(stringIdMap);
        printEntries(out, strings);
    }

    public void printSqls(PrintStream out) {
        List<Pair<Integer, String>> sqls = syncCopy(sqlIdMap);
        printEntries(out, sqls);
    }

    private <K, V> List<Pair<K, V>> syncCopy(Map<K, V> map) {
        List<Pair<K, V>> list = new ArrayList<>(map.size());

        Set<Entry<K, V>> entries = map.entrySet();
        synchronized (map) {
            for (Entry<K, V> entry : entries) {
                list.add(new Pair<>(entry.getKey(), entry.getValue()));
            }
            return list;
        }
    }

    private void printEntries(PrintStream out, List<Pair<Integer, String>> entries) {
        Collections.sort(entries, COMPARATOR);
        for (Pair<Integer, String> e : entries) {
            out.println(e.getKey() + ": " + e.getValue());
        }
    }
    
}
