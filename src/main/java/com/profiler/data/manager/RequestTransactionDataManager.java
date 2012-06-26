package com.profiler.data.manager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import com.profiler.config.TomcatProfilerReceiverConfig;
import com.profiler.config.TomcatProfilerReceiverConstant;
import com.profiler.data.store.hbase.put2.DataPutThreadManager;
import com.profiler.data.store.hbase.put2.PutDatabaseData;
import com.profiler.data.store.hbase.put2.PutRequestTransactionData;
import com.profiler.data.store.hbase.put2.PutTPSData;
import com.profiler.data.vo.AgentVO;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestThriftDTO;

public class RequestTransactionDataManager {
	/**
	 * This table temporary saves request and response data. 
	 * After saveData it must removed.
	 */
	private static Hashtable<Integer,RequestThriftDTO> requestDTOTable=new Hashtable<Integer,RequestThriftDTO>();
	private static Hashtable<Integer,RequestThriftDTO> responseDTOTable=new Hashtable<Integer,RequestThriftDTO>();
	private static Hashtable<Integer,RequestDataListThriftDTO> responseDataListDTOTable=new Hashtable<Integer,RequestDataListThriftDTO>();
	private static Hashtable<Integer,byte[]> responseDataByteArrayTable=new Hashtable<Integer,byte[]>();
	/**
	 * This table temporary saves response data. 
	 * After saveData it must removed.
	 */
	private static Hashtable<String,List<Integer>> requestHashCodeListTable=new Hashtable<String,List<Integer>>();
	
	
	/**
	 * Agent's data table. 
	 * It contains agent IP, Port, Agent TCP Port number
	 */
	private static Hashtable<Integer,AgentVO> agentTable=new Hashtable<Integer,AgentVO>();
	/**
	 * Agent list set.
	 */
	private static HashSet<Integer> agentSet=new HashSet<Integer>();
	/**
	 * Request per seconds data table
	 */
	private static Hashtable<String,Integer> reqPerSecTable=new Hashtable<String,Integer>();
	/**
	 * Response per seconds data table
	 */
	private static Hashtable<String,Integer> resPerSecTable=new Hashtable<String,Integer>();
	/**
	 * This table is used to check request url.
	 * After debugging, this table can be removed.
	 */
	private static Hashtable<Integer,String> requestHashCodeTable=new Hashtable<Integer,String>();
	public RequestTransactionDataManager() {
		
	}
	public void addAgent(int hostHashCode,String hostIP,String portNumber,int agentTCPPortNumber) {
		agentTable.put(hostHashCode, new AgentVO(hostIP,portNumber,agentTCPPortNumber));
	}
	public AgentVO getAgentVO(int hashCode) {
		return agentTable.get(hashCode);
	}
	public CharSequence getAgentName(int hashCode) {
		AgentVO agentVO=agentTable.get(hashCode);
		if(agentVO!=null) {
			return agentVO.toString();
		}
		return "No Agent Info";
	}
	public boolean containsAgentInfo(int hashCode) {
		return agentTable.containsKey(hashCode);
	}
	public void addAgentHashCode(int agentHashCode) {
		agentSet.add(agentHashCode);
	}
	private static Object requestHashCodeListTableLock=new Object();
	public void addRequest(RequestThriftDTO dto) {
		int requestDataType=dto.getDataType();
		int agentHashCode=dto.getHostHashCode();
		long dataTime=dto.getDataTime();
		String key=getTimeKey(agentHashCode,dataTime);
//		System.out.println(key);
		
		int requestHashCode=dto.getRequestHashCode();
		if(requestDataType==1) {
			//it is request data 
			requestDTOTable.put(requestHashCode, dto);
			if(reqPerSecTable.containsKey(key)) {
				reqPerSecTable.put(key, reqPerSecTable.get(key)+1);
			} else {
				reqPerSecTable.put(key, 1);
			}
			
		} else {
			//it is response data
			responseDTOTable.put(requestHashCode, dto);
			// insert request and response hashCode to requestHashCodeListTable
			// this data is removed with saveRequestAndResponseData() method when RPS data is fetched.
			synchronized(requestHashCodeListTableLock) {
				List<Integer> requestHashCodeList=null;
				if(requestHashCodeListTable.containsKey(key)) {
					requestHashCodeList=requestHashCodeListTable.get(key);
				} else {
					requestHashCodeList=new ArrayList<Integer>();
				}
				requestHashCodeList.add(requestHashCode);
				requestHashCodeListTable.put(key, requestHashCodeList);
			}
			if(resPerSecTable.containsKey(key)) {
				resPerSecTable.put(key, resPerSecTable.get(key)+1);
			} else {
				resPerSecTable.put(key, 1);
			}
		}
		// This code used to check Request URL.
		// After debugging, this code can be removed.
		if(dto.getDataType()==TomcatProfilerReceiverConstant.DATA_TYPE_REQUEST) {
			String url=dto.getRequestURL();
			requestHashCodeTable.put(requestHashCode, url);
		} else {
			if(requestHashCodeTable.containsKey(requestHashCode)) {
				requestHashCodeTable.remove(key);
			}
		}
	}
	public void addRequestDataList(RequestDataListThriftDTO dto,byte[] original) {
		int requestHashCode=dto.getRequestHashCode();
		responseDataListDTOTable.put(requestHashCode, dto);
		responseDataByteArrayTable.put(requestHashCode, original);
//		System.out.println("added");
	}
	/**
	 * This method saves Request and Response data.
	 * It is called from FetchRPSDataThread.
	 * @param key
	 */
	public void saveRequestAndResponseData(int agentHashCode,String key) {
		List<Integer> hashCodeList=requestHashCodeListTable.get(key);
		if(hashCodeList!=null) {
			requestHashCodeListTable.remove(key);
			
			int hashCodeSize=hashCodeList.size();
			List<RequestThriftDTO> requestDtoList=new ArrayList<RequestThriftDTO>(hashCodeSize);
			List<RequestThriftDTO> responseDtoList=new ArrayList<RequestThriftDTO>(hashCodeSize);
			List<RequestDataListThriftDTO> responseDataListDTOList=new ArrayList<RequestDataListThriftDTO>(hashCodeSize);
			List<byte[]> responseDataByteArrayList=new ArrayList<byte[]>(hashCodeSize);
			for(int hashCode:hashCodeList) {
				RequestThriftDTO tempReqDto=requestDTOTable.get(hashCode);
				requestDTOTable.remove(hashCode);
				RequestThriftDTO tempResDto=responseDTOTable.get(hashCode);
				responseDTOTable.remove(hashCode);
				if(responseDataListDTOTable.containsKey(hashCode)) {
					responseDataListDTOList.add(responseDataListDTOTable.get(hashCode));
					responseDataListDTOTable.remove(hashCode);
					responseDataByteArrayList.add(responseDataByteArrayTable.get(hashCode));
					responseDataByteArrayTable.remove(hashCode);
				} else {
					responseDataListDTOList.add(null);
					responseDataByteArrayList.add(null);
				}
				requestDtoList.add(tempReqDto);
				responseDtoList.add(tempResDto);
//				System.out.println(tempReqDto.getRequestURL());
			}
			if(TomcatProfilerReceiverConfig.USING_HBASE) {
				String tableName=TomcatProfilerReceiverConstant.HBASE_REQUEST_TABLE+"_"+agentHashCode;
				PutRequestTransactionData put=new PutRequestTransactionData(tableName,requestDtoList,responseDtoList,responseDataByteArrayList);
				DataPutThreadManager.execute(put);
				PutDatabaseData put2=new PutDatabaseData(TomcatProfilerReceiverConstant.HBASE_DATABASE_TABLE,responseDataListDTOList);
				DataPutThreadManager.execute(put2);
			}
		}
	}
	private static Hashtable<Integer,List<Integer[]>> agentTPSData=new Hashtable<Integer,List<Integer[]>>(); 
	/**
	 * Saves Request/Response Per Seconds data
	 * @param tempAgent
	 * @param reqPS
	 * @param resPS
	 */
	public void saveTPSData(int agentHashCode,int reqPS,int resPS,long lastDataTime) {
		Integer[] data=new Integer[2];
		data[0]=reqPS;
		data[1]=resPS;
		List<Integer[]> saveDataList=null;
		if(agentTPSData.containsKey(agentHashCode)) {
			List<Integer[]> dataList=agentTPSData.get(agentHashCode);
			dataList.add(data);
			//if dataList size is over 10, It saves data
			if(dataList.size()>10) {
				saveDataList=dataList;
				agentTPSData.put(agentHashCode, new ArrayList<Integer[]>());
			}
		} else {
			List<Integer[]> dataList=new ArrayList<Integer[]>();
			dataList.add(data);
			agentTPSData.put(agentHashCode, dataList);
		}
		if(saveDataList!=null) {
			if(TomcatProfilerReceiverConfig.USING_HBASE) {
				String tableName=TomcatProfilerReceiverConstant.HBASE_TPS_TABLE+"_"+agentHashCode;
				PutTPSData put=new PutTPSData(tableName,saveDataList,lastDataTime);
				DataPutThreadManager.execute(put);
			}
		}
	}
	public String getRequestURL(int requestHashCode) {
		if(requestHashCodeTable.containsKey(requestHashCode)) {
			return requestHashCodeTable.get(requestHashCode);
		} else {
			return "No URL Data";
		}
	}
	/**
	 * Return agent's time values
	 * @param agentHashCode
	 * @param dataTime
	 * @return
	 */
	public String getTimeKey(int agentHashCode,long dataTime) {
		return agentHashCode+TomcatProfilerReceiverConstant.DATE_FORMAT_YMD_HMS.format(new Date(dataTime));
	}
	
