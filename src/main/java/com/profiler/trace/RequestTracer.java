package com.profiler.trace;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.dto.AgentInfoDTO;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestThriftDTO;
import com.profiler.sender.DataSender;

public class RequestTracer {

	public static final String FQCN = RequestTracer.class.getName();

	private static final ThreadLocal<String> currentRequestID = new ThreadLocal<String>();
	private static final ThreadLocal<Integer> currentRequestHash = new ThreadLocal<Integer>();
	private static final Set<String> requestSet = Collections.synchronizedSet(new HashSet<String>());

	public static void startTransaction(String requestURL, String clientIP, long requestTime, StringBuilder params) {
		long cpuUserTime[] = getThreadTime();

		String tempRequestID = Thread.currentThread().getName() + "_" + System.nanoTime();
		int tempRequestHashCode = tempRequestID.hashCode();

		currentRequestID.set(tempRequestID);
		currentRequestHash.set(tempRequestHashCode);
		requestSet.add(tempRequestID);

		RequestThriftDTO dto = new RequestThriftDTO(AgentInfoDTO.staticHostHashCode, tempRequestHashCode, TomcatProfilerConstant.DATA_TYPE_REQUEST, requestTime, cpuUserTime[0], cpuUserTime[1]);
		dto.setClientIP(clientIP);
		dto.setRequestURL(requestURL);

		int paramsLength = params.length();
		if (paramsLength > 0) {
			params.deleteCharAt(paramsLength - 1);
			dto.setExtraData1(params.toString());
		}

		DataSender.getInstance().addDataToSend(dto);
	}

	/**
	 * Transaction is successfully ended.
	 */
	public static void endTransaction() {
		long cpuUserTime[] = getThreadTime();
		RequestThriftDTO dto = new RequestThriftDTO(AgentInfoDTO.staticHostHashCode, currentRequestHash.get(), TomcatProfilerConstant.DATA_TYPE_RESPONSE, System.currentTimeMillis(), cpuUserTime[0], cpuUserTime[1]);

		finishTransaction(dto);
	}

	/**
	 * There was an Exception processing transaction.
	 * 
	 * @param throwable
	 */
	public static void exceptionTransaction(Throwable throwable) {
		long cpuUserTime[] = getThreadTime();

		RequestThriftDTO dto = new RequestThriftDTO(AgentInfoDTO.staticHostHashCode, currentRequestHash.get(), TomcatProfilerConstant.DATA_TYPE_UNCAUGHT_EXCEPTION, System.currentTimeMillis(), cpuUserTime[0], cpuUserTime[1]);

		dto.setExtraData1(throwable.getMessage());

		StackTraceElement[] tempElement = throwable.getStackTrace();
		dto.setExtraData2(tempElement[0].toString());

		finishTransaction(dto);
	}

	/**
	 * Transaction is ended and send request end data
	 * 
	 * @param dto
	 */
	private static void finishTransaction(RequestThriftDTO dto) {
		RequestDataListThriftDTO dataListDto = DatabaseRequestTracer.getRequestDataList();

		if (dataListDto != null) {
			DataSender.getInstance().addDataToSend(dataListDto);
		}

		DataSender.getInstance().addDataToSend(dto);

		requestSet.remove(currentRequestID.get());
		DatabaseRequestTracer.removeRequestDataList();
		DatabaseRequestTracer.removeFetchCount();
	}

	public static int getActiveThreadCount() {
		return requestSet.size();
	}

	public static Integer getCurrentRequestHash() {
		return currentRequestHash.get();
	}

	/**
	 * If every time call Thread's CPU time it affect to TPS and CPU usage. It
	 * is one of bottle neck.
	 * 
	 * @return
	 */
	public static long[] getThreadTime() {
		long result[] = new long[2];

		// ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		//
		// System.out.println(Thread.currentThread().getName() + " CPU:" +
		// bean.getCurrentThreadCpuTime() + " User:" +
		// bean.getCurrentThreadUserTime());
		//
		// result[0] = bean.getCurrentThreadCpuTime();
		// result[1] = bean.getCurrentThreadUserTime();

		return result;
	}
}
