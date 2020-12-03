/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.dao.mysql;

import com.navercorp.pinpoint.batch.dao.AlarmDao;
import com.navercorp.pinpoint.batch.alarm.vo.CheckerResult;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class MysqlAlarmDao implements AlarmDao {

    private static final String NAMESPACE = AlarmDao.class.getPackage().getName() + "." + AlarmDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlAlarmDao(@Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public void insertCheckerResult(CheckerResult checkerResult) {
        sqlSessionTemplate.insert(NAMESPACE + "insertCheckerResult", checkerResult);
    }

    @Override
    public List<CheckerResult> selectBeforeCheckerResultList(String applicationId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectBeforeCheckerResultList", applicationId);
    }


    @Override
    public void deleteCheckerResult(String ruleId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteCheckerResult", ruleId);
    }
}
