/*
 * Copyright 2020 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jboss.descriptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.MethodType;

/**
 * The Class RemotingInvocationMethodDescriptor.
 *
 * @author <a href="mailto:guillermoadrianmolina@hotmail.com">Guillermo Adrian
 *         Molina</a>
 * @author Guillermo Adrian Molina
 */
public class RemotingInvocationMethodDescriptor implements MethodDescriptor {

    /** The api id. */
    private int apiId = 0;

    /** The type. */
    private int type = MethodType.INVOCATION;

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getMethodName()
     */
    @Override
    public String getMethodName() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getClassName()
     */
    @Override
    public String getClassName() {
        return null;
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
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getFullName()
     */
    @Override
    public String getFullName() {
        return RemotingInvocationMethodDescriptor.class.getName();
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
        return "Jboss Remoting Invocation";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.context.MethodDescriptor#getType()
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(final int type) {
        this.type = type;
    }
}