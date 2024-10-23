/*
 * Copyright 2022 NAVER Corp.
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

package com.pinpoint.test.plugin;

import com.mysql.cj.xdevapi.Collection;
import com.mysql.cj.xdevapi.DocResult;
import com.mysql.cj.xdevapi.Schema;
import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SessionFactory;
import com.pinpoint.test.common.view.ApiLinkPage;
import com.pinpoint.test.common.view.HrefTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
public class MysqlDBPluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public MysqlDBPluginController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/")
    String welcome() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();
        List<HrefTag> list = new ArrayList<>();
        for (RequestMappingInfo info : handlerMethods.keySet()) {
            for (String path : info.getDirectPaths()) {
                list.add(HrefTag.of(path));
            }
        }
        list.sort(Comparator.comparing(HrefTag::getPath));
        return new ApiLinkPage("mysql-jdbc-plugin-testweb")
                .addHrefTag(list)
                .build();
    }


    @RequestMapping(value = "/mysql/execute1")
    public String execute1() throws Exception {
        testStatements();
        return "OK";
    }

    @RequestMapping(value = "/mysql/execute2")
    public String execute2() throws Exception {
        testStoredProcedure_with_IN_OUT_parameters();
        return "OK";
    }

    @RequestMapping(value = "/mysql/execute3")
    public String execute3() throws Exception {
        testStoredProcedure_with_INOUT_parameters();
        return "OK";
    }

    @RequestMapping(value = "/mysql/xdev1")
    public String xdev1() throws Exception {
        xdev();
        return "OK";
    }

    void testStatements() throws Exception {

        final Connection conn = getConnection();

        conn.setAutoCommit(false);

        String insertQuery = "INSERT INTO test (name, age) VALUES (?, ?)";
        String selectQuery = "SELECT * FROM test";
        String deleteQuery = "DELETE FROM test";

        PreparedStatement insert = conn.prepareStatement(insertQuery);
        insert.setString(1, "maru");
        insert.setInt(2, 5);
        insert.execute();

        Statement select = conn.createStatement();
        ResultSet rs = select.executeQuery(selectQuery);

        while (rs.next()) {
            final int id = rs.getInt("id");
            final String name = rs.getString("name");
            final int age = rs.getInt("age");
        }

        Statement delete = conn.createStatement();
        delete.executeUpdate(deleteQuery);

        conn.commit();
        conn.close();
    }


    /*  CREATE OR REPLACE PROCEDURE concatCharacters(IN  a CHAR(1), IN  b CHAR(1), OUT c CHAR(2))
        BEGIN
            SET c = CONCAT(a, b);
        END                                             */
    void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        final String param1 = "a";
        final String param2 = "b";
        final String storedProcedureQuery = "{ call concatCharacters(?, ?, ?) }";

        final Connection conn = getConnection();

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setString(1, param1);
        cs.setString(2, param2);
        cs.registerOutParameter(3, Types.VARCHAR);
        cs.execute();


        conn.close();
    }

    /*
        CREATE OR REPLACE PROCEDURE swapAndGetSum(INOUT a INT, INOUT b INT)
        BEGIN
            DECLARE temp INT;
            SET temp = a;
            SET a = b;
            SET b = temp;
            SELECT temp + a;
        END
     */
    void testStoredProcedure_with_INOUT_parameters() throws Exception {
        final int param1 = 1;
        final int param2 = 2;
        final String storedProcedureQuery = "{ call swapAndGetSum(?, ?) }";

        final Connection conn = getConnection();

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setInt(1, param1);
        cs.setInt(2, param2);
        cs.registerOutParameter(1, Types.INTEGER);
        cs.registerOutParameter(2, Types.INTEGER);
        ResultSet rs = cs.executeQuery();

        conn.close();
    }

    void xdev() {
        Session session = new SessionFactory().getSession("mysqlx://localhost:32772/test?user=root&password=");
        Schema schema = session.getSchema("test");

        Collection collection = schema.getCollection("name");
        DocResult docResult = collection.find("name like :param").limit(1).bind("param", "L%").execute();
        System.out.println(docResult.fetchOne());

        session.close();
    }


    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MysqlDBServer.getUri(), "root", "");
    }
}
