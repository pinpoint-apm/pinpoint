package com.navercorp.pinpoint.metric.collector.dao.mysql;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MysqlTestConfig.class)
@TestPropertySource(properties = {"pinpoint.profiles.active=local"})
@WebAppConfiguration
@Transactional
public class MysqlMetricTagDaoTest {

    @Autowired
    private MysqlMetricTagDao mysqlMetricTagDao;

    @Test
    public void insertAndSelectTest() {
        String hostGroupName = "applicationId";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";

        List<Tag> tagList = new ArrayList<>(5);
        tagList.add(new Tag("key1", "value1"));
        tagList.add(new Tag("key2", "value2"));
        tagList.add(new Tag("key3", "value3"));
        tagList.add(new Tag("key4", "value4"));
        tagList.add(new Tag("key5", "value5"));
        tagList.add(new Tag("key6", "value6"));

        MetricTag metricTag = new MetricTag(hostGroupName, hostName, metricName, fieldName, tagList);
        mysqlMetricTagDao.insertMetricTag(metricTag);

        MetricTagCollection metricTagCollection = mysqlMetricTagDao.selectMetricTag(new MetricTagKey(hostGroupName, hostName, metricName, fieldName));
        Assert.assertEquals(metricTagCollection.getMetricTagList().size(), 1);

        mysqlMetricTagDao.insertMetricTag(metricTag);
        MetricTagCollection metricTagCollection2 = mysqlMetricTagDao.selectMetricTag(new MetricTagKey(hostGroupName, hostName, metricName, fieldName));
        Assert.assertEquals(metricTagCollection2.getMetricTagList().size(), 1);

        List<Tag> tagList2 = new ArrayList<>(5);
        tagList2.add(new Tag("A_key1", "A_value1"));
        tagList2.add(new Tag("A_key2", "A_value2"));
        tagList2.add(new Tag("A_key3", "A_value3"));
        tagList2.add(new Tag("A_key4", "A_value4"));
        tagList2.add(new Tag("A_key5", "A_value5"));
        tagList2.add(new Tag("A_key6", "A_value6"));

        MetricTag metricTag2 = new MetricTag(hostGroupName, hostName, metricName, fieldName, tagList2);
        mysqlMetricTagDao.insertMetricTag(metricTag2);

        MetricTagCollection metricTagCollection3 = mysqlMetricTagDao.selectMetricTag(new MetricTagKey(hostGroupName, hostName, metricName, fieldName));
        Assert.assertEquals(metricTagCollection3.getMetricTagList().size(), 2);
    }
}