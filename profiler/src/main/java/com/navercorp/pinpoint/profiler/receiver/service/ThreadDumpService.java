package com.nhn.pinpoint.profiler.receiver.service;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.nhn.pinpoint.thrift.dto.command.TCommandThreadDump;
import com.nhn.pinpoint.thrift.dto.command.TCommandThreadDumpResponse;
import com.nhn.pinpoint.thrift.dto.command.TMonitorInfo;
import com.nhn.pinpoint.thrift.dto.command.TThreadDump;
import com.nhn.pinpoint.thrift.dto.command.TThreadDumpType;
import com.nhn.pinpoint.thrift.dto.command.TThreadState;

/**
 * @author koo.taejin
 */
public class ThreadDumpService implements ProfilerRequestCommandService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public TBase<?, ?> requestCommandService(TBase tbase) {
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

		TCommandThreadDumpResponse response = new TCommandThreadDumpResponse();

		for (ThreadInfo info : threadInfoList) {
			TThreadDump dump = new TThreadDump();

			dump.setThreadName(info.getThreadName());
			dump.setThreadId(info.getThreadId());
			dump.setBlockedTime(info.getBlockedTime());
			dump.setBlockedCount(info.getBlockedCount());
			dump.setWaitedTime(info.getWaitedTime());
			dump.setWaitedCount(info.getWaitedCount());

			dump.setLockName(info.getLockName());
			dump.setLockOwnerId(info.getLockOwnerId());
			dump.setLockOwnerName(info.getLockOwnerName());

			dump.setInNative(info.isInNative());
			dump.setSuspended(info.isSuspended());

			dump.setThreadState(getThreadState(info));

			StackTraceElement[] stackTraceElements = info.getStackTrace();
			for (StackTraceElement each : stackTraceElements) {
				dump.addToStackTrace(each.toString());
			}

			MonitorInfo[] monitorInfos = info.getLockedMonitors();
			for (MonitorInfo each : monitorInfos) {
				TMonitorInfo tMonitorInfo = new TMonitorInfo();

				tMonitorInfo.setStackDepth(each.getLockedStackDepth());
				tMonitorInfo.setStackFrame(each.getLockedStackFrame().toString());

				dump.addToLockedMonitors(tMonitorInfo);
			}

			LockInfo[] lockInfos = info.getLockedSynchronizers();
			for (LockInfo lockInfo : lockInfos) {
				dump.addToLockedSynchronizers(lockInfo.toString());
			}

			response.addToThreadDumps(dump);
		}

		return response;
	}

	private TThreadState getThreadState(ThreadInfo info) {
		
		String stateName = info.getThreadState().name();
		
		for (TThreadState state : TThreadState.values()) {
			if (state.name().equalsIgnoreCase(stateName)) {
				return state;
			}
		}
		
		return null;
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
	
	@Override
	public Class<? extends TBase> getCommandClazz() {
		return TCommandThreadDump.class;
	}

}
