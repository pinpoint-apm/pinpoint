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
import com.navercorp.pinpoint.rpc.client.PinpointSocketReconnectEventListener;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;

/**
 * @author Jongho Moon
 *
 */
public class TestTcpDataSender implements EnhancedDataSender {
    private final List<TBase<?, ?>> datas = new ArrayList<TBase<?, ?>>();
    private final Map<Integer, String> idMap = new HashMap<Integer, String>();
    private final Map<String, Integer> descriptionMap = new HashMap<String, Integer>();

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
            
            idMap.put(md.getApiId(), api);
            descriptionMap.put(api, md.getApiId());
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
    public boolean addReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
        return false;
    }

    @Override
    public boolean removeReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
        return false;
    }
    
    public String getApiDescirption(int id) {
        return idMap.get(id);
    }

    public int getApiId(String description) {
        Integer id = descriptionMap.get(description);
        
        if (id == null) {
            throw new NoSuchElementException(description);
        }
        
        return id;
    }
    

    public List<TBase<?, ?>> getDatas() {
        return datas;
    }
    
    public void clear() {
        datas.clear();
    }
    
    public void printApis(PrintStream out) {
        List<Map.Entry<Integer, String>> apis = new ArrayList<Map.Entry<Integer, String>>(idMap.entrySet());
        
        Collections.sort(apis, new Comparator<Map.Entry<Integer, String>>() {

            @Override
            public int compare(Entry<Integer, String> o1, Entry<Integer, String> o2) {
                return o1.getKey() > o2.getKey() ? 1 : (o1.getKey() < o2.getKey() ? -1 : 0);
            }
            
        });
        
        for (Map.Entry<Integer, String> api : apis) {
            out.println(api.getKey() + ": " + api.getValue());
        }
    }
}
