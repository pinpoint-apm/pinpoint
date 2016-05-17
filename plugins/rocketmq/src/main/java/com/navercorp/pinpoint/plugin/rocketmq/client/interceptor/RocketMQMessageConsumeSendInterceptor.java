package com.navercorp.pinpoint.plugin.rocketmq.client.interceptor;

import com.alibaba.rocketmq.common.message.MessageDecoder;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rocketmq.client.RocketMQConstants;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class RocketMQMessageConsumeSendInterceptor extends SpanSimpleAroundInterceptor {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

	public RocketMQMessageConsumeSendInterceptor(TraceContext traceContext) {
		this(traceContext, new RocketMQConsumerEntryMethodDescriptor());
	}

	private RocketMQMessageConsumeSendInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
		super(traceContext, methodDescriptor, RocketMQMessageConsumeSendInterceptor.class);
		traceContext.cacheApi(methodDescriptor);
	}

	@Override
	protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
		recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT);

		List<MessageExt> msgs = (List<MessageExt>) args[0];

		MessageExt messageext = msgs.get(0);
		if (null == messageext) {
			return;
		}
		recorder.recordAttribute(RocketMQConstants.ROCKETMQ_MESSAGE, new String(messageext.getBody()));
		try {

			// 消费者的本机名
			recorder.recordEndPoint(InetAddress.getLocalHost().getHostAddress());
			// 消费者的连接的主机名
			InetSocketAddress remoteAddress=(InetSocketAddress) messageext.getStoreHost();
			recorder.recordRemoteAddress(remoteAddress.getAddress().getHostAddress());

			// 订阅的Topic
			recorder.recordRpcName(messageext.getTopic());

			// 消息存储的地址
			recorder.recordAcceptorHost(messageext.getTopic());

			String parentApplicationName = messageext.getProperties().get("Pinpoint-pAppName");
			short parentApplicationType=-1;
			if (!recorder.isRoot() && parentApplicationName != null) {
				parentApplicationType = Short.parseShort(messageext.getProperties().get("Pinpoint-pAppType"));
				recorder.recordParentApplication(parentApplicationName, parentApplicationType);
			}
		} catch (Exception e) {
			logger.error(e + "");
		}
	}

	@Override
	protected Trace createTrace(Object target, Object[] args) {
		List<MessageExt> msgs = (List<MessageExt>) args[0];
		MessageExt messageext = msgs.get(0);
		if (null == messageext) {
			return traceContext.newTraceObject();
		}
		Map<String, String> header = messageext.getProperties();
		String transactionId = header.get("Pinpoint-TraceID");
		long spanId = -1;
		if (null != header.get("Pinpoint-SpanID")) {
			spanId = Long.parseLong(header.get("Pinpoint-SpanID"));
		}
		long parentSpanId = -1;
		if (null != header.get("Pinpoint-pSpanID")) {
			parentSpanId = Long.parseLong(header.get("Pinpoint-pSpanID"));
		}
		short flags = 0;
		if (null != header.get("Pinpoint-Flags")) {
			flags = Short.parseShort(header.get("Pinpoint-Flags"));
		}
		header.get("Pinpoint-pAppName");
		header.get("Pinpoint-pAppType");
		System.out.println(MessageDecoder.messageProperties2String(header));
		if (transactionId != null) {
			final TraceId traceId = traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
			return traceContext.continueTraceObject(traceId);
		} else {
			return traceContext.newTraceObject();
		}
	}

	@Override
	protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result,
			Throwable throwable) {
		recorder.recordApi(methodDescriptor);
		if (throwable != null) {
			recorder.recordException(throwable);
		}

	}

}
