package com.nhn.pinpoint.trace;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.nhn.pinpoint.DefaultAgent;
import com.nhn.pinpoint.common.dto.thrift.RequestDataListThriftDTO;
import com.nhn.pinpoint.common.dto.thrift.RequestThriftDTO;
import com.nhn.pinpoint.config.ProfilerConstant;

@Deprecated
public class RequestTracer {

    public static final String FQCN = RequestTracer.class.getName();

    private static final ThreadLocal<String> currentRequestID = new ThreadLocal<String>();
    private static final ThreadLocal<Integer> currentRequestHash = new ThreadLocal<Integer>();
    private static final Set<String> requestSet = Collections.synchronizedSet(new HashSet<String>());

    public static void startTransaction(String requestURL, String clientIP, long requestTime, String parameters) {
//        long cpuUserTime[] = SystemUtils.getThreadTime();
        long cpuUserTime[] = null;
        String tempRequestID = Thread.currentThread().getName() + "_" + System.nanoTime();
        int tempRequestHashCode = tempRequestID.hashCode();

        currentRequestID.set(tempRequestID);
        currentRequestHash.set(tempRequestHashCode);
        requestSet.add(tempRequestID);

        RequestThriftDTO dto = new RequestThriftDTO(DefaultAgent.getInstance().getAgentId(), tempRequestHashCode, ProfilerConstant.DATA_TYPE_REQUEST, requestTime, cpuUserTime[0], cpuUserTime[1]);
        dto.setClientIP(clientIP);
        dto.setRequestURL(requestURL);

//		int paramsLength = params.length();
//		if (paramsLength > 0) {
//			params.deleteCharAt(paramsLength - 1);
//			dto.setExtraData1(params.toString());
//		}

//        UdpDataSender.getInstance().send(dto);
    }

    /**
     * Transaction is successfully ended.
     */
    public static void endTransaction() {
        long cpuUserTime[] = null;
//        long cpuUserTime[] = SystemUtils.getThreadTime();
        RequestThriftDTO dto = new RequestThriftDTO(DefaultAgent.getInstance().getAgentId(), currentRequestHash.get(), ProfilerConstant.DATA_TYPE_RESPONSE, System.currentTimeMillis(), cpuUserTime[0], cpuUserTime[1]);

        finishTransaction(dto);
    }

    /**
     * There was an Exception processing transaction.
     *
     * @param throwable
     */
    public static void exceptionTransaction(Throwable throwable) {
//        long cpuUserTime[] = SystemUtils.getThreadTime();
        long cpuUserTime[] = null;
        RequestThriftDTO dto = new RequestThriftDTO(DefaultAgent.getInstance().getAgentId(), currentRequestHash.get(), ProfilerConstant.DATA_TYPE_UNCAUGHT_EXCEPTION, System.currentTimeMillis(), cpuUserTime[0], cpuUserTime[1]);

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
//            UdpDataSender.getInstance().send(dataListDto);
        }

//        UdpDataSender.getInstance().send(dto);

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
}
