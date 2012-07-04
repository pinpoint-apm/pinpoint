package com.profiler.data.store.hbase.get;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.TCell;

import com.profiler.data.store.hbase.thrift.ThriftClientManager;
import com.profiler.util.Converter;

public abstract class AbstractGetData {
	public static final String NEW_LINE="<BR>";
	public static final String COMMA=",";
	public static final String SPACES="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	ThriftClientManager manager;
	
	String dataType=null;
	public AbstractGetData(String dataType) {
		this.dataType=dataType;
	}
	protected Hbase.Client getClient() {
		long startTime=System.nanoTime();
		long clientConnectionTime=startTime;
		Hbase.Client client=null;
		try {
			manager=new ThriftClientManager();
			client=manager.getClient();
			clientConnectionTime=System.nanoTime();
			log(dataType+" Connection="+(clientConnectionTime-startTime)/1000000.0+"ms.");
			return client;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		} finally {
//			log("Fetch Time="+(endTime-clientConnectionTime)+"ms.");	
		}
	}
	public CharSequence get() {
		long startTime=System.nanoTime();
		CharSequence result=getData();
		long endTime=System.nanoTime();
		close();
		log(dataType+" get="+(endTime-startTime)/1000000.0+" ms.");
		log("====================================================");
		return result;
	}
	public abstract CharSequence getData();
	protected void close() {
		manager.close();
	}
	
	protected void log(String message) {
		System.out.println(message);
	}
	public String[] seperateFamilyQualifier(String key) {
		return key.split(":");
	}
	protected String[] getDataKeyList(int columnSize,Map<ByteBuffer,TCell> columns) {
		Set<ByteBuffer> keySet=columns.keySet();
		Iterator<ByteBuffer> iterator=keySet.iterator();
		String keyList[]=new String[columnSize];
		for(int loop=0;loop<columnSize;loop++) {
			keyList[loop]=Converter.toString(iterator.next());
		}
		Arrays.sort(keyList);
		return keyList;
	}
	
	protected void appendLineGraphData(int from,int perColumnDataCount,Map<ByteBuffer,TCell> columns,String keyList[],StringBuilder builder) {
		for(int loop=0;loop<perColumnDataCount;loop++) {
//			TCell tempValue=columns.get(Converter.toByteBuffer(keyList[]));
			String value=getStringValue(from*perColumnDataCount+loop,columns,keyList);
			builder.append(value).append(COMMA);
		}
	}
	protected long getLongValue(int pos,Map<ByteBuffer,TCell> columns,String keyList[]) {
		String value=getStringValue(pos,columns,keyList);
		return Long.parseLong(value);
	}
	protected String getStringValue(int pos,Map<ByteBuffer,TCell> columns,String keyList[]) {
		TCell tempValue=columns.get(Converter.toByteBuffer(keyList[pos]));
		String value=new String(tempValue.getValue());
		return value;
	}
	
	
}
