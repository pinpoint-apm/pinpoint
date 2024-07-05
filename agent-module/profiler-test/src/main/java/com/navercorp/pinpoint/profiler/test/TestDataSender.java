/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.test;

import com.google.common.primitives.UnsignedBytes;
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlUidMetaData;
import com.navercorp.pinpoint.profiler.metadata.StringMetaData;
import com.navercorp.pinpoint.profiler.test.util.BiHashMap;
import com.navercorp.pinpoint.profiler.test.util.Pair;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class TestDataSender implements EnhancedDataSender<MetaDataType> {

    private final List<Object> datas = Collections.synchronizedList(new ArrayList<>());

    private final BiHashMap<Integer, String> apiIdMap = newBiHashMap();

    private final BiHashMap<Integer, String> sqlIdMap = newBiHashMap();

    private final BiHashMap<byte[], String> sqlUidMap = newBiHashMap();

    private final BiHashMap<Integer, String> stringIdMap = newBiHashMap();

    private static final Comparator<Pair<Integer, ?>> COMPARATOR = Comparator.comparingInt(Pair::getKey);
    private static final Comparator<Pair<byte[], ?>> BYTES_COMPARATOR = Comparator.comparing(Pair::getKey, UnsignedBytes.lexicographicalComparator());

    private <K, V> BiHashMap<K, V> newBiHashMap() {
        return new BiHashMap<>();
    }

    @Override
    public boolean send(MetaDataType data) {
        addData(data);
        return false;
    }

    private void addData(MetaDataType data) {
        if (data instanceof ApiMetaData) {
            ApiMetaData md = (ApiMetaData)data;

            int apiId = md.getApiId();
            String javaMethodDescriptor = toJavaMethodDescriptor(md);

            syncPut(this.apiIdMap, apiId, javaMethodDescriptor);
        } else if (data instanceof SqlMetaData) {
            SqlMetaData md = (SqlMetaData) data;

            int id = md.getSqlId();
            String sql = md.getSql();

            syncPut(sqlIdMap, id, sql);
        } else if (data instanceof SqlUidMetaData) {
            SqlUidMetaData md = (SqlUidMetaData) data;

            byte[] uid = md.getSqlUid();
            String sql = md.getSql();

            syncPut(sqlUidMap, uid, sql);
        } else if (data instanceof StringMetaData) {
            StringMetaData md = (StringMetaData) data;

            int id = md.getStringId();
            String string = md.getStringValue();

            syncPut(stringIdMap, id, string);
        }

        datas.add(data);
    }

    private <K, V> V syncPut(BiHashMap<K, V> map, K key, V value) {
        synchronized (map) {
            return map.put(key, value);
        }
    }

    private <K, V> V syncGet(BiHashMap<K, V> map, K key) {
        synchronized (map) {
            return map.get(key);
        }
    }

    private <K, V> int syncSize(BiHashMap<K, V> map) {
        synchronized (map) {
            return map.size();
        }
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
    public boolean request(MetaDataType data) {
        addData(data);
        return true;
    }

    @Override
    public boolean request(MetaDataType data, int retry) {
        addData(data);
        return true;
    }


    public String getApiDescription(int id) {
        return syncGet(apiIdMap, id);
    }

    public int getApiId(String description) {
        return findIdByValue(apiIdMap, description);
    }

    public String getString(int id) {
        return syncGet(stringIdMap, id);
    }

    public int getStringId(String string) {
        return findIdByValue(stringIdMap, string);
    }

    public String getSql(int id) {
        return syncGet(sqlIdMap, id);
    }

    public int getSqlId(String sql) {
        return findIdByValue(sqlIdMap, sql);
    }

    public String getSql(byte[] uid) {
        return syncGet(sqlUidMap, uid);
    }

    public byte[] getSqlUid(String sql) {
        return findIdByValue(sqlUidMap, sql);
    }

    private <K> K findIdByValue(BiHashMap<K, String> map, String value) {
        synchronized (map) {
            K id = map.reverseGet(value);
            if (id == null) {
                throw new NoSuchElementException(value);
            }
            return id;
        }
    }

    public List<Object> getDatas() {
        return datas;
    }

    public void clear() {
        datas.clear();
    }

    public void printDatas(PrintStream out) {
        out.println("API(" + syncSize(apiIdMap) + "):");
        printApis(out);
        out.println("SQL(" + syncSize(sqlIdMap) + "):");
        printSqls(out);
        out.println("SQLUID(" + syncSize(sqlUidMap) + "):");
        printSqlUids(out);
        out.println("STRING(" + syncSize(stringIdMap) + "):");
        printStrings(out);
    }
    
    public void printApis(PrintStream out) {
        List<Pair<Integer, String>> apis = syncCopy(apiIdMap);
        apis.sort(COMPARATOR);
        List<String> apiList = toStringList(apis);
        printEntries(out, apiList);
    }
    
    public void printStrings(PrintStream out) {
        List<Pair<Integer, String>> strings = syncCopy(stringIdMap);
        strings.sort(COMPARATOR);
        List<String> strList = toStringList(strings);
        printEntries(out, strList);
    }

    public void printSqls(PrintStream out) {
        List<Pair<Integer, String>> sqls = syncCopy(sqlIdMap);
        sqls.sort(COMPARATOR);
        List<String> sqlList = toStringList(sqls);
        printEntries(out, sqlList);
    }

    public void printSqlUids(PrintStream out) {
        List<Pair<byte[], String>> sqlUids = syncCopy(sqlUidMap);
        sqlUids.sort(BYTES_COMPARATOR);
        List<String> sqlUidList = toStringList(sqlUids);
        printEntries(out, sqlUidList);
    }

    private <K, V> List<String> toStringList(List<Pair<K, V>> list) {
        List<String> result = new ArrayList<>(list.size());
        for (Pair<K, V> pair : list) {
            result.add(toStringPair(pair));
        }
        return result;
    }

    public <K, V> String toStringPair(Pair<K, V> input) {
        return input.getKey() + ":" + input.getValue();
    }

    private <K, V> List<Pair<K, V>> syncCopy(BiHashMap<K, V> map) {
        synchronized (map) {
            List<Pair<K, V>> list = new ArrayList<>(map.size());
            for (Entry<K, V> entry : map.entrySet()) {
                list.add(new Pair<>(entry.getKey(), entry.getValue()));
            }
            return list;
        }
    }


    private void printEntries(PrintStream out, List<String> list) {
        for (String str : list) {
            out.println(str);
        }
    }
    
}
