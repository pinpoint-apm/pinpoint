package com.nhn.pinpoint.common.hbase;

import java.io.IOException;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.HbaseConfigurationFactoryBean;
import org.springframework.data.hadoop.hbase.HbaseSystemException;

import com.nhn.pinpoint.common.util.PropertyUtils;


/**
 * @author emeroad
 */
public class HbaseTemplate2IT {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static HbaseConfigurationFactoryBean hbaseConfigurationFactoryBean;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-hbase.properties");

        Configuration cfg = HBaseConfiguration.create();
        cfg.set("hbase.zookeeper.quorum", properties.getProperty("hbase.client.host"));
        cfg.set("hbase.zookeeper.property.clientPort", properties.getProperty("hbase.client.port"));
        hbaseConfigurationFactoryBean = new HbaseConfigurationFactoryBean();
        hbaseConfigurationFactoryBean.setConfiguration(cfg);
        hbaseConfigurationFactoryBean.afterPropertiesSet();
    }

    @AfterClass
    public static void afterClass() {
        if (hbaseConfigurationFactoryBean != null) {
            hbaseConfigurationFactoryBean.destroy();
        }

    }


    @Test
    @Ignore
    public void notExist() throws Exception {

        HbaseTemplate2 hbaseTemplate2 = new HbaseTemplate2();
        hbaseTemplate2.setConfiguration(hbaseConfigurationFactoryBean.getObject());
        hbaseTemplate2.afterPropertiesSet();

        try {
            hbaseTemplate2.put("NOT_EXIST", new byte[0], "familyName".getBytes(), "columnName".getBytes(), new byte[0]);
            Assert.fail("exceptions");
        } catch (HbaseSystemException e) {
            if (!(e.getCause().getCause() instanceof TableNotFoundException)) {
                Assert.fail("unexpected exception :" + e.getCause());
            }
        } finally {
            hbaseTemplate2.destroy();
        }


    }
}
