package com.nhn.hippo.web.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.calltree.span.SpanAligner;
import com.nhn.hippo.web.dao.TraceDao;
import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.common.mapping.ApiUtils;
import com.profiler.common.mapping.MethodMapping;

/**
 *
 */
@Service
public class SpanServiceImpl implements SpanService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TraceDao traceDao;

	@Override
	public List<SpanAlign> selectSpan(String uuid) {
		UUID id = UUID.fromString(uuid);
		List<SpanBo> spans = traceDao.selectSpanAndAnnotation(id);
		if (spans == null) {
			return Collections.emptyList();
		}
		transitionApiId(spans);
		List<SpanAlign> order = order(spans);
		// TODO root span not found시 row data라도 보여줘야 됨.
		if (order.size() != spans.size()) {
			// TODO 중간 노드 데이터 분실 ? 혹은 잘못된 데이터 생성?
			logger.info("span node not complete! ");
		}
		return order;

	}

	private void transitionApiId(List<SpanBo> spans) {
		for (SpanBo spanBo : spans) {
			List<AnnotationBo> annotationBoList = spanBo.getAnnotationBoList();
			for (AnnotationBo annotationBo : annotationBoList) {
				// TODO API-ID 일단 날코딩 나중에 뭔가 key를 따자
				if ("API-ID".equals(annotationBo.getKey())) {
					MethodMapping methodMapping = null;
					try {
						methodMapping = ApiMappingTable.findMethodMapping((Integer) annotationBo.getValue());
					} catch (Exception e) {
						e.printStackTrace(); // To change body of catch
												// statement use File | Settings
												// | File Templates.
					}
					String className = methodMapping.getClassMapping().getClassName();
					String methodName = methodMapping.getMethodName();
					String[] parameterType = methodMapping.getParameterType();
					String[] parameterName = methodMapping.getParameterName();
					String args = ApiUtils.mergeParameterVariableNameDescription(parameterType, parameterName);
					AnnotationBo api = new AnnotationBo();
					api.setKey("API");
					api.setValue(className + "." + methodName + args);
					annotationBoList.add(api);
					break;
				}
			}
		}
	}

	private List<SpanAlign> order(List<SpanBo> spans) {

		SpanAligner spanAligner = new SpanAligner(spans);
		return spanAligner.sort();
	}
}
