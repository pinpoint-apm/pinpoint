package com.profiler.data.store.hbase.put2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataPutThreadManager {
	private static final int corePoolSize=10;
	private static final int maximumPoolSize=1024;
	private static final long keepAliveTime=10;
	private static final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
	protected static final ThreadPoolExecutor executor=new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,TimeUnit.SECONDS,queue);
	public DataPutThreadManager() {}
	public static void execute(AbstractPutData put) {
		executor.execute(put);
	}
}
