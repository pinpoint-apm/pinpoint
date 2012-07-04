package com.profiler.util;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.thrift2.generated.TColumn;
import org.apache.hadoop.hbase.thrift2.generated.TColumnValue;
import org.apache.hadoop.hbase.thrift2.generated.TResult;

import com.profiler.config.TomcatProfilerReceiverConstant;

public class Converter {
	public static ByteBuffer toByteBuffer(String data) {
		return ByteBuffer.wrap(data.getBytes());
	}
	public static String toString(ByteBuffer data) {
		return new String(data.array());
	}
	public static String toHmsMs(byte[] byteLong) {
		return toHmsMs(Long.parseLong(new String(byteLong)));
		
	}
	public static String toHmsMs(long dateTime) {
		return TomcatProfilerReceiverConstant.DATE_FORMAT_HMS_MS.format(new Date(dateTime));
	}
	public static String toDecimalComma(byte[] byteLong) {
		Long longValue=Long.parseLong(new String(byteLong));
		return TomcatProfilerReceiverConstant.DECIMAL_FORMAT_THOUSAND_COMMA.format(longValue);
	}
	public static String toDecimalDot(double decimalValue) {
		return TomcatProfilerReceiverConstant.DECIMAL_FORMAT_DOT.format(decimalValue);
	}
	public static Map<String,String> columnListToMap(List<TColumnValue> columnList,Map<String,String> columnMap,boolean ignoreQualifier) {
		if(columnMap==null) {
			columnMap=new HashMap<String,String>();
		}
		for(TColumnValue columnValue:columnList) {
			String qualifierString="";
			if(!ignoreQualifier) {
				byte[] qualifier=columnValue.getQualifier();
//				System.out.println("Qualifier="+new String(qualifier));
				qualifierString=":"+new String(qualifier);
			}
			columnMap.put(new String(columnValue.getFamily())+qualifierString, new String(columnValue.getValue()));
		}
		return columnMap;
	}
	public static Map<String,String> tResultToMap(TResult result,Map<String,String> columnMap,boolean ignoreQualifier) {
		List<TColumnValue> columnList=result.getColumnValues();
		return columnListToMap(columnList,columnMap,ignoreQualifier);
	}
	public static TColumn toTColumn(String columnName) {
		TColumn column=new TColumn();
		column.setFamily(columnName.getBytes());
		return column;
	}
}
