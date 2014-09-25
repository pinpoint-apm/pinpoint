package com.nhn.pinpoint.collector.dao.hbase;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author emeroad
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class HbaseApplicationTraceIndexDaoTest {

    @Autowired
    @Qualifier("applicationTraceIndexDistributor")
    private RowKeyDistributorByHashPrefix distributorByHashPrefix;

    @Test
    public void testInsert() throws Exception {
//        distributorByHashPrefix.getOriginalKey(Bytes.read(1));
    }
}