	public HashSet<Integer> getAgentSet() {
		return agentSet;
	}
	/**
	 * Return Request Per Second value.
	 * After get data, removes that data.
	 * @param key
	 * @return
	 */
	public int getReqPerSecond(String key) {
		int result;
		if(reqPerSecTable.containsKey(key)) {
			result= reqPerSecTable.get(key);
			reqPerSecTable.remove(key);
		} else {
			result=0;
		}
		return result;
	}
	/**
	 * Return Response Per Second value.
	 * After get data, removes that data.
	 * @param key
	 * @return
	 */
	public int getResPerSecond(String key) {
		int result;
		if(resPerSecTable.containsKey(key)) {
			result= resPerSecTable.get(key);
			resPerSecTable.remove(key);
		} else {
			result=0;
		}
		return result;
	}
	public RequestThriftDTO getRequestDTO(int hashCode) {
		RequestThriftDTO dto=null;
		//Sometimes request data is arrives late than response data.
		if(!requestDTOTable.containsKey(hashCode)) {
			System.out.println("Request data is not arrived yet ! sleep");
			try {
				Thread.sleep(10);
			} catch(Exception e) {
				
			}
		}
		if(requestDTOTable.containsKey(hashCode)) {
			dto=requestDTOTable.get(hashCode);
			requestDTOTable.remove(hashCode);
		}
		return dto;
	}
}
