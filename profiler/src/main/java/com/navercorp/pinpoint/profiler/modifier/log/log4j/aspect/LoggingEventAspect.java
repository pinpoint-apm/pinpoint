/*
 * Copyright 2014 NAVER Corp.
 *
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

package com.navercorp.pinpoint.profiler.modifier.log.log4j.aspect;

import com.navercorp.pinpoint.profiler.interceptor.aspect.Aspect;
import com.navercorp.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.navercorp.pinpoint.profiler.interceptor.aspect.PointCut;

/**
 * add transaction id
 * @author minwoo.jung
 */
@Aspect
public abstract class LoggingEventAspect {
    
    //!!!!!!threadLocal 객체로 구현해야할듯
    protected String __transactionId = "[[transactionId==123123123]]";

    private String get__transactionId() {
        return __transactionId;
    }

    private void set__transactionId(String __transactionId) {
        this.__transactionId = __transactionId;
    }

    @PointCut
    public Object getMessage() {
        Object message = __getMessage();
        
        if (message instanceof String) {
//            message = this.transactionId + (String)message;
            message = __transactionId + (String)message;
        }
        
        return message;
    }

    @JointPoint
    abstract Object __getMessage();
    
    
    @PointCut
    public String getRenderedMessage() {
//        return this.transactionId + __getRenderedMessage();
        return __transactionId + __getRenderedMessage();
    }

    @JointPoint
    abstract String __getRenderedMessage();
}
