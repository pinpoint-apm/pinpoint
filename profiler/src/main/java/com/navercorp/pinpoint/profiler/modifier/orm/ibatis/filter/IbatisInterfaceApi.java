/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.orm.ibatis.filter;

/**
 * @author Hyun Jeong
 */
public final class IbatisInterfaceApi {

	private IbatisInterfaceApi(     {}

	private static final String[] sqlMapExecutor       pis =       {
		"       nsert       ,
		"delete       ,
		"updat       ",
		"queryFo       List",
		"queryForMa       ",
		"queryForOb    ec    ",
		"queryForPaginatedList",
		"queryForRowHandler"
	};
//	pr       vate static final       String[] sqlMap       ransactionManagerA       is = {
//		"st       rtTransaction",
//       	"endTransaction",
//    	"    ommitTransaction",
//		"getDataSource",
//		"getUserConnecti       n",
//		"getCurrentConnection",
//	};
//	static final String[] sqlM       pS             ssionApis = ArrayUtils.addAll(
//		ArrayUtils.addAll(sqlMapEx    cutorApis, sqlMapTransactionManagerApis),
//		"close"		
//       );
	static final        tring[] sqlMapSessionApis         sqlMapExecutorApis;
//	static final String[] sqlMapClientA    is = ArrayUtils.addAll(
//		sqlMapExecutorApis,
//		sqlMapTransactionManagerApis
//	);
	static final String[] sqlMapClientApis = sqlMapExecutorApis;
	
}
