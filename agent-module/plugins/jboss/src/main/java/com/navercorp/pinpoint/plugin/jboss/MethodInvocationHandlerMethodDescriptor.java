/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jboss;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.MethodType;
import com.navercorp.pinpoint.common.util.LineNumber;

/**
 * The Class MethodInvocationHandlerMethodDescriptor.
 *
 * @author <a href="mailto:suraj.raturi89@gmail.com">Suraj Raturi</a>
 */
public class MethodInvocationHandlerMethodDescriptor implements MethodDescriptor {

    /** The api id. */
    private int apiId = 0;

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getMethodName()
     */
    @Override
    public String getMethodName() {
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getClassName()
     */
    @Override
    public String getClassName() {
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getParameterTypes()
     */
    @Override
    public String[] getParameterTypes() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getParameterVariableName()
     */
    @Override
    public String[] getParameterVariableName() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getParameterDescriptor()
     */
    @Override
    public String getParameterDescriptor() {
        return "()";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getLineNumber()
     */
    @Override
    public int getLineNumber() {
        return LineNumber.NO_LINE_NUMBER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getFullName()
     */
    @Override
    public String getFullName() {
        return MethodInvocationHandlerMethodDescriptor.class.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#setApiId(int)
     */
    @Override
    public void setApiId(final int apiId) {
        this.apiId = apiId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getApiId()
     */
    @Override
    public int getApiId() {
        return apiId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getApiDescriptor()
     */
    @Override
    public String getApiDescriptor() {
        return "Jboss Method Invocation Handler Process";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getType()
     */
    @Override
    public int getType() {
        return MethodType.WEB_REQUEST;
    }

}