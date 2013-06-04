package com.nhn.pinpoint.profiler.trace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.nhn.pinpoint.common.dto.thrift.RequestDataListThriftDTO;
import com.nhn.pinpoint.common.dto.thrift.RequestDataThriftDTO;
import com.nhn.pinpoint.profiler.config.ProfilerConstant;
import com.nhn.pinpoint.profiler.util.NamedThreadLocal;
import com.nhn.pinpoint.profiler.util.QueryStringUtil;

@Deprecated
public class DatabaseRequestTracer {

    public static final String FQCN = DatabaseRequestTracer.class.getName();

    private static ConcurrentMap<Integer, String> dbConnectionURL = new ConcurrentHashMap<Integer, String>();

    private static Set<Integer> sqlSet = null;

    static {
//		if (ProfilerConfig.QUERY_COUNT_OVER_10000) {
//			sqlSet = new CopyOnWriteArraySet<Integer>();
//		} else {
        sqlSet = new HashSet<Integer>(1024);
//		}
    }

    private static final ThreadLocal<RequestDataListThriftDTO> requestDataThreadLocal = new NamedThreadLocal<RequestDataListThriftDTO>("requestDataThreadLocal");
    private static final ThreadLocal<HashMap<Integer, String>> sqlParamMapThreadLocal = new NamedThreadLocal<HashMap<Integer, String>>("sqlParamMapThreadLocal");

    /**
     * These two variables are used counting "ResultSet.next()" times.
     */
    private static final ThreadLocal<Integer> fetchCountThreadLocal = new NamedThreadLocal<Integer>("fetchCountThreadLocal");
    private static final ThreadLocal<Integer> totalFetchCountThreadLocal = new NamedThreadLocal<Integer>("totalFetchCountThreadLocal");

    public static RequestDataListThriftDTO getRequestDataList() {
        return requestDataThreadLocal.get();
    }

    public static void removeRequestDataList() {
        requestDataThreadLocal.remove();
    }

