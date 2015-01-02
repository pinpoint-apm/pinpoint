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

import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.scope.SessionScope;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;

/**
 * @author Hyun Jeong
 */
public class SqlMapClientImplModifierTest extends BasePinpointTest {

    public class MockSqlMapExecutorDelegate extends SqlMapExecutorDelegate       {
		@       verride
		public SessionScope beginSe          sionScope() {
			r             tu    n mockSessionScope;
		}
	}

	@Mock
	private MockSqlMapExecut    rDe    egate mockSqlMapExecutorDelegate;
	@M    ck
	p    ivate SessionScope mockSessionScope;
	@Before
	public void setUp()        hrows Exception {
		MockitoAnnotations.initMocks(this);
		when(this.mockSqlMapExecutorDe          eg    te.beginSessionScope()).thenReturn(this.mockSessionScope);
	}
	@       est
	public void exceptionsThrownShouldBeTraced() throws Exception {
		       / Given
		when(this.mockSqlMapExecutorDelegate.beginSessionScope()).thenReturn       nul       )
		SqlMapClient sqlMapClient = new Sql          apClientImpl(this.mockSqlMapExecutorDelegate);
		// When       		try {
			sqlMapClient.insert          "          nsertShouldThrowNPE");
			fail("sqlMapClient.insert sh          uld throw NullPointerException"          ;
		} catch (NullPointerException e) {
			// Then
			          inal List<SpanEventBo> spanEvents = getCurrentSpanEv          nts();
			assertThat(spanEvents.size(), is(1));
			f                nal SpanEventBo exceptionSpanEventBo      spanEvents.get(0);
			assertThat(exceptionSpanEventBo.hasException(), is(true));
			assertThat(exceptionSpanEventBo.getExceptionId(), not(0));
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void test() throws SQLException {
        // Given
        when(this.mockSqlMapExecutorDelegat          .b    ginSessionScope()).thenReturn(null);
        SqlMapClient sqlM       pCli       nt = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
        // When
                    sqlMapClient.insert("       nsertShouldThrowNPE");
	}
	
       @Te       t
	public void nullParametersShouldNotBeTraced() throws        xception {
		// Given
		SqlMapCli             nt sqlMa       Client = new SqlMapClientImpl(this.mockSqlMapExecuto       Delegate);
		// When
		sqlMapClient.insert(null);
		sqlMap       lient.queryForList(null);
		// Then
		final L       st<SpanEventBo> spanEvents = getCurrentSpanEvents()
		assertThat(spanEvents.size(), is(2));
		
		// Check Method
		final SpanEven             Bo insertSp       nEventBo = spanEvents.get(0);
		final SpanEventB        queryForListSpanEventBo = spanEvents.get(1);
		assert          ha    (insertSpanEventBo.getApiId(), not(0));
		assertThat(queryForList       panE       entBo.getApiId(), not(0));
		assertThat(insertSpanEventBo.getApiId(), not(quer       For       istSpanEventBo.getApiId()))
		
		// Check Parameter
		       sse       tNull(insertSpanEventBo.getAnnotationBoList());
		assert       ull(queryForListSpanEventBo.getAn             otationB       List());
	}
	
	@Test
	public void sameApiCallsShouldH       veTheSameApiId() throws Exception {
		// Given
		SqlM       pClient sqlMapClient = new SqlMapClientImpl(th       s.mockSqlMapExecutorDelegate);
		// When
		sql       apClient.insert("insertA");
		sqlMapClient.insert("insertB");
		// Then
	                   final List<SpanEventBo> spanEvents = getCurrentSpan       vent       ();
		assertThat(spanEvents.size(), is(2));
		
		// Check Method
		final SpanE       ent       o insertASpanEventBo = spanE       ents.get(0);
		final SpanEventBo insertBSp       nEv       ntBo = spanEvents.get(1);
		assertThat(insertASpanEventB       .getApiId(), not(0));
		assertThat       insertBSpan       ventBo.getApiId(), not(0));
		assertThat(insertASpanEventBo.       etApiId(), is(insertBSpanEventBo.getApiId()));
		
	}
	
	@Tes
	public void insertShouldBeTraced() throws Exception       {
		// Given
		SqlMapClient sqlMapClient = new SqlMap       lientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.insert("insertId");
       	sqlMapClient.       nsert("insertId", new Object());
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanE       ents();
		assertThat(spanEvents.size(), is(2));
       		// Check Method
		final SpanEventBo insertWith1ArgSpanEventBo = spanEvents.get(0);       		final SpanEventBo insertWith2ArgSpanEventBo = spanEvents.get(1);
		assertThat(insertWith1A             gSpanEventBo.getApiId(), not(0));
		assertThat(insertWith2ArgSpanEventBo.getApiId(), not(0));
       	assertThat(insertWith1ArgSpanEventBo.getApiId()        not(insertWith2ArgSpanEventBo.getApiId()));

		// Check Parameter
		final        ist<AnnotationBo> insertWith1ArgAnnotations = insertWith1ArgSpanEventBo.getAnnotati                   nBoList();
		assertThat(insertWith1ArgAnnotations.s       ze()        is(1));
		final AnnotationBo insertWith1ArgParameterAnnotation = insertWith1A       gAn       otations.get(0);
		assertTha       (insertWith1ArgParameterAnnotation.getKey(       , i       (AnnotationKey.CACHE_ARGS0.getCode()));
		
		final List<       nnotationBo> insertWith2ArgAnnotat       ons = inser       With2ArgSpanEventBo.getAnnotationBoList();
		assertThat(in       ertWith2ArgAnnotations.size(), is(1));
		final AnnotationB        insertWith2ArgAnnotation = insertWith2ArgAnnotatio       s.get(0);
		assertThat(insertWith2ArgAnnotation.get       ey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
		
	}
	
	@Test
	public void deleteSho       ldBeTraced() t       rows Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapE       ecutorDelegate);
		// When
		sqlMapClient.delete       "deleteId");
		sqlMapClient.delete("deleteId", new Object());
		// Then
		final List       SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));

	             // Check Method
		final SpanEventBo deleteWith1ArgSpanEvent = spanEvents.get(0);
		final Spa       EventBo deleteWith2ArgSpanEvent = spanEvents.get       1);
		assertThat(deleteWith1ArgSpanEvent.getApiId(), not(0));
		assertThat(       eleteWith2ArgSpanEvent.getApiId(), not(0));
		assertThat(deleteWith1ArgSpanEvent.ge          Ap    Id(), not(deleteWith2ArgSpanEvent.getApiId()));

		       / Ch       ck Parameter
		final List<AnnotationBo> deleteWith1ArgAnnotations = deleteWith       Arg       panEvent.getAnnotationBoList       );
		assertThat(deleteWith1ArgAnnotations.       ize       ), is(1));
		final AnnotationBo deleteWith1ArgParameterA       notation = deleteWith1ArgAnnotatio       s.get(0);
	       assertThat(deleteWith1ArgParameterAnnotation.getKey(), is(       nnotationKey.CACHE_ARGS0.getCode()));
		
		final List<Anno       ationBo> deleteWith2ArgAnnotations = deleteWith2Arg       panEvent.getAnnotationBoList();
		assertThat(delete       ith2ArgAnnotations.size(), is(1));
		final AnnotationBo deleteWith2ArgAnnotation = de       eteWith2ArgAnn       tations.get(0);
		assertThat(deleteWith2ArgAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.ge       Code()));
	}
	
	@Test
	public void updateShouldB       Traced() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClien       Impl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.update("updateId");
		sqlMap             lient.update("updateId", new Object());
		// Then
		final List<SpanEventBo> spanEvents = get       urrentSpanEvents();
		assertThat(spanEvents.size       ), is(2));

		// Check Method
		final SpanEventBo updateWith1ArgSpanEvent =       spanEvents.get(0);
		final SpanEventBo updateWith2ArgSpanEvent = spanEvents.get(1);             	    assertThat(updateWith1ArgSpanEvent.getApiId(), not(0));
	       asse       tThat(updateWith2ArgSpanEvent.getApiId(), not(0));
		assertThat(updateWith1Arg       pan       vent.getApiId(), not(updateWi       h2A       gSpanEvent.getApiId()));

		// Check Parameter
		final L       st<AnnotationBo> updateWith1ArgAnn       tations = u       dateWith1ArgSpanEvent.getAnnotationBoList();
		assert       hat(updateWith1ArgAnnotations.size(), is(1));
	       final Annotati       nBo updateWith1ArgParameterAnnotation = updateWith1ArgAnnotations.get(0);
		asser       That(updateWith1ArgParameterAnnotation.g       tKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
		
		final Li       t<AnnotationBo> updateWith2ArgAnnotations = updateWith2ArgSpanEvent.getAnnotatio        oLi    t();
		assertThat(updateWith2ArgAnnotations.size(), is(1));       		fi       al AnnotationBo updateWith2ArgAnnotation = updateWith2ArgAnnotations.get(0);
	       ass       rtThat(updateWith2ArgAnnotation.getKey(), is(AnnotationKey       CAC       E_ARGS0.getCode()));
		
	}

	@Test
	public void queryFor       istShouldBeTraced() throws Excepti       n {
		// Gi       en
		SqlMapClient sqlMapClient = new SqlMapClientImpl       this.mockSqlMapExecutorDelegate);
		// When
		s       lMapClient.que       yForList("abc");
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanE       ents();
		assertThat(spanEvents.size(),        s(1));

		// Check Method
		final SpanEventBo apiCallSpanEventB        = spanEvents.get(0);
		assertThat(apiCallSpanEventBo.getApiId(), not(0));

		//          Check Parameter
		final List<AnnotationBo> ann    tat    onBoList = apiCallSpanEventBo.getAnnotationBoList();
		as       ertT       at(annotationBoList.size(), is(1));

		final AnnotationBo parameterAnnotationB        =        nnotationBoList.get(0);
		as       ertThat(parameterAnnotationBo       getKey(), is(AnnotationKey       CAC       E_ARGS0.getCode()));
	}

	@Test
	public void queryForObj       ctShouldBeTraced() throws Excepti             n {
		//       Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.       ockSqlMapExecutorDelegate);
		// When
		sqlMapClient.queryForOb       ect("abrgrgfdaghertah", new Object());
		// Then
		final Lis             <SpanEventBo> spanEvents = getCurrentSpanEvents();
	       assertThat(spanEvents.size(), is(1));

		// Check Method       		final SpanEventBo apiCallSpanEventBo = spanEvents.g             t(0);
		assertThat(apiCallSpanEventBo.getApiId(), not(0));

		// Check Parameter
		final L       st<AnnotationBo> annotationBoList = apiCallSpanEventBo.getAnnotationBoList();
		assertThat(       nnotationBoList.size(), is(1));

		final AnnotationBo parameterAnnotationBo = annotationBo             ist.get(0);       		assertThat(parameterAnnotationBo.getKey(), is(Annotation       ey.CACHE_ARGS0.getCode()));
	}
	
	@Ignore // Changed to tra       e only query operations
	@Test
	public void transactions    houldBeTraced() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.startTransaction();
		sqlMapClient.commitTransaction();
		sqlMapClient.endTransaction();
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(3));
		
		// Check Method
		final SpanEventBo startTransactionSpanEventBo = spanEvents.get(0);
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

}
