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

package com.navercorp.pinpoint.profiler.modifier.orm.mybatis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;

/**
 * @author Hyun Jeong
 */
public abstract class MyBatisClientModifierTest extends BasePinpointTest {

    public static final int NOT_CACHED =        ;
	
	protected abstract SqlSession getSqlSe       sion    );
	
	@Before
	public void setUp() t       rows Exception {
		MockitoAnnot          tio    s.initMocks(this);
	}
	
	@After
	publi        void cleanUp() thro         Ex    eption {
		getSqlSession().close();
	}

	@Test
	public void n       llP       rameterShouldNotBeTraced(        th       ows Exception {
		// When
		getSqlSession().insert(null)
		// Then
		final List<SpanEvent             o> spanE       ents = getCurrentSpanEvents();
		assertThat(spanEven       s.size(), is(1));
		
		// Check Method
		final SpanEve             tBo insertS       anEventBo = spanEvents.get(0);
		assertThat(inse        Spa    EventBo.getApiId(), not(NOT_CACHED));
		
		// Check Pa       ame       er
		assertNull(insertSpanEventBo.g       tAnnotationBoList());
	}

	@Test
	public        oid       selectOneShouldBe        ace    () throws Exception {
		// When
		getSqlSession().selec       One       "selectOne");
		getSqlSession().selec       One("selectOne", null);
		// Then
		assertN       perations(2);
	}

	@Test
	public void selectListS       oul       BeTraced() throws          Ex    eption {
		// When
		getSqlSession().selectList("selec       List       );
       	getSqlSession().selectList("selectList",       null);
		getSqlSession().selectList("selectList       , null, null);
		// Then
		assertNOperations(3);
	}
	       	@T       st
	public void s        ect    apShouldBeTraced() throws Exception {
		// Given
		       / W       en
		getSqlSession().selectMap("sel       ctMap", null);
		getSqlSession().selectMa       ("selectMap", null, null);
		getSqlSession().se       ect       ap("selectMap", n          ll     null, null);
		// Then
		assertNOperations(3);
	}
       	@T       st
	public void selectShouldB       Traced() throws Exception {
		// When
		get       qlS       ssion().select("s          le    t", null);
		getSqlSession().select("select", null,       nul       );
		getSqlSession().select("       elect", null, null, null);
		// Then
		asse       tNO       erations(3);
	}
	          	@    est
	public void insertShouldBeTraced() throws Exce       tio        {
		// When
		getSqlSession(       .insert("insert");
		getSqlSession().insert       "in       ert", new Object(        ;
		// Then
		assertNOperations(2);
	}
	
	@Test    	pu    lic void updateShouldBeTraced() throws Exception {
       	//       When
		getSqlSession(       .update("update");
		getS       lSe       sion().update("update", new Object());
		// Then
		asser       NOperations(2);
	}
	
	@Test
	publ             c void delet       ShouldBeTraced() throws Exception {
		// When
		getSqlSess       on().delete("delete");
		getSqlSession().delete("delete",        ew Object());
		// Then
		assertNOperations(2);
	}

	@Ignore       // Changed to trace only query operations
	@Test
	public voi        commitShouldBeTraced() throws Exception {
		// When
		getSqlSession().commit();
		g             tSqlSession       ).commit(true);
		// Then
		final List<SpanEventBo> sp       nEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));
		
		// Check MethodInfo
		final Span        entBo commitWith0ArgSpanEvent = spanEvents.get(    );
    	final SpanEventBo commitWith1ArgSpanEvent = spanEven       s.g       t(1);
		assertThat(comm       tWith0ArgSpanEvent.getApiId       ),        ot(NOT_CACHED));
		assertThat(commitWith1ArgSpanEvent.ge       ApiId(), not(NOT_CACHED));
		asse             tThat(commit       ith0ArgSpanEvent.getApiId(), not(commitWith1ArgSpanEvent.get       piId()));
		
		// Check Parameter
		assertNull(commitWith0Ar       SpanEvent.getAnnotationBoList());
		assertThat(commitWith1ArgS       anEvent.getAnnotationBoList().get(0).getKey(), is(AnnotationKe       .CACHE_ARGS0.getCode()));
	}

	@Ignore // Changed to trace only query operations
	@Test
             public void       rollbackShouldBeTraced() throws Exception {
		// When
		       etSqlSession().rollback();
		getSqlSession().rollback(true);
		// Then
		final List<SpanEventBo> spanEvents = get        rrentSpanEvents();
		assertThat(spanEvents.size    ),     s(2));
		
		// Check MethodInfo
		final SpanEventBo rollback       ith       ArgSpanEvent = spanEvents.get(       );
       	final SpanEventBo rollbackWith1ArgSpanEvent = spanEvent       .get(1);
		assertThat(rollbackWit             0ArgSpanEven       .getApiId(), not(NOT_CACHED));
		assertThat(rollbackWith1Ar       SpanEvent.getApiId(), not(NOT_CACHED));
		assertThat(rollbackW       th0ArgSpanEven       .getApiId(), not(rollbackWith1ArgSpanEvent.getApiId()))
		// Check Parameter
		assertNull(rollbackWi    h0A    gSpanEvent.getAnnotationBoList());
		assertThat(ro       lba       kWith1ArgSpanEvent.g       tAn       otationBoList().get(0).getKey(), is(AnnotationKey.CACHE_       RGS0.getCode()));
	}

	@Ignore //             Changed to t       ace only query operations
	@Test
	public void flu       hStatementsShouldBeTraced() throws Exception {
		//             When
		getS       lSession().flushStatements();
		// Then
		fin         List<SpanEventBo> spanEvents = getCurrentSpanE    ent    ();
		assertThat(spanEvents.size(), is(1));
		
		// Check Met       odI       fo
		final SpanEventBo flushSta       eme       tsSpanEvent = spanEvents.get(0);
		assertThat(flushState       entsSpanEvent.getApiId(), not(NOT             CACHED));

	       // Check Parameter
		assertNull(flushStatementsSpanEvent.get       nnotationBoList());
	}

	@Ignore // Changed to trace only quer              operations       	@Test
	public void closeShouldBeTraced() throws Excepti         {
		// When
		getSqlSession().close();
		// Th    n
	    final List<SpanEventBo> spanEvents = getCurrentSpanEve       ts()
		assertThat       spa       Events.size(), is(1));
		
		// Check M       tho       Info
		final SpanEventBo closeSpanEvent = spanEvents.get       0);
		assertThat(closeSpanEvent.g             tApiId(), no       (NOT_CACHED));
		
		// Check Parameter
		assertNull(close       panEvent.getAnnotationBoList());
	}

	@Ignore // Changed to             trace only        uery operations
	@Test
	public void getConfigurationShouldBeTraced() throws Exception {
		// When
		getSqlSess        n().getConfiguration();
		// Then
		final List<    pan    ventBo> spanEvents = getCurrentSpanEvents();
		assertThat(       pan       vents.size(), is(1));
		
		/        Ch       ck MethodInfo
		final SpanEventBo getConfigurationSpanEv       nt = spanEvents.get(0);
		assertT             at(getConfig       rationSpanEvent.getApiId(), not(NOT_CACHED));
		
		// Che       k Parameter
		assertNull(getConfigurationSpanEvent.getAnnot             tionBoList(       );
	}

	@Ignore // Changed to trace only query operat          ons
	@Test
	public void getMapperShouldBeTraced(        throws Exception {
		// Given
		class SomeBean {}
		//        hen
		getSqlSession().getMapper(SomeBean.clas             );
		// Then
		final List<SpanEventBo> spanEvents         getCurrentSpanEvents();
		assertThat          spanEvents.size(), is(1));
		
		// Check Meth          dInfo
		final SpanEventBo getConnect          onSpanEvent =          spanEvents.get(0);
		assertThat(getConnectionSp          nEvent.getAp          Id(), not(NOT_CACHED));
		
		// Check Parameter
		assertThat(getConnect          onSpanEvent.getAnnotationBoList().g          t(0).getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
	}
          	@Ignore // Changed to trace only query operations
	@Test
	public void getConne             tionShouldBeTraced() throws Exception {
		//        hen
		getSqlSession().getConnection();
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));
		
		// Check MethodInfo
		final SpanEventBo getConnectionSpanEvent = spanEvents.get(0);
		assertThat(getConnectionSpanEvent.getApiId(), not(NOT_CACHED));
		
		// Check Parameter
		assertNull(getConnectionSpanEvent.getAnnotationBoList());
	}
	
	private void assertNOperations(int numOperations) {
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(numOperations));
		
		final Set<Integer> uniqueApiIds = new HashSet<Integer>();
		for (int n = 0; n < numOperations; ++n) {
			final SpanEventBo apiSpanEvent = spanEvents.get(n);
			uniqueApiIds.add(apiSpanEvent.getApiId());
			// Check MethodInfo
			assertThat(apiSpanEvent.getApiId(), not(NOT_CACHED));
			// Check Parameter
			final List<AnnotationBo> apiAnnotations = apiSpanEvent.getAnnotationBoList();
			assertThat(apiAnnotations.size(), is(1));
			final AnnotationBo apiParameterAnnotation = apiAnnotations.get(0);
			assertThat(apiParameterAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
		}
		assertThat(uniqueApiIds.size(), is(numOperations));
	}
	
}
