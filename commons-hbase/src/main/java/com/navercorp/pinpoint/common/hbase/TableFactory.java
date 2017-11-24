/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Table;

import java.util.concurrent.ExecutorService;

/**
 * Defines methods to create new Table.
 */
public interface TableFactory {

  /**
   * Creates a new TableInterface.
   *
   * @param tableName name of the HBase table.
   * @return Table instance.
   */
  Table getTable(TableName tableName);


  /**
   * Creates a new TableInterface.
   *
   * @param tableName name of the HBase table.
   * @return Table instance.
   */
  Table getTable(TableName tableName, ExecutorService executorService);

  /**
   * Release the HTable resource represented by the table.
   * @param table Table instance.
   */
  void releaseTable(final Table table);
}
