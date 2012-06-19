package com.profiler.trace;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.dto.AgentInfoDTO;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestThriftDTO;
import com.profiler.sender.RequestDataSender;
import com.profiler.sender.RequestTransactionDataSender;

public class RequestTransactionTracer extends AbstractTracer {
//	private static Hashtable<String,RequestThriftDTO> requestTable=new Hashtable<String,RequestThriftDTO>();
	private static Set<String> requestSet=null;
	static{
		requestSet=Collections.synchronizedSet(new HashSet<String>());
	}

	public RequestTransactionTracer() {
	}
	private static final ThreadLocal<String> requestID=new ThreadLocal<String>();
	private static final ThreadLocal<Integer> requestHashCode=new ThreadLocal<Integer>();
	public static Integer getRequestHashCode() {
		return requestHashCode.get();
	}
	public static void startTransaction(String requestURL,String clientIP,long requestTime,StringBuilder params) {
//		printStackTrace();
//		long cpuUserTime[]=getThreadTime();
		long cpuUserTime[]=new long[2];
		
		String currentThreadName=Thread.currentThread().getName();
		//### set Thread id with thread local
		String tempRequestID=currentThreadName+"_"+System.nanoTime();
		requestID.set(tempRequestID);
		int tempRequestHashCode=tempRequestID.hashCode();
		requestHashCode.set(tempRequestHashCode);
		RequestThriftDTO dto=new RequestThriftDTO(AgentInfoDTO.staticHostHashCode,tempRequestHashCode,TomcatProfilerConstant.DATA_TYPE_REQUEST,requestTime,cpuUserTime[0],cpuUserTime[1]);
		dto.setClientIP(clientIP);
		dto.setRequestURL(requestURL);
		int paramsLength=params.length();
		if(paramsLength>0) {
			params.deleteCharAt(paramsLength-1);
			dto.setExtraData1(params.toString());
		}
		requestSet.add(requestID.get());
		RequestTransactionDataSender sender=new RequestTransactionDataSender(dto);
		sender.send();
	}
	/**
	 * Transaction is successfully ended.
	 */
	public static void endTransaction() {
//		long cpuUserTime[]=getThreadTime();
		long cpuUserTime[]=new long[2];
		RequestThriftDTO dto=new RequestThriftDTO(AgentInfoDTO.staticHostHashCode,requestHashCode.get(),TomcatProfilerConstant.DATA_TYPE_RESPONSE,System.currentTimeMillis(),cpuUserTime[0],cpuUserTime[1]);
		
		finishTransaction(dto);
	}
	/**
	 * There was an Exception processing transaction.
	 * @param throwable
	 */
	public static void exceptionTransaction(Throwable throwable) {
//		long cpuUserTime[]=getThreadTime();
		long cpuUserTime[]=new long[2];
		RequestThriftDTO dto=new RequestThriftDTO(AgentInfoDTO.staticHostHashCode,requestHashCode.get(),TomcatProfilerConstant.DATA_TYPE_UNCAUGHT_EXCEPTION,System.currentTimeMillis(),cpuUserTime[0],cpuUserTime[1]);
		dto.setExtraData1(throwable.getMessage());
		StackTraceElement[] tempElement=throwable.getStackTrace();
		dto.setExtraData2(tempElement[0].toString());
		
		finishTransaction(dto);
	}
	/**
	 * Transaction is ended and send request end data
	 * @param dto
	 */
	public static void finishTransaction(RequestThriftDTO dto) {
		RequestDataListThriftDTO dataListDto=RequestDataTracer.getRequestDataList();
		if(dataListDto!=null) {
			RequestDataSender dSender=new RequestDataSender(dataListDto);
			dSender.send();
		}
		RequestTransactionDataSender tSender=new RequestTransactionDataSender(dto);
		tSender.send();
		
		requestSet.remove(requestID.get());
		RequestDataTracer.removeRequestDataList();
		
		RequestDataTracer.removeFetchCount();
	}
	
	public static int getActiveThreadCount() {
		return requestSet.size();
	}
	/* If every time call Thread's CPU time 
	 * it affect to TPS and CPU usage.
	 * It is one of bottle neck.
	public static long[] getThreadTime() {
		long result[]=new long[2];
		
		ThreadMXBean bean=ManagementFactory.getThreadMXBean();
//		System.out.println(Thread.currentThread().getName()+" CPU:"+bean.getCurrentThreadCpuTime()+" User:"+bean.getCurrentThreadUserTime());
		result[0]=bean.getCurrentThreadCpuTime();
		result[1]=bean.getCurrentThreadUserTime();
		return result;
	}
	*/
}
