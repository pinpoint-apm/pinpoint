/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.google.common.collect.Maps;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;
import org.apache.thrift.TBase;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class TestTcpDataSender implements EnhancedDataSender {

    private final List<TBase<?, ?>> datas = Collections.synchronizedList(new ArrayList<TBase<?, ?>>());

    private final BiMap<Integer, String> apiIdMap = newSynchronizedBiMap();

    private final BiMap<Integer, String> sqlIdMap = newSynchronizedBiMap();

    private final BiMap<Integer, String> stringIdMap = newSynchronizedBiMap();

    private static final Comparator<Map.Entry<Integer, String>> COMPARATOR = new Comparator<Map.Entry<Integer, String>>() {

        @Override
        public int compare(Entry<Integer, String> o1, Entry<Integer, String> o2) {
            return o1.getKey() > o2.getKey() ? 1 : (o1.getKey() < o2.getKey() ? -1 : 0);
        }

    };

    private <K, V> BiMap<K, V> newSynchronizedBiMap() {
        BiMap<K, V> hashBiMap = HashBiMap.create();
        return Maps.synchronizedBiMap(hashBiMap);
    }


    @Override
    public boolean send(TBase<?, ?> data) {
        addData(data);
        return false;
    }

    private void addData(TBase<?, ?> data) {
        if (data instanceof TApiMetaData) {
            TApiMetaData md = (TApiMetaData)data;

            final String javaMethodDescriptor = toJavaMethodDescriptor(md);
            apiIdMap.put(md.getApiId(), javaMethodDescriptor);

        } else if (data instanceof TSqlMetaData) {
            TSqlMetaData md = (TSqlMetaData)data;

            int id = md.getSqlId();
            String sql = md.getSql();

            sqlIdMap.put(id, sql);
        } else if (data instanceof TStringMetaData) {
            TStringMetaData md = (TStringMetaData)data;

            int id = md.getStringId();
            String string = md.getStringValue();

            stringIdMap.put(id, string);
        }

        datas.add(data);
    }

    private String toJavaMethodDescriptor(TApiMetaData apiMetaData) {
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
    public boolean request(TBase<?, ?> data) {
        addData(data);
        return true;
    }

    @Override
    public boolean request(TBase<?, ?> data, int retry) {
        addData(data);
        return true;
    }

    @Override
    public boolean request(TBase<?, ?> data, FutureListener<ResponseMessage> listener) {
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
        BiMap<String, Integer> apiDescriptionMap = apiIdMap.inverse();
        Integer id = apiDescriptionMap.get(description);

        if (id == null) {
            throw new NoSuchElementException(description);
        }

        return id;
    }

    public String getString(int id) {
        return stringIdMap.get(id);
    }

    public int getStringId(String string) {
        BiMap<String, Integer> stringMap = stringIdMap.inverse();
        Integer id = stringMap.get(string);

        if (id == null) {
            throw new NoSuchElementException(string);
        }

        return id;
    }

    public String getSql(int id) {
        return sqlIdMap.get(id);
    }

    public int getSqlId(String sql) {
        BiMap<String, Integer> sqlMap = sqlIdMap.inverse();
        Integer id = sqlMap.get(sql);

        if (id == null) {
            throw new NoSuchElementException(sql);
        }

        return id;
    }

    public List<TBase<?, ?>> getDatas() {
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
        List<Map.Entry<Integer, String>> apis = new ArrayList<Map.Entry<Integer, String>>(apiIdMap.entrySet());
        printEntries(out, apis);
    }
    
    public void printStrings(PrintStream out) {
        List<Map.Entry<Integer, String>> strings = new ArrayList<Map.Entry<Integer, String>>(stringIdMap.entrySet());
        printEntries(out, strings);
    }
    
    public void printSqls(PrintStream out) {
        List<Map.Entry<Integer, String>> sqls = new ArrayList<Map.Entry<Integer, String>>(sqlIdMap.entrySet());
        printEntries(out, sqls);
    }


    private void printEntries(PrintStream out, List<Map.Entry<Integer, String>> entries) {
        Collections.sort(entries, COMPARATOR);
        
        for (Map.Entry<Integer, String> e : entries) {
            out.println(e.getKey() + ": " + e.getValue());
        }
    }
    
}
