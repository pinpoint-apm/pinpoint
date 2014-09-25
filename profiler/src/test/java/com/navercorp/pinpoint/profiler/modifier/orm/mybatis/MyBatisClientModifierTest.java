package com.nhn.pinpoint.profiler.modifier.orm.mybatis;

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

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.profiler.junit4.BasePinpointTest;

/**
 * @author Hyun Jeong
 */
public abstract class MyBatisClientModifierTest extends BasePinpointTest {

	public static final int NOT_CACHED = 0;
	
	protected abstract SqlSession getSqlSession();
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public void cleanUp() throws Exception {
		getSqlSession().close();
	}

	@Test
	public void nullParameterShouldNotBeTraced() throws Exception {
		// When
		getSqlSession().insert(null);
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));
		
		// Check Method
		final SpanEventBo insertSpanEventBo = spanEvents.get(0);
		assertThat(insertSpanEventBo.getApiId(), not(NOT_CACHED));
		
		// Check Parameter
		assertNull(insertSpanEventBo.getAnnotationBoList());
	}

	@Test
	public void selectOneShouldBeTraced() throws Exception {
		// When
		getSqlSession().selectOne("selectOne");
		getSqlSession().selectOne("selectOne", null);
		// Then
		assertNOperations(2);
	}

	@Test
	public void selectListShouldBeTraced() throws Exception {
		// When
		getSqlSession().selectList("selectList");
		getSqlSession().selectList("selectList", null);
		getSqlSession().selectList("selectList", null, null);
		// Then
		assertNOperations(3);
	}
	
	@Test
	public void selectMapShouldBeTraced() throws Exception {
		// Given
		// When
		getSqlSession().selectMap("selectMap", null);
		getSqlSession().selectMap("selectMap", null, null);
		getSqlSession().selectMap("selectMap", null, null, null);
		// Then
		assertNOperations(3);
	}

	@Test
	public void selectShouldBeTraced() throws Exception {
		// When
		getSqlSession().select("select", null);
		getSqlSession().select("select", null, null);
		getSqlSession().select("select", null, null, null);
		// Then
		assertNOperations(3);
	}
	
	@Test
	public void insertShouldBeTraced() throws Exception {
		// When
		getSqlSession().insert("insert");
		getSqlSession().insert("insert", new Object());
		// Then
		assertNOperations(2);
	}
	
	@Test
	public void updateShouldBeTraced() throws Exception {
		// When
		getSqlSession().update("update");
		getSqlSession().update("update", new Object());
		// Then
		assertNOperations(2);
	}
	
	@Test
	public void deleteShouldBeTraced() throws Exception {
		// When
		getSqlSession().delete("delete");
		getSqlSession().delete("delete", new Object());
		// Then
		assertNOperations(2);
	}

	@Ignore // 쿼리 작업만 tracing 하도록 변경
	@Test
	public void commitShouldBeTraced() throws Exception {
		// When
		getSqlSession().commit();
		getSqlSession().commit(true);
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));
		
		// Check Method
		final SpanEventBo commitWith0ArgSpanEvent = spanEvents.get(0);
		final SpanEventBo commitWith1ArgSpanEvent = spanEvents.get(1);
		assertThat(commitWith0ArgSpanEvent.getApiId(), not(NOT_CACHED));
		assertThat(commitWith1ArgSpanEvent.getApiId(), not(NOT_CACHED));
		assertThat(commitWith0ArgSpanEvent.getApiId(), not(commitWith1ArgSpanEvent.getApiId()));
		
		// Check Parameter
		assertNull(commitWith0ArgSpanEvent.getAnnotationBoList());
		assertThat(commitWith1ArgSpanEvent.getAnnotationBoList().get(0).getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
	}

	@Ignore // 쿼리 작업만 tracing 하도록 변경
	@Test
	public void rollbackShouldBeTraced() throws Exception {
		// When
		getSqlSession().rollback();
		getSqlSession().rollback(true);
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(2));
		
		// Check Method
		final SpanEventBo rollbackWith0ArgSpanEvent = spanEvents.get(0);
		final SpanEventBo rollbackWith1ArgSpanEvent = spanEvents.get(1);
		assertThat(rollbackWith0ArgSpanEvent.getApiId(), not(NOT_CACHED));
		assertThat(rollbackWith1ArgSpanEvent.getApiId(), not(NOT_CACHED));
		assertThat(rollbackWith0ArgSpanEvent.getApiId(), not(rollbackWith1ArgSpanEvent.getApiId()));
		
		// Check Parameter
		assertNull(rollbackWith0ArgSpanEvent.getAnnotationBoList());
		assertThat(rollbackWith1ArgSpanEvent.getAnnotationBoList().get(0).getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
	}

	@Ignore // 쿼리 작업만 tracing 하도록 변경
	@Test
	public void flushStatementsShouldBeTraced() throws Exception {
		// When
		getSqlSession().flushStatements();
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));
		
		// Check Method
		final SpanEventBo flushStatementsSpanEvent = spanEvents.get(0);
		assertThat(flushStatementsSpanEvent.getApiId(), not(NOT_CACHED));

		// Check Parameter
		assertNull(flushStatementsSpanEvent.getAnnotationBoList());
	}

	@Ignore // 쿼리 작업만 tracing 하도록 변경
	@Test
	public void closeShouldBeTraced() throws Exception {
		// When
		getSqlSession().close();
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));
		
		// Check Method
		final SpanEventBo closeSpanEvent = spanEvents.get(0);
		assertThat(closeSpanEvent.getApiId(), not(NOT_CACHED));
		
		// Check Parameter
		assertNull(closeSpanEvent.getAnnotationBoList());
	}

	@Ignore // 쿼리 작업만 tracing 하도록 변경
	@Test
	public void getConfigurationShouldBeTraced() throws Exception {
		// When
		getSqlSession().getConfiguration();
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));
		
		// Check Method
		final SpanEventBo getConfigurationSpanEvent = spanEvents.get(0);
		assertThat(getConfigurationSpanEvent.getApiId(), not(NOT_CACHED));
		
		// Check Parameter
		assertNull(getConfigurationSpanEvent.getAnnotationBoList());
	}

	@Ignore // 쿼리 작업만 tracing 하도록 변경
	@Test
	public void getMapperShouldBeTraced() throws Exception {
		// Given
		class SomeBean {}
		// When
		getSqlSession().getMapper(SomeBean.class);
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));
		
		// Check Method
		final SpanEventBo getConnectionSpanEvent = spanEvents.get(0);
		assertThat(getConnectionSpanEvent.getApiId(), not(NOT_CACHED));
		
		// Check Parameter
		assertThat(getConnectionSpanEvent.getAnnotationBoList().get(0).getKey(), is(AnnotationKey.CACHE_ARGS0.getCode()));
	}

	@Ignore // 쿼리 작업만 tracing 하도록 변경
	@Test
	public void getConnectionShouldBeTraced() throws Exception {
		// When
		getSqlSession().getConnection();
		// Then
		final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
		assertThat(spanEvents.size(), is(1));
		
		// Check Method
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
			// Check Method
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
