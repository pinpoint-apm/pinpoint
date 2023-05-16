/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.mybatis;

import com.navercorp.pinpoint.pinot.datasource.WrappedPinotConnection;
import com.navercorp.pinpoint.pinot.datasource.WrappedPinotPreparedStatement;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hyunjoon Cho
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:/pinot/applicationContext-pinot-test.xml"})
public class MybatisTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Disabled
    @Test
    public void testDataSource() throws Exception {
        Connection connection = dataSource.getConnection();
        assertThat(connection).isInstanceOf(WrappedPinotConnection.class);
        PreparedStatement preparedStatement = connection.prepareStatement("testSQL");
        assertThat(preparedStatement).isInstanceOf(WrappedPinotPreparedStatement.class);
        connection.close();
    }

    @Disabled
    @Test
    public void testFactory() {
        SqlSession session = sqlSessionFactory.openSession();
        logger.info("session: {}", session);
    }

    @Disabled
    @Test
    public void testTemplate() {
        logger.info("sqlSessionTemplate: {}", sqlSessionTemplate);
        logger.info("selectList: {}", sqlSessionTemplate.selectList(MybatisTest.class.getPackage().getName() + "." + MybatisTest.class.getSimpleName() + "." + "selectMetricName", "hyunjoon.cho"));
    }
}
