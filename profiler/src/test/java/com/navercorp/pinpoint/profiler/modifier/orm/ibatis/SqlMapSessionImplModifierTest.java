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

package com.navercorp.pinpoint.profiler.modifier.orm.ibatis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl;
import com.ibatis.sqlmap.engine.scope.SessionScope;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;

/**
 * @author Hyun Jeong
 */
public class SqlMapSessionImplModifierTest extends BasePinpointTest {

    public class MockSqlMapExecutorDelegate extends SqlMapExecutorDelegate       {
		@       verride
		public SessionScope beginSe          sionScope() {
			r                turn mockSessionScope;
		}
	}
	
	p    iva    e SqlMapClientImpl sqlMapClient;

	@Mock
	private MockSqlMap    xec    torDelegate mockSqlMapExecutorDelega       e;
	    Mock
	private SessionScope mockSessi       nScope;
	
	@Before
	public void       setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(this.mockSqlMapE       ecutorDelegate.beginSessionScope()).thenReturn(this.mockSessionScope);          		t    is.sqlMapClient = new SqlMapClientImpl       this.mockSqlMapExecut          rD    legate);
	}
	
	@After
	public void cleanUp() throws Exception       {
		       his.sqlMapClient = null;
	}
	
	@Test
	public void ex       eptionsThrownShouldBeTraced() throws Exception {
		// Given
		final String exceptionInsertId = "insertShouldThrowNPE";
		w       en(this.mockSqlMapExecutorDelegate.insert(mockSessionScope, excepti       nIn       e          tId, null)).thenThrow(new NullPoin          erException());
		SqlMapSession sqlMapSession = new SqlMapS       ssionImpl(this.sqlMapClient);
          	          / When
		try {
			sqlMapSession.insert(exceptionInsert          d);
			fail("sqlMapSession.inse          t() should throw NullPointerException");
		} catch (N          llPointerException e) {
			// Then
			final List<Spa          EventBo> spanEvents = getCurrentSpanEvents();
			ass                    tThat(spanEvents.size(), is(1));
			final SpanEventBo exceptio       Span       ventBo = spanEvents.get(0);
			assertThat(exceptionSpanEventBo.hasE       cep       ion(), is(true));
			as       ertThat(exceptionSpanEventBo.       etE       ceptionId(), not(0));
		}
	}
	
	@Test
	public void nullP       rametersShouldNotBeTraced() throw              Excepti       n {
		// Given
		SqlMapSession sqlMapSession = new S       lMapSessionImpl(this.sqlMapClient);
		// When
		sqlMapSess       on.insert(null);
		sqlMapSession.queryForList       null);
		// Then
		final List<SpanEventBo> spanEven       s = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));
		
		// Ch             ck Method
	       final SpanEventBo insertSpanEventBo = spanEvents       get(0);
		final SpanEventBo queryForListSpanEventBo =           pa    Events.get(1);
		assertThat(insertSpanEventBo.getApiId(), not(0))
		a       sertThat(queryForListSpanEventBo.getApiId(), not(0));
		assertThat(       nse       tSpanEventBo.getApiId(), not       queryForListSpanEventBo.getA       iId       )));
		
		// Check Parameter
		assertNull(insertSpanEven       Bo.getAnnotationBoList());
		asse             tNull(qu       ryForListSpanEventBo.getAnnotationBoList());
	}
	
	@T       st
	public void sameApiCallsShouldHaveTheSameApiId()        hrows Exception {
		// Given
		SqlMapSession s       lMapSession = new SqlMapSessionImpl(this.sqlMa       Client);
		// When
		sqlMapSession.insert("insertA");
		sqlMapSession.ins                   rt("insertB");
		// Then
		final List<SpanEventBo>        panE       ents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2       );

		// Check Method
		final S       anEventBo insertASpanEventBo = spanEvents.g       t(0       ;
		final SpanEventBo insertBSpanEventBo = spanEvents.ge       (1);
		assertThat(insertASpanEvent       o.getApiId(       , not(0));
		assertThat(insertBSpanEventBo.getApiId(), not(0       );
		assertThat(insertASpanEventBo.getApiId(), is(insertBSpa       EventBo.getApiId()));
		
	}
	
	@Test
	public void ins       rtShouldBeTraced() throws Exception {
		// Given
		Sq       MapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
		// When
		sqlMapSe       sion.insert("i       sertId");
		sqlMapSession.insert("insertId", new Object());
		// Then
		final List<SpanEventBo> s       anEvents = getCurrentSpanEvents();
		assertThat(       panEvents.size(), is(2));

		// Check Method
		final SpanEventBo insertWith1ArgSpanE       entBo = spanEvents.get(0);
		final SpanEventBo insertWith2ArgSpanEventBo = spanEvents.get(1)
		assertThat(insertWith1ArgSpanEventBo.getApiId(), not(0));
		assertThat(insertWith2ArgSpanEv       ntBo.getApiId(), not(0));
		assertThat(insertWit       1ArgSpanEventBo.getApiId(), not(insertWith2ArgSpanEventBo.getApiId()));

		       / Check Parameter
		final List<AnnotationBo> insertWith1ArgAnnotations = insertWith                   ArgSpanEventBo.getAnnotationBoList();
		assertThat(       nser       With1ArgAnnotations.size(), is(1));
		final AnnotationBo insertWith       Arg       arameterAnnotation = insertWi       h1ArgAnnotations.get(0);
		assertThat(inser       Wit       1ArgParameterAnnotation.getKey(), is(AnnotationKey.CACHE       ARGS0.getCode()));
		
		final List       AnnotationB       > insertWith2ArgAnnotations = insertWith2ArgSpanEventBo.ge       AnnotationBoList();
		assertThat(insertWith2ArgAnnotations       size(), is(1));
		final AnnotationBo insertWith2Arg       nnotation = insertWith2ArgAnnotations.get(0);
		ass       rtThat(insertWith2ArgAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));

	}
	
	@Test
	       ublic void deleteShouldBeTraced() throws Exception {
		// Given
		SqlMapSession sqlMapSession =       new SqlMapSessionImpl(this.sqlMapClient);
		// W       en
		sqlMapSession.delete("deleteId");
		sqlMapSession.delete("deleteId", new Object       ));
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(sp             nEvents.size(), is(2));

		// Check Method
		final SpanEventBo deleteWith1ArgSpanEvent = spa       Events.get(0);
		final SpanEventBo deleteWith2Ar       SpanEvent = spanEvents.get(1);
		assertThat(deleteWith1ArgSpanEvent.getApiI       (), not(0));
		assertThat(deleteWith2ArgSpanEvent.getApiId(), not(0));
		assertThat          de    eteWith1ArgSpanEvent.getApiId(), not(deleteWith2Arg       panE       ent.getApiId()));

		// Check Parameter
		final List<AnnotationBo>        ele       eWith1ArgAnnotations = delete       ith1ArgSpanEvent.getAnnotationBoList();
		a       ser       That(deleteWith1ArgAnnotations.size(), is(1));
		final A       notationBo deleteWith1ArgParameter       nnotation =       deleteWith1ArgAnnotations.get(0);
		assertThat(deleteWith1       rgParameterAnnotation.getKey(), is(AnnotationKey.CACHE_ARG       0.getCode()));
		
		final List<AnnotationBo> delete       ith2ArgAnnotations = deleteWith2ArgSpanEvent.getAnn       tationBoList();
		assertThat(deleteWith2ArgAnnotations.size(), is(1));
		final Annota       ionBo deleteWi       h2ArgAnnotation = deleteWith2ArgAnnotations.get(0);
		assertThat(deleteWith2ArgAnnotation.getKe       (), is(AnnotationKey.CACHE_ARGS0.getCode()));
	}
	@Test
	public void updateShouldBeTraced() throws Exception {
		// Given
		SqlMapS       ssion sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
		// When
		sqlMapSession.up             ate("updateId");
		sqlMapSession.update("updateId", new Object());
		// Then
		final List<Sp       nEventBo> spanEvents = getCurrentSpanEvents();
	       assertThat(spanEvents.size(), is(2));

		// Check Method
		final SpanEventB        updateWith1ArgSpanEvent = spanEvents.get(0);
		final SpanEventBo updateWith2ArgSpa             E    ent = spanEvents.get(1);
		assertThat(updateWith1ArgSpanE       ent.       etApiId(), not(0));
		assertThat(updateWith2ArgSpanEvent.getApiId()        no       (0));
		assertThat(updateWith1       rgS       anEvent.getApiId(), not(updateWith2ArgSpanEvent.getApiId       )));

		// Check Parameter
		final       List<Annota       ionBo> updateWith1ArgAnnotations = updateWith1ArgSpan       vent.getAnnotationBoList();
		assertThat(update       ith1ArgAnnotat       ons.size(), is(1));
		final AnnotationBo updateWith1ArgParameterAnnotation = upda       eWith1ArgAnnotations.get(0);
		assertTha       (updateWith1ArgParameterAnnotation.getKey(), is(AnnotationKey.C       CHE_ARGS0.getCode()));
		
		final List<AnnotationBo> updateWith2ArgAnnotations =        pda    eWith2ArgSpanEvent.getAnnotationBoList();
		assertThat(upda       eWit       2ArgAnnotations.size(), is(1));
		final AnnotationBo updateWith2Arg       nno       ation = updateWith2ArgAnnotations.get(0);
		assertThat(upda       eWi       h2ArgAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.g       tCode()));
		
	}

	@Test
	public v       id queryFor       istShouldBeTraced() throws Exception {
		// Given
		S       lMapSession sqlMapSession = new SqlMapSessionIm       l(this.sqlMapC       ient);
		// When
		sqlMapSession.queryForList("abc");
		// Then
		final List<Span       ventBo> spanEvents = getCurrentSpanEvent       ();
		assertThat(spanEvents.size(), is(1));

		// Check Method
       	final SpanEventBo apiCallSpanEventBo = spanEvents.get(0);
		assertThat(apiCallS        nEventBo.getApiId(), not(0));

		// Check Param    ter    		final List<AnnotationBo> annotationBoList = apiCallSpan       vent       o.getAnnotationBoList();
		assertThat(annotationBoList.size(), is(1       );
       		final AnnotationBo paramete       AnnotationBo = annotationBoLis       .get(0);
		assertThat(param       ter       nnotationBo.getKey(), is(AnnotationKey.CACHE_ARGS0.getCo       e()));
	}

	@Test
	public void qu             ryForObj       ctShouldBeTraced() throws Exception {
		// Given
		SqlMapSessi       n sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
		/        When
		sqlMapSession.queryForObject("abrgrgfdaghertah", new             Object());
		// Then
		final List<SpanEventBo> spanE       ents = getCurrentSpanEvents();
		assertThat(spanEvents.s       ze(), is(1));

		// Check Method
		final SpanEventBo              piCallSpanEventBo = spanEvents.get(0);
		assertThat(apiCallSpanEventBo.getApiId(), not(0))

		// Check Parameter
		final List<AnnotationBo> annotationBoList = apiCallSpanEventBo.get       nnotationBoList();
		assertThat(annotationBoList.size(), is(1));

		final AnnotationBo par             meterAnnota       ionBo = annotationBoList.get(0);
		assertThat(parameterAnn       tationBo.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()))
	}

	@Ignore // Changed to trace only query operations
        Test
	public void transactionsShouldBeTraced()     hro    s Exception {
		// Given
		SqlMapSession sqlMapSes       ion         new SqlMapSessionImpl(this.sqlMapClient);
		// When
		sqlMapSessio       .st       rtTransaction();
	       sql       apSession.commitTransaction();
		sqlMapSession.endTransa       tion();
		// Then
		final List<Sp             nEventBo        spanEvents = getCurrentSpanEvents();
		assertThat(       panEvents.size(), is(3));
		
		// Check Meth             d
		final S       anEventBo startTransactionSpanEventBo = spanEve    ts.get(0);
		final SpanEventBo commitTransactionSpanEventBo = spanEvents.get(1);
		final SpanEventBo endTransactionSpanEventBo = spanEvents.get(2);
		
		assertThat(startTransactionSpanEventBo.getApiId(), not(0));
		assertThat(commitTransactionSpanEventBo.getApiId(), not(0));
		assertThat(endTransactionSpanEventBo.getApiId(), not(0));
		
		assertThat(startTransactionSpanEventBo.getApiId(), not(commitTransactionSpanEventBo.getApiId()));
		assertThat(commitTransactionSpanEventBo.getApiId(), not(endTransactionSpanEventBo.getApiId()));
		assertThat(endTransactionSpanEventBo.getApiId(), not(startTransactionSpanEventBo.getApiId()));
		
		// Check Parameter
		assertNull(startTransactionSpanEventBo.getAnnotationBoList());
		assertNull(commitTransactionSpanEventBo.getAnnotationBoList());
		assertNull(endTransactionSpanEventBo.getAnnotationBoList());
	}

	@Ignore // Changed to trace only query operations
	@Test
	public void closeShouldBeTraced() throws Exception {
		// Given
		SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
		// When
		sqlMapSession.close();
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));
		
		// Check Method
		final SpanEventBo closeSpanEventBo = spanEvents.get(0);
		assertThat(closeSpanEventBo.getApiId(), not(0));
		
		// Check Parameter
		assertNull(closeSpanEventBo.getAnnotationBoList());
	}
}