    private static boolean isRequestData() {
        Integer reqHashCode = RequestTracer.getCurrentRequestHash();
        if (reqHashCode == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Put data to requestDataThreadLocal.
     *
     * @param dataType
     */
    public static void put(int dataType) {
        // System.out.println(dataType+"-----RequestHashCode="+RequestTransactionTracer.getRequestHashCode());
        if (isRequestData()) {
            RequestDataListThriftDTO dto = requestDataThreadLocal.get();
            dto = checkDTO(dto);
            List<RequestDataThriftDTO> list = dto.getRequestDataList();
            // System.out.println("-----RequestDataListThriftDTO list size="+list.size());

            checkSqlParamMap(list);

            RequestDataThriftDTO dataDto = new RequestDataThriftDTO(dataType, System.currentTimeMillis());
            list.add(dataDto);
            requestDataThreadLocal.set(dto);
        }
    }

    /**
     * Put Connection data to requestDataThreadLocal.
     *
     * @param dataType
     */
    public static void putConnection(int dataType, String url) {
        // System.out.println(dataType+"-----RequestHashCode="+RequestTransactionTracer.getRequestHashCode());
        if (isRequestData()) {
            RequestDataListThriftDTO dto = requestDataThreadLocal.get();
            dto = checkDTO(dto);
            List<RequestDataThriftDTO> list = dto.getRequestDataList();
            // System.out.println("-----RequestDataListThriftDTO list size="+list.size());

            checkSqlParamMap(list);

            RequestDataThriftDTO dataDto = new RequestDataThriftDTO(dataType, System.currentTimeMillis());
            if (url != null) {
                int hashCode = url.hashCode();
                String before = dbConnectionURL.putIfAbsent(hashCode, url);
                if (before == null) {
                    dataDto.setDataString(url);
                }

                dataDto.setDataHashCode(hashCode);
            }
            list.add(dataDto);
            requestDataThreadLocal.set(dto);
        }
    }

    /**
     * Put SQL Query data into requestDataThreadLocal.
     *
     * @param dataType
     */
    public static void putSqlQuery(int dataType, String data) {
        if (isRequestData()) {
            RequestDataListThriftDTO dto = requestDataThreadLocal.get();
            dto = checkDTO(dto);
            List<RequestDataThriftDTO> list = dto.getRequestDataList();
            // System.out.println("-----RequestDataListThriftDTO list size="+list.size());

            checkSqlParamMap(list);

            RequestDataThriftDTO dataDto = new RequestDataThriftDTO(dataType, System.currentTimeMillis());
            int dataHashCode = data.hashCode();
            dataDto.setDataHashCode(dataHashCode);
            boolean isAlreadySent = checkHashCode(dataHashCode);
            if (!isAlreadySent) {
                if (data != null) {
                    dataDto.setDataString(QueryStringUtil.removeAllMultiSpace(data));
                }
            }
            list.add(dataDto);
            requestDataThreadLocal.set(dto);
        }
    }

    /**
     * Check SQL Query HashCode set. If Query count is over 10000, it can make
     * memory problem. So this method removes 100 hashCode.
     * <p/>
     * If you use HashSet this remove code will not run.
     *
     * @param dataHashCode
     * @return
     */
    private static boolean checkHashCode(int dataHashCode) {
//		if (ProfilerConfig.QUERY_COUNT_OVER_10000) {
        if (true) {
            // If sqlSet is CopyOnWriteArraySet, it removes data.
            if (sqlSet.size() > 10000) {
                Iterator<Integer> iterator = sqlSet.iterator();
                for (int loop = 0; loop < 100; loop++) {
                    sqlSet.remove(iterator.next());
                }
            }
        }
        if (sqlSet.contains(dataHashCode)) {
            return true;
        } else {
            sqlSet.add(dataHashCode);
            return false;
        }
    }

    /**
     * Manage sql param list
     *
     * @param list
     */
    private static void checkSqlParamMap(List<RequestDataThriftDTO> list) {
        if (isRequestData()) {
            HashMap<Integer, String> map = sqlParamMapThreadLocal.get();
            if (map != null) {
                int mapSize = map.size();
                StringBuilder params = new StringBuilder();
                for (int loop = 1; loop <= mapSize; loop++) {
                    params.append(map.get(loop)).append(",");
                }

                RequestDataThriftDTO dataDto = new RequestDataThriftDTO(ProfilerConstant.REQ_DATA_TYPE_DB_PREPARED_STATEMENT_PARAM, System.currentTimeMillis());
                dataDto.setDataString(params.toString());
                list.add(dataDto);
            }
            sqlParamMapThreadLocal.remove();
        }
    }

    /**
     * Add sql parameter
     *
     * @param sequence
     * @param data
     */
    public static void putSqlParam(int sequence, String data) {
        if (isRequestData()) {
            HashMap<Integer, String> map = sqlParamMapThreadLocal.get();
            if (map == null) {
                map = new HashMap<Integer, String>();
            }
            // if(data!=null) {
            // try {
            // System.out.print("Before="+data);
            // String afterData1=new String(data.getBytes("MS949"),
            // "ISO-8859-1");;
            // System.out.print(" After1="+afterData1);
            // String afterData1_1=new String(afterData1.getBytes("ISO-8859-1"),
            // "EUC-KR");;
            // System.out.print(" After1_1="+afterData1_1);
            //
            //
            // String afterData3=new
            // String(data.getBytes("ISO-8859-1"),"MS949");
            // System.out.print(" After3="+afterData3);
            // String afterData3_1=new
            // String(afterData3.getBytes("MS949"),"EUC-KR");
            // System.out.print(" After3_1="+afterData3_1);
            //
            // String afterData5=new
            // String(data.getBytes("ISO-8859-1"),"EUC-KR");
            // System.out.print(" After5="+afterData5);
            // String afterData6=new String(data.getBytes("MS949"),"EUC-KR");
            // System.out.print(" After6="+afterData6);
            //
            // String afterData7=new String(data.getBytes(),"ISO-8859-1");
            // System.out.print(" After7="+afterData7);
            //
            // System.out.println();
            // } catch(Exception e) {
            // e.printStackTrace();
            // }
            // }
            map.put(sequence, data);
            sqlParamMapThreadLocal.set(map);
        }
    }

    public static void putSqlParam(int sequence, byte[] data) {
        putSqlParam(sequence, new String(data));
    }

    public static void putSqlParam(int sequence, Object data) {
        if (data != null) {
            putSqlParam(sequence, data.toString());
        } else {
            putSqlParam(sequence, "null");
        }
    }

    /**
     * Check RequestDataListThriftDTO is null. If this object is null, current
     * request called this Class first time. So it make RequestDataListThriftDTO
     * object.
     *
     * @param dto
     * @return
     */
    private static RequestDataListThriftDTO checkDTO(RequestDataListThriftDTO dto) {
        if (dto == null) {
//			dto = new RequestDataListThriftDTO(Agent.getInstance().getAgentId(), RequestTracer.getCurrentRequestHash(), new ArrayList<RequestDataThriftDTO>());
        }
        return dto;
    }

    /**
     * add ResultSet.next() method call count.
     */
    public static void updateFetchCount() {
        Integer totalFetchCount = totalFetchCountThreadLocal.get();
        Integer fetchCount = fetchCountThreadLocal.get();
        if (totalFetchCount == null) {
            totalFetchCountThreadLocal.set(0);
        }
        if (fetchCount == null) {
            fetchCountThreadLocal.set(0);
        }
        totalFetchCountThreadLocal.set(totalFetchCountThreadLocal.get() + 1);
        fetchCountThreadLocal.set(fetchCountThreadLocal.get() + 1);
    }

    /**
     * Before transaction end, removes current thread's fetch count data.
     */
    public static void removeFetchCount() {
        totalFetchCountThreadLocal.remove();
        fetchCountThreadLocal.remove();
    }

    /**
     * If ResultSet.close() method is called, this method is called.
     */
    public static void addResultSetData() {
        if (isRequestData()) {
            // set data fetch count
            Integer fetchCount = fetchCountThreadLocal.get();
            if (fetchCount != null) {
                Integer totalFetchCount = totalFetchCountThreadLocal.get();
                fetchCountThreadLocal.remove();

                RequestDataListThriftDTO dto = requestDataThreadLocal.get();
                dto = checkDTO(dto);
                List<RequestDataThriftDTO> list = dto.getRequestDataList();
                int listSize = list.size();
                RequestDataThriftDTO previousDTO = list.get(listSize - 1);
                if (previousDTO.getDataType() != ProfilerConstant.REQ_DATA_TYPE_DB_FETCH) {
                    RequestDataThriftDTO dataDto = new RequestDataThriftDTO(ProfilerConstant.REQ_DATA_TYPE_DB_FETCH, System.currentTimeMillis());
                    dataDto.setExtraInt1(fetchCount);
                    dataDto.setExtraInt2(totalFetchCount);
                    list.add(dataDto);
                } else {
                    // Because of MS SQL.
                    int previousTotalFetchCount = previousDTO.getExtraInt2();
                    totalFetchCountThreadLocal.set(previousTotalFetchCount);
                }
                requestDataThreadLocal.set(dto);
            }
        }
    }
}
