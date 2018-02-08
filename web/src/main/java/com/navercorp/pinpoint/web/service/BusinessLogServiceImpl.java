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


package com.navercorp.pinpoint.web.service;


import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.navercorp.pinpoint.web.dao.businesslog.BusinessLogDao;

/**
 * [XINGUANG]Created by Administrator on 2017/6/12.
 */
@Service
public class BusinessLogServiceImpl implements BusinessLogService{
    @Autowired
    BusinessLogDao businessLogDao;
    @Override
    public  String getBusinessLog(String agentId, String transactionId,String spanId,long time){
        System.out.println("agentId = "+ agentId + "transactionId = " + transactionId + " ; spanId = " + spanId +  " ; time = " + time);
        List<String> lString =  businessLogDao.getBusinessLog(agentId,transactionId,spanId,time);
        StringBuilder sb = new StringBuilder();
        for (String str : lString){
            sb.append(str);
            sb.append("\r\n");
        }
        System.out.println(sb.toString());
        return sb.toString();
    }
}
