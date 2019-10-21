/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.common.hbase;

import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory for creating HBase specific configuration. By default cleans up any connection associated with the current configuration.
 *
 * @author Costin Leau
 */
public class HbaseConfigurationFactoryBean implements InitializingBean, FactoryBean<Configuration> {

    private Configuration configuration;
    private Configuration hadoopConfig;
    private Properties properties;

    /**
     * Sets the Hadoop configuration to use.
     *
     * @param configuration The configuration to set.
     */
    public void setConfiguration(Configuration configuration) {
        this.hadoopConfig = configuration;
    }

    /**
     * Sets the configuration properties.
     *
     * @param properties The properties to set.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void afterPropertiesSet() {
        configuration = (hadoopConfig != null ? HBaseConfiguration.create(hadoopConfig) : HBaseConfiguration.create());
        addProperties(configuration, properties);
    }
    
    /**
     * Adds the specified properties to the given {@link Configuration} object.  
     * 
     * @param configuration configuration to manipulate. Should not be null.
     * @param properties properties to add to the configuration. May be null.
     */
    private void addProperties(Configuration configuration, Properties properties) {
        Objects.requireNonNull(configuration, "A non-null configuration is required");
        if (properties != null) {
            Enumeration<?> props = properties.propertyNames();
            while (props.hasMoreElements()) {
                String key = props.nextElement().toString();
                configuration.set(key, properties.getProperty(key));
            }
        }
    }

    public Configuration getObject() {
        return configuration;
    }

    public Class<? extends Configuration> getObjectType() {
        return (configuration != null ? configuration.getClass() : Configuration.class);
    }

    public boolean isSingleton() {
        return true;
    }
}