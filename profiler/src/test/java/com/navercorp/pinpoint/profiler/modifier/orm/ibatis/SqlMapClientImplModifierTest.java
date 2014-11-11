package com.nhn.pinpoint.profiler.modifier.orm.ibatis;

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
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.profiler.junit4.BasePinpointTest;

/**
 * @author Hyun Jeong
 */
public class SqlMapClientImplModifierTest extends BasePinpointTest {

	public class MockSqlMapExecutorDelegate extends SqlMapExecutorDelegate {
		@Override
		public SessionScope beginSessionScope() {
			return mockSessionScope;
		}
	}

	@Mock
	private MockSqlMapExecutorDelegate mockSqlMapExecutorDelegate;
	@Mock
	private SessionScope mockSessionScope;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(this.mockSqlMapExecutorDelegate.beginSessionScope()).thenReturn(this.mockSessionScope);
	}
	
	@Test
	public void exceptionsThrownShouldBeTraced() throws Exception {
		// Given
		when(this.mockSqlMapExecutorDelegate.beginSessionScope()).thenReturn(null);
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		try {
			sqlMapClient.insert("insertShouldThrowNPE");
			fail("sqlMapClient.insert should throw NullPointerException");
		} catch (NullPointerException e) {
			// Then
			final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
			assertThat(spanEvents.size(), is(1));
			final SpanEventBo exceptionSpanEventBo = spanEvents.get(0);
			assertThat(exceptionSpanEventBo.hasException(), is(true));
			assertThat(exceptionSpanEventBo.getExceptionId(), not(0));
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void test() throws SQLException {
        // Given
        when(this.mockSqlMapExecutorDelegate.beginSessionScope()).thenReturn(null);
        SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
        // When
        sqlMapClient.insert("insertShouldThrowNPE");
	}
	
	@Test
	public void nullParametersShouldNotBeTraced() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.insert(null);
		sqlMapClient.queryForList(null);
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));
		
		// Check Method
		final SpanEventBo insertSpanEventBo = spanEvents.get(0);
		final SpanEventBo queryForListSpanEventBo = spanEvents.get(1);
		assertThat(insertSpanEventBo.getApiId(), not(0));
		assertThat(queryForListSpanEventBo.getApiId(), not(0));
		assertThat(insertSpanEventBo.getApiId(), not(queryForListSpanEventBo.getApiId()));
		
		// Check Parameter
		assertNull(insertSpanEventBo.getAnnotationBoList());
		assertNull(queryForListSpanEventBo.getAnnotationBoList());
	}
	
	@Test
	public void sameApiCallsShouldHaveTheSameApiId() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.insert("insertA");
		sqlMapClient.insert("insertB");
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));
		
		// Check Method
		final SpanEventBo insertASpanEventBo = spanEvents.get(0);
		final SpanEventBo insertBSpanEventBo = spanEvents.get(1);
		assertThat(insertASpanEventBo.getApiId(), not(0));
		assertThat(insertBSpanEventBo.getApiId(), not(0));
		assertThat(insertASpanEventBo.getApiId(), is(insertBSpanEventBo.getApiId()));
		
	}
	
	@Test
	public void insertShouldBeTraced() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.insert("insertId");
		sqlMapClient.insert("insertId", new Object());
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));

		// Check Method
		final SpanEventBo insertWith1ArgSpanEventBo = spanEvents.get(0);
		final SpanEventBo insertWith2ArgSpanEventBo = spanEvents.get(1);
		assertThat(insertWith1ArgSpanEventBo.getApiId(), not(0));
		assertThat(insertWith2ArgSpanEventBo.getApiId(), not(0));
		assertThat(insertWith1ArgSpanEventBo.getApiId(), not(insertWith2ArgSpanEventBo.getApiId()));

		// Check Parameter
		final List<AnnotationBo> insertWith1ArgAnnotations = insertWith1ArgSpanEventBo.getAnnotationBoList();
		assertThat(insertWith1ArgAnnotations.size(), is(1));
		final AnnotationBo insertWith1ArgParameterAnnotation = insertWith1ArgAnnotations.get(0);
		assertThat(insertWith1ArgParameterAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
		
		final List<AnnotationBo> insertWith2ArgAnnotations = insertWith2ArgSpanEventBo.getAnnotationBoList();
		assertThat(insertWith2ArgAnnotations.size(), is(1));
		final AnnotationBo insertWith2ArgAnnotation = insertWith2ArgAnnotations.get(0);
		assertThat(insertWith2ArgAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
		
	}
	
	@Test
	public void deleteShouldBeTraced() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.delete("deleteId");
		sqlMapClient.delete("deleteId", new Object());
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));

		// Check Method
		final SpanEventBo deleteWith1ArgSpanEvent = spanEvents.get(0);
		final SpanEventBo deleteWith2ArgSpanEvent = spanEvents.get(1);
		assertThat(deleteWith1ArgSpanEvent.getApiId(), not(0));
		assertThat(deleteWith2ArgSpanEvent.getApiId(), not(0));
		assertThat(deleteWith1ArgSpanEvent.getApiId(), not(deleteWith2ArgSpanEvent.getApiId()));

		// Check Parameter
		final List<AnnotationBo> deleteWith1ArgAnnotations = deleteWith1ArgSpanEvent.getAnnotationBoList();
		assertThat(deleteWith1ArgAnnotations.size(), is(1));
		final AnnotationBo deleteWith1ArgParameterAnnotation = deleteWith1ArgAnnotations.get(0);
		assertThat(deleteWith1ArgParameterAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
		
		final List<AnnotationBo> deleteWith2ArgAnnotations = deleteWith2ArgSpanEvent.getAnnotationBoList();
		assertThat(deleteWith2ArgAnnotations.size(), is(1));
		final AnnotationBo deleteWith2ArgAnnotation = deleteWith2ArgAnnotations.get(0);
		assertThat(deleteWith2ArgAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
	}
	
	@Test
	public void updateShouldBeTraced() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.update("updateId");
		sqlMapClient.update("updateId", new Object());
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));

		// Check Method
		final SpanEventBo updateWith1ArgSpanEvent = spanEvents.get(0);
		final SpanEventBo updateWith2ArgSpanEvent = spanEvents.get(1);
		assertThat(updateWith1ArgSpanEvent.getApiId(), not(0));
		assertThat(updateWith2ArgSpanEvent.getApiId(), not(0));
		assertThat(updateWith1ArgSpanEvent.getApiId(), not(updateWith2ArgSpanEvent.getApiId()));

		// Check Parameter
		final List<AnnotationBo> updateWith1ArgAnnotations = updateWith1ArgSpanEvent.getAnnotationBoList();
		assertThat(updateWith1ArgAnnotations.size(), is(1));
		final AnnotationBo updateWith1ArgParameterAnnotation = updateWith1ArgAnnotations.get(0);
		assertThat(updateWith1ArgParameterAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
		
		final List<AnnotationBo> updateWith2ArgAnnotations = updateWith2ArgSpanEvent.getAnnotationBoList();
		assertThat(updateWith2ArgAnnotations.size(), is(1));
		final AnnotationBo updateWith2ArgAnnotation = updateWith2ArgAnnotations.get(0);
		assertThat(updateWith2ArgAnnotation.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
		
	}

	@Test
	public void queryForListShouldBeTraced() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.queryForList("abc");
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));

		// Check Method
		final SpanEventBo apiCallSpanEventBo = spanEvents.get(0);
		assertThat(apiCallSpanEventBo.getApiId(), not(0));

		// Check Parameter
		final List<AnnotationBo> annotationBoList = apiCallSpanEventBo.getAnnotationBoList();
		assertThat(annotationBoList.size(), is(1));

		final AnnotationBo parameterAnnotationBo = annotationBoList.get(0);
		assertThat(parameterAnnotationBo.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
	}

	@Test
	public void queryForObjectShouldBeTraced() throws Exception {
		// Given
		SqlMapClient sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
		// When
		sqlMapClient.queryForObject("abrgrgfdaghertah", new Object());
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));

		// Check Method
		final SpanEventBo apiCallSpanEventBo = spanEvents.get(0);
		assertThat(apiCallSpanEventBo.getApiId(), not(0));

		// Check Parameter
		final List<AnnotationBo> annotationBoList = apiCallSpanEventBo.getAnnotationBoList();
		assertThat(annotationBoList.size(), is(1));

		final AnnotationBo parameterAnnotationBo = annotationBoList.get(0);
		assertThat(parameterAnnotationBo.getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
	}
	
	@Ignore // 쿼리 작업만 tracing 하도록 변경
	@Test
	public void transactionsShouldBeTraced() throws Exception {
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
