package com.profiler.data.store.hbase.put2;

import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.thrift.async.AsyncMethodCallback;

import com.profiler.data.store.hbase.thrift.Thrift2ClientManager;

@Deprecated
public class PutAsyncResultCallback implements AsyncMethodCallback<THBaseService.AsyncClient.put_call>  {
	String putType=null;
	Thrift2ClientManager manager=null;
	public PutAsyncResultCallback(String putType,Thrift2ClientManager manager) {
		this.putType=putType;
	}
	@Override
	public void onComplete(THBaseService.AsyncClient.put_call response) {
		System.out.println(putType+" is completed !!");
//		manager.closeAsync();
	}

	@Override
	public void onError(Exception exception) {
		System.out.println(putType+" has an error ToT ");
	}

	

}
