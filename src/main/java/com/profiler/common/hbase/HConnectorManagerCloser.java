package com.profiler.common.hbase;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseConfigurationFactoryBean;

/**
 *
 */
public class HConnectorManagerCloser implements DisposableBean {
    @Autowired
    private HbaseConfigurationFactoryBean hbaseConfigurationFactoryBean;

    public HConnectorManagerCloser() {
        System.out.println("start-------------");
    }

    public HbaseConfigurationFactoryBean getHbaseConfigurationFactoryBean() {
        return hbaseConfigurationFactoryBean;
    }

    public void setHbaseConfigurationFactoryBean(HbaseConfigurationFactoryBean hbaseConfigurationFactoryBean) {
        this.hbaseConfigurationFactoryBean = hbaseConfigurationFactoryBean;
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("-------------------------");
        if (hbaseConfigurationFactoryBean != null) {
            System.out.println("---------------------------------");
            hbaseConfigurationFactoryBean.destroy();
        }
    }
}
