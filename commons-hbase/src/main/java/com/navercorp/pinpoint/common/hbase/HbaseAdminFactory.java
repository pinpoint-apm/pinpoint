/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class HbaseAdminFactory implements AdminFactory {

    private final Connection connection;

    public HbaseAdminFactory(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    @Override
    public Admin getAdmin() {
        if (connection.isClosed()) {
            throw new HBaseAccessException("Connection already closed");
        }
        try {
            return connection.getAdmin();
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    @Override
    public void releaseAdmin(Admin admin) {
        if (admin == null) {
            return;
        }
        try {
            admin.close();
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }
}
