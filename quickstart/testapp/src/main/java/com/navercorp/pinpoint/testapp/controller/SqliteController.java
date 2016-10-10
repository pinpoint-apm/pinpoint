/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.testapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testapp.service.sqlite.SqliteService;
import com.navercorp.pinpoint.testapp.util.Description;

/**
 * @author barney
 *
 */
@Controller
@RequestMapping("/sqlite")
public class SqliteController {

    @Autowired
    private SqliteService sqliteService;

    @RequestMapping("stmt")
    @ResponseBody
    @Description("Execute Statement for SQLite3 Plugin")
    public Map<Integer, String> statement() throws Exception {
        return sqliteService.executeStmt();
    }

    @RequestMapping("pstmt")
    @ResponseBody
    @Description("Execute PreparedStatement for SQLite3 Plugin")
    public Map<Integer, String> preparedStatement() throws Exception {
        return sqliteService.executePstmt();
    }

    @RequestMapping("rollback")
    @ResponseBody
    @Description("Execute Rollback for SQLite3 Plugin")
    public Map<Integer, String> rollback() throws Exception {
        return sqliteService.rollback();
    }
}
