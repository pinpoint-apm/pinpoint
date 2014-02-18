package com.nhn.pinpoint.collector.handler;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.collector.dao.HostApplicationMapDao;
import com.nhn.pinpoint.collector.dao.TracesDao;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;
import com.nhn.pinpoint.common.util.SpanEventUtils;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 * @author netspider
 */
@Service
public class SpanHandler implements SimpleHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TracesDao traceDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private StatisticsHandler statisticsHandler;

	@Autowired
	private HostApplicationMapDao hostApplicationMapDao;

	public void handler(TBase<?, ?> tbase) {

		if (!(tbase instanceof TSpan)) {
			throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
		}

		try {
			final TSpan span = (TSpan) tbase;
			if (logger.isDebugEnabled()) {
				logger.debug("Received SPAN={}", span);
			}

            traceDao.insert(span);
			applicationTraceIndexDao.insert(span);

            // map 통계 정보 저장.
            insertAcceptorHost(span);
            insertSpanStat(span);
            insertSpanEventStat(span);
		} catch (Exception e) {
			logger.warn("Span handle error. Caused:{}. Span:{}",e.getMessage(), tbase, e);
		}
	}

    private void insertSpanStat(TSpan span) {
        // TODO span.isSetErr(); 로 변경해야 되는것이 아닌가???
        final boolean isError = span.getErr() != 0;
        int bugCheck = 0;
        if (span.getParentSpanId() == -1) {
            // FIXME 테스트용. host값에 agentId를 입력.
            // user를 생성하는 부분
            statisticsHandler.updateCaller(span.getApplicationName(), ServiceType.USER.getCode(), span.getAgentId(), span.getApplicationName(), span.getServiceType(), span.getAgentId(), span.getElapsed(), isError);
            statisticsHandler.updateCallee(span.getApplicationName(), span.getServiceType(), span.getApplicationName(), ServiceType.USER.getCode(), span.getAgentId(), span.getElapsed(), isError);
            bugCheck++;
        }

        // parentApplicationContext가 있으면 statistics정보를 저장한다.
        // 통계정보에 기반한 서버 맵을 그릴 때 WAS1 -> WAS2요청에서 WAS2를 호출한 application의 이름
        // 즉, WAS1을 알아야한다.
        if (span.getParentApplicationName() != null) {
            logger.debug("Received parent application name. {}", span.getParentApplicationName());
            // TODO 원래는 부모의 serviceType을 알아야 한다.
            // 여기에서는 그냥 부모는 모두 TOMCAT이라 가정하고 테스트.
            statisticsHandler.updateCallee(span.getApplicationName(), span.getServiceType(), span.getParentApplicationName(), span.getParentApplicationType(), span.getAgentId(), span.getElapsed(), isError);
            // statisticsHandler.updateCallee(span.getParentApplicationName(), span.getParentApplicationType(), span.getApplicationName(), span.getServiceType(), span.getEndPoint(), span.getElapsed(), span.getErr() > 0);
            bugCheck++;
        }

        // 자기 자신(Tomcat, BLOC)의 responseTime 레코딩
        // 위의 calee 키와 충돌이 나는 성격이 있는것으로 보임. 이미 호출자에서 caller데이터를 레코딩하고 있는데 반대로 레코딩을 해야 되는 부분이 좀 이상함.
        // 해당 데이터는 진정한 호출자 데이터가 아님. timeout이라던가 네트워크 오류로 인해서 데이터 자체는 틀릴수 있음.
        //
        statisticsHandler.updateResponseTime(span.getApplicationName(), span.getServiceType(), span.getAgentId(), span.getElapsed(), isError);

        if (bugCheck != 1) {
            logger.warn("ambiguous span found(bug). span:{}", span);
        }
    }

    private void insertSpanEventStat(TSpan span) {

        final List<TSpanEvent> spanEventList = span.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }

        logger.debug("handle spanEvent size:{}", spanEventList.size());
        // TODO 껀바이 껀인데. 나중에 뭔가 한번에 업데이트 치는걸로 변경해야 될듯.
        for (TSpanEvent spanEvent : spanEventList) {
            ServiceType serviceType = ServiceType.findServiceType(spanEvent.getServiceType());
            if (!serviceType.isRecordStatistics()) {
                continue;
            }

            // if terminal update statistics
            final int elapsed = spanEvent.getEndElapsed();
            final boolean hasException = SpanEventUtils.hasException(spanEvent);

            // 통계정보에 기반한 서버맵을 그리기 위한 정보 저장.
            // 내가 호출한 정보 저장. (span이 호출한 spanevent)
            statisticsHandler.updateCaller(span.getApplicationName(), span.getServiceType(), span.getAgentId(), spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);

            // 나를 호출한 정보 저장 (spanevent를 호출한 span)
            statisticsHandler.updateCallee(spanEvent.getDestinationId(), spanEvent.getServiceType(), span.getApplicationName(), span.getServiceType(), span.getEndPoint(), elapsed, hasException);
        }
    }

    private void insertAcceptorHost(TSpan span) {
        // host application map 저장.
        // root span이 아닌 경우에만 profiler에서 acceptor host를 채워주게 되어있다.
        final String acceptorHost = span.getAcceptorHost();
        if (acceptorHost == null) {
            return;
        }
        hostApplicationMapDao.insert(acceptorHost, span.getApplicationName(), span.getServiceType());
    }
}
