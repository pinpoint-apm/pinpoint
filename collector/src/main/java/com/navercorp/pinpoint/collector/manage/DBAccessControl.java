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

package com.navercorp.pinpoint.collector.manage;

import com.navercorp.pinpoint.common.hbase.AccessControlOperations;

/**
 * @author Taejin Koo
 */
public class DBAccessControl extends AbstractPinpointCollectorMBean implements DBAccessControlMBean {
    
    private final AccessControlOperations accessControlOperations;
    
    public DBAccessControl(AccessControlOperations accessControlOperations) {
        this.accessControlOperations = accessControlOperations;
    }
    
    @Override
    public void enableAccess() {
        accessControlOperations.enableAccess();
    }

    @Override
    public void disableAccess() {
        accessControlOperations.disableAccess();
    }

    @Override
    public boolean isEnableAccess() {
        return accessControlOperations.isEnableAccess();
    }

}
