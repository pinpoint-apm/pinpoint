package com.nhn.pinpoint.profiler.receiver.bo;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.profiler.receiver.TBaseRequestBO;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandThreadDump;
import com.nhn.pinpoint.thrift.dto.command.TThreadDumpType;

/**
 * @author koo.taejin
 */
public class ThreadDumpBO implements TBaseRequestBO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public TBase<?, ?> handleRequest(TBase tbase) {
		logger.info("{} execute {}.", this, tbase);
		
		TCommandThreadDump param = (TCommandThreadDump) tbase;
		TThreadDumpType type = param.getType();
		
		List<ThreadInfo> threadInfoList = null;
		if (TThreadDumpType.TARGET == type) {
			threadInfoList = getThreadInfo(param.getName());
		} else if (TThreadDumpType.PENDING == type) {
			threadInfoList = getThreadInfo(param.getPendingTimeMillis());
		} else {
			threadInfoList = Arrays.asList(getAllThreadInfo());
		}
		
		StringBuilder dump = new StringBuilder();
		for (ThreadInfo info : threadInfoList) {
			dump.append(info.getThreadName());
			dump.append("\t Thread.State: ");
            dump.append(info.getThreadState());
			
			StackTraceElement[] elements = info.getStackTrace();
			for (StackTraceElement element : elements) {
				dump.append("\r\n\t at");
                dump.append(element);
			}
			dump.append("\r\n");
			dump.append("\r\n");
		}
		
		TResult result = new TResult(true);
		result.setMessage(dump.toString());

		logger.debug("Result = {}", result);
		
		return result;
	}

	private List<ThreadInfo> getThreadInfo(String threadName) {
		List<ThreadInfo> result = new ArrayList<ThreadInfo>();
		
		if (threadName == null || threadName.trim().equals("")) {
			return Arrays.asList(getAllThreadInfo());
		}

		ThreadInfo[] threadInfos = getAllThreadInfo();
		for (ThreadInfo threadIno : getAllThreadInfo()) {
			if (threadName.equals(threadIno.getThreadName())) {
				result.add(threadIno);
			}
		}

		return result;
	}

	// 이건 나중에 수정이 필요함
	private List<ThreadInfo> getThreadInfo(long pendingTimeMillis) {
		List<ThreadInfo> result = new ArrayList<ThreadInfo>();

		if (pendingTimeMillis <= 0) {
			return Arrays.asList(getAllThreadInfo());
		}

		for (ThreadInfo threadInfo : getAllThreadInfo()) {
			if (threadInfo.getBlockedTime() >= pendingTimeMillis) {
				result.add(threadInfo);
				continue;
			}
			
			if (threadInfo.getWaitedTime() >= pendingTimeMillis) {
				result.add(threadInfo);
				continue;
			}
		}

		return result;
	}

	private ThreadInfo[] getAllThreadInfo() {
		ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMxBean.getThreadInfo(threadMxBean.getAllThreadIds(), 100);

		return threadInfos;
	}

}
