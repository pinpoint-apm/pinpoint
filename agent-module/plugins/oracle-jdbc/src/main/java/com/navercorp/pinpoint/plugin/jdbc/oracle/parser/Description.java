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

package com.navercorp.pinpoint.plugin.jdbc.oracle.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.navercorp.pinpoint.plugin.jdbc.oracle.parser.ParserUtils.compare;

/**
 * @author emeroad
 */
public class Description implements DatabaseSpec {
    public static final String DESCRIPTION = "description";

    private String serviceName;
    private String sid;
    private final ArrayList<Address> addressList = new ArrayList<>();

    public Description() {
    }

    public Description(KeyValue<?> keyValue) {
        Objects.requireNonNull(keyValue, "keyValue");

        mapping(keyValue);
    }


    private void mapping(KeyValue<?> keyValue) {
        if (!compare(DESCRIPTION, keyValue)) {
            throw new OracleConnectionStringException(DESCRIPTION  + " node not found");
        }
        if (!(keyValue instanceof KeyValue.KeyValueList)) {
            return;
        }
        final KeyValue.KeyValueList keyValueList = (KeyValue.KeyValueList) keyValue;
        for (KeyValue<?> kv : keyValueList.getValue()) {
            if (compare("address", kv)) {
                if (kv instanceof KeyValue.KeyValueList) {
                    final KeyValue.KeyValueList addressList = (KeyValue.KeyValueList) kv;
                    String host = null;
                    String port = null;
                    String protocol = null;
                    for (KeyValue<?> addressKv : addressList.getValue()) {
                        if (addressKv instanceof KeyValue.TerminalKeyValue) {
                            final KeyValue.TerminalKeyValue terminal = (KeyValue.TerminalKeyValue) addressKv;
                            if (compare("host", terminal)) {
                                host = terminal.getValue();
                            } else if (compare("port", terminal)) {
                                port = terminal.getValue();
                            } else if (compare("protocol", terminal)) {
                                protocol = terminal.getValue();
                            }
                        }
                    }
                    this.addAddress(protocol, host, port);
                }
            } else if (compare("connect_data", kv)) {
                if (kv instanceof KeyValue.KeyValueList) {
                    final KeyValue.KeyValueList connectDataList = (KeyValue.KeyValueList) kv;
                    for (KeyValue<?> connectData : connectDataList.getValue()) {
                        if (connectData instanceof KeyValue.TerminalKeyValue) {
                            final KeyValue.TerminalKeyValue terminal = (KeyValue.TerminalKeyValue) connectData;
                            if (compare("service_name", terminal)) {
                                this.serviceName = terminal.getValue();
                            } else if (compare("sid", terminal)) {
                                // sid also needed to check compatibility.
                                this.sid = terminal.getValue();
                            }
                        }
                    }
                }
            }
        }
    }

    public String getServiceName() {
        return serviceName;
    }


    void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSid() {
        return sid;
    }

    void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public List<String> getJdbcHost() {
        List<String> hostList = new ArrayList<>();
        for(Address address : addressList) {
            String host = address.getHost();
            String port = address.getPort();
            if (port == null) {
                // set default port
                port = "1521";
            }
            hostList.add(host + ":" + port);
        }
        return hostList;
    }

    @Override
    public String getDatabaseId() {
        // Find serviceName first
        String serviceName = getServiceName();
        if(serviceName != null) {
            return serviceName;
        }
        // Use sid if serviceName is not available
        String sid = getSid();
        if (sid != null) {
            return sid;
        }
        return "oracleDatabaseId not found";
    }


    void addAddress(String protocol, String host, String port) {
        this.addressList.add(new Address(protocol, host, port));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Description that = (Description) o;

        if (addressList != null ? !addressList.equals(that.addressList) : that.addressList != null) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (sid != null ? !sid.equals(that.sid) : that.sid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + (sid != null ? sid.hashCode() : 0);
        result = 31 * result + (addressList != null ? addressList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Description");
        sb.append("{serviceName='").append(serviceName).append('\'');
        sb.append(", sid='").append(sid).append('\'');
        sb.append(", addressList=").append(addressList);
        sb.append('}');
        return sb.toString();
    }
}
