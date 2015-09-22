/**
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;

/**
 * @author Jongho Moon
 *
 */
public class TestTcpDataSender implements EnhancedDataSender {
    private final List<TBase<?, ?>> datas = new ArrayList<TBase<?, ?>>();
    private final Map<Integer, String> apiIdMap = new HashMap<Integer, String>();
    private final Map<String, Integer> apiDescriptionMap = new HashMap<String, Integer>();
    
    private final Map<String, Integer> sqlMap = new HashMap<String, Integer>();
    private final Map<Integer, String> sqlIdMap = new HashMap<Integer, String>();
    
    private final Map<String, Integer> stringMap = new HashMap<String, Integer>();
    private final Map<Integer, String> stringIdMap = new HashMap<Integer, String>();
    
    private static final Comparator<Map.Entry<Integer, String>> COMPARATOR = new Comparator<Map.Entry<Integer, String>>() {

        @Override
        public int compare(Entry<Integer, String> o1, Entry<Integer, String> o2) {
            return o1.getKey() > o2.getKey() ? 1 : (o1.getKey() < o2.getKey() ? -1 : 0);
        }
        
    };
    

    @Override
    public boolean send(TBase<?, ?> data) {
        addData(data);
        return false;
    }

    private void addData(TBase<?, ?> data) {
        if (data instanceof TApiMetaData) {
            TApiMetaData md = (TApiMetaData)data;
            
            String api = md.getApiInfo();
            if (md.getLine() != -1) {
                api += ":" + md.getLine();
            }
            
            apiIdMap.put(md.getApiId(), api);
            apiDescriptionMap.put(api, md.getApiId());
        } else if (data instanceof TSqlMetaData) {
            TSqlMetaData md = (TSqlMetaData)data;
            
            int id = md.getSqlId();
            String sql = md.getSql();
            
            sqlMap.put(sql, id);
            sqlIdMap.put(id, sql);
        } else if (data instanceof TStringMetaData) {
            TStringMetaData md = (TStringMetaData)data;
            
            int id = md.getStringId();
            String string = md.getStringValue();
            
            stringMap.put(string, id);
            stringIdMap.put(id, string);
        }
        
        datas.add(data);
    }

    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public boolean isNetworkAvailable() {
        return false;
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
