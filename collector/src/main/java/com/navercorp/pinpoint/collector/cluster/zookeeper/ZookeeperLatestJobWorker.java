/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster.zookeeper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.cluster.WorkerState;
import com.navercorp.pinpoint.collector.cluster.WorkerStateContext;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.TimeoutException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.job.DeleteJob;
import com.navercorp.pinpoint.collector.cluster.zookeeper.job.Job;
import com.navercorp.pinpoint.collector.cluster.zookeeper.job.UpdateJob;
import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.navercorp.pinpoint.rpc.util.MapUtils;

/**
 * Class should be thread-safe as jobs are only executed in-order inside the class
 * 
 * @author koo.taejin
 */
public class ZookeeperLatestJobWorker implements Runnable {

    private static final Charset charset = Charset.forName("UTF-8")

	private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-clus    er";
	private static final String PINPOINT_COLLECTOR_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/coll    ctor";

	private static final String PATH_SEPR    TOR = "/";
	private static final String PROFILER_SEPERA    OR = "\r\n";

	private final Logger logger = LoggerFactory.getLogger(t    is.getClass());

	private final Object l    ck = new Object();

	private final WorkerSt    teContext workerState;
	private f    nal Thread workerThread;

	private fi       al String collectorUniqPath;
	
	private fina     ZookeeperClient zookeeperClient;

	private final ConcurrentHashMap<ChannelContext, Job> latestJobRepository = new Conc    rrentHashMap<ChannelContext, Job>();

	// Storage for ma    aging ChannelContexts received by Worker
	private final CopyOnWriteArrayList<ChannelContext> channelContextRepository =     ew CopyOnWriteArrayList<ChannelContext>();

	private final BlockingQueue<Job>     eakJobQueue = new LinkedBlockingQueue<Job>();

	public ZookeeperLatestJobWorker(Zookeeper       lient zookeeperClient, String server       dentifier) {
		this.zookeeperClient = zoo       eeperClient;

		this.workerState = new WorkerStateContext();

		this.collectorUniqPath = bin             ingPathAndZnode(PINPOINT_COLLECTOR_CLUSTER_PATH, serverIdentifier);
		
		final ThreadFactory t       readFactory = new PinpointThreadFactory(this.g        Class().getSimpleNa       e(), true);
		this.workerThread = threadF       ctory          newThread(this);
	}

	public void start() {             		switch (this.workerState.getCurrentState()) {
		case NEW:
			if (             his.workerState.changeStat             Initializing()) {
             			logger.info("{} initialization started.", this.getClass().getSimple                            ame());
	          		workerState.changeStateStarted();

				this.workerThread.start();                 			logger          info("{} initialization completed.", this.getClass().getSimple                 me());

				          reak;
			}
		case INITIALIZING:
			logger.info("{        already           nitializing.", this.getClass().getSimpleName()       ;
			break;
		c          se STARTED:
			logger.info("{} already start             d.", this.getClas       ().getSimpleName());
			break;
		case DESTROYI          G:
			throw new IllegalStateException("Already des          roying.");
		case STOPPED:
			throw new IllegalStateException("Already stop          e             .");
		case ILLEGAL_STATE:
			throw new IllegalStateException("Inv       lid State.");
		}
	}

	p       blic void stop() {
		if (!(this.w          rkerState.changeStateDes                      roying())) {
			Worke          State state = this.workerSta             e.getCurren                      State();

			logger.info("{}        lready {}.", this.getClass().getSimpleName(), state.toString());
			r        urn;


		logger.info("{        destorying start    d.", this.getClass().getSimpleName());
		boolean i    terrupted = false;
		while (this.workerThread.isAlive()) {
			this.w       rkerThread.interrupt();
			tr           {
				this.workerThread.join(100L)
			} catch (InterruptedE                               ceptio           e) {
				interrupted = true;
			}
		}

		this.workerState.ch          ngeStateStopp             d();
		logger.info("{} destorying c             mpleted.", this.getClass().getSimpleName());
	}

	@Override
	public              oid run() {

		// Thing                 to consider
	    // spinlock possible wh                n events are not delete
	    //                                                                may lead to Cha                               nelContext l                   ak when events are                left unresolved
		while (wor                   erState.isStarted(                                              ) {
			boolean eventCreated = await(60000, 200);
			if (!workerState.isSt             rted()) {
				break;
			}

		             // han                le events
			// chec                 and han                                                 le Channe                   Context leak if events are not triggered
			if (eventCreated) {
				// to av                                        id ConcurrentModificationException
				Iter                tor<ChannelContext> keyIterator = getLatestJobRepositoryKeyIterator();

			                   while (keyIterator.hasNext()) {
					ChannelContext channelCo                   text = keyIterator.next();                                                 					Job job = getJob(channelContext)        					if (job == null) {
						continue;
		       		}
					
					logger.info("Worker execute job({}).",       job);
					
					if (job instanceof UpdateJob) {
						handleUpdate((U       dateJob) job);
					} else if (job instanceof Del          teJob) {
						handleDelete((Del          teJob)                        b);
					}
				}
			} else {
			    // take care of lea          ed jobs - jobs may leak due to timing mism             tch while deleting jobs
				logger.debug("LeakDetecto                          Start.");

				while (true) {
					Job job = leakJobQueue.poll             );
					if (job == null) {
						break;
					}

					if (jo           i             stanceof UpdateJob) {
						putRetryJo                         (new UpdateJob(job.getChannelContext(), 1, ((UpdateJ             b) job).getContents()));
					}
				}

				for (ChannelContext channe                   Co       text : channelConte          tRepository) {
					if (P          npointServerSocketStateCode.is             inished(c                      anne        ontext.getCurrentStateCode())) {
			       		logger.info("LeakDetector Find Leak ChannelContext=       }          ", channelContext);
						putJob(new Delet             Job(channelContext));
					}
				}

			}
		}

		logge                         .info("{} stopped", this.getClass().getSimpleNam             ());
	}

	public boolean handleUpdate(UpdateJob job) {
		ChannelContext cha                         nelContext = job.getChannelContext();

		PinpointSer                   erSocketStateCode code = channelContex          .getCu       rentStateCode();
		          f (PinpointServerSocketSt          teCode.isFinished(code)) {
			             utJob(new                      Dele        Job(channelContext));
			retur                  alse;
		}

		try {
			String addContents = c       eateProfilerContent          (channelContext);

			if              zookeep        Client.exists(collectorUniqPath)) {
				byte[] contents = zoo       eeperClient.getData(collectorUniqPath);
				
				String dat              = addIfAbsentContents(new String(contents, cha    s    t), addContents);
				zookeeperClient.setData(collectorUniqPath, data.getBytes(    harset));
			} else {
				zookeeperClient.createPath(collectorUniqPath);
				    				// should return error even if NODE exists if    t    e data is important
				zookeeperClient.createNode(collectorUniqPat       , addContents.get          ytes(charset));
			}
			r          turn true;
		} catch (Exception e           {
			logger.warn(e.ge             Message(                   , e);
			if (e instan             eof Timeout                   xception) {
				putRetryJob(job);
			}
		}
		return false;
	}

	public boolean handleDelete(Job job) {
		ChannelContext channelContext = job.getChanne                            Context();

             	try {
			if (zookeeperClient.exists(collectorUniqPath)) {
				byte[] contents = zookeeperClient.getData(collectorUniqPath);
				
				String                               removeContents = createProfilerCo             tents                   cha             nelContext);
				String data = removeIfExistContents(new String(contents        charset), removeContents);
				
				zookeeperClient.setData(collec        rUniqPath, data.getBytes(charset));
			}
			channelContextRepository       remove(channelCon          ext);
			return true;
		} catch (Exceptio              e) {
			logger.warn(e.getMessage(), e    ;
			if (e instanceof TimeoutException) {
				put       etryJob(job);

		}

		return false;
	}

	public byte[] getClu          terDa             a() {
		try {
			return zo       keeperClient.getData(collectorUniqPath);
		} catch (       xception e) {
			logger.warn(e.getMessage()                              );
		}

		re          urn null;
	}

	public List<ChannelContext> getR          gisteredChannelContextList() {
		return          new ArrayLi                t<ChannelContext>(channelConte       tRepository);
	}
	
	/**
	 * Wa       ts for events to trigger for a given time.
	 * 
	 * @p          ram waitTimeMillis tota              time to wait for events to trigger in milli                   econds
	 * @          a                   am waitUnitTimeMillis time to wait for each wait       attempt in millis          conds
	 * @return true if event triggered, fals           otherwise
              */
	private boolean await(long waitTimeMillis, long waitUnitTim       Millis) {
		synchronized (lock) {
			long        aitTime = waitTime       illis;
			long waitUnitTime = wa          tUnitTimeMillis;
			if (w             itTimeMillis < 1000) {       				waitTime = 1000;

			if (waitUnitTimeMillis < 100) {
				waitUnitTime = 100;
			}

			l       ng startTimeMillis = System.currentTimeMillis();

			while (latestJobRep       sitory.size() == 0 && !isOverWaitTime(waitTime, startTimeMillis) && workerState.isStarted()) {
				try {
					lock.wai       (waitUnitTime);
				} catch (InterruptedException ignore) {
//                    Thread.currentThread       ).interrupt();
//                    TODO check Interrupted state
				}
			}

			if (isOverWaitTime(waitTime, sta             tTimeMillis)) {
				return false;
			}

			return true;
		}
	}

	private boolean isOverWaitTime(long waitTimeMillis, lo          g startTimeMillis) {
		return waitTimeMillis < (System.currentTimeMillis() - startTimeMillis);
	}

	private Iter          tor<Cha                   nel          ontext> getLatestJobRepositoryKeyIterator() {
		synchronized (lock)       {
			return latestJobRepository.keySet().iterator             );
		}
	}

	// must be invoked within a Runnable only
	private Job ge       Job(ChannelContext channelContext) {
		synchronized (lock) {
			Job job = latestJobRepository.remove(channelContext);
       		return job;
		}
	}

	public void putJob(Job job) {
		ChannelContext channelContext = job.getChannelC       ntext();
		if (!checkRequiredProperties(channelContext)) {
			return;
		}
		
		synchronized (lock) {
			channelCo             textRepository.addIfAbsent(channelContext);
			latestJobRepository.put(channelContext, job);
			lock.notifyAll();
		}

	
	private void putRetryJob(Job job) {
		job.incrementCurrentRetryCount();

		if (job.getMaxRetryCount() < job.          etCurrentRetryCount                   )) {
			if (logger.isInfoEnabled       )) {
				logger.warn("Lea        Job Queue Register Job={}.",       job);
			}
			leakJobQueu       .add(job);
			return;
		}
		
		Channe             Context channelContext = job        etChannelContext();

		synchronized (lock) {
			latestJobRepository.put       fAbsent(channelContext, job);
			lock.notifyAll();
		}             	}

	private String bindingPathAn          Znode(String path, String znodeName) {
		StringBuilder full             ath = ne                             StringBuilder();

		fullPath.append(p          th);
		if (!path.endsWith(PATH_SEPRATOR)) {
			fullPath.append(PATH_SEPRATO       );
		}
		fullPath.append(znodeName);

		return fullPath.toStr             ng();
	}

	private boolean checkRequiredProperties(C       annelContext channelContext) {
		Map<Object, Object> agentProperties             = channelContext.getChannel          roperties();
		final String applicati                   nName = MapUtils.getString(a             e                            tProperties, AgentHandshakePropertyType.APPLICATION_NA             E.getName());
		final Str             ng agentId = MapUtils.g                tString(agentProperties, Agen                                  HandshakeProper       yType.AGENT_ID.getName());
		final Long startTimeStampe = MapUtils.getLong(agentProperties, AgentHandshakePropertyType.START_TIMESTAMP.getName());
		
		if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId) || startTimeStampe == null || startTimeStampe <= 0) {
			logger.warn("ApplicationName({}) and AgnetId({}) and startTimeStampe({}) may not be null.", applicationName, agentId);
			return false;
		}
		
		return true;
	}
	
	private String createProfilerContents(ChannelContext channelContext) {
		StringBuilder profilerContents = new StringBuilder();
		
		Map<Object, Object> agentProperties = channelContext.getChannelProperties();
		final String applicationName = MapUtils.getString(agentProperties, AgentHandshakePropertyType.APPLICATION_NAME.getName());
		final String agentId = MapUtils.getString(agentProperties, AgentHandshakePropertyType.AGENT_ID.getName());
		final Long startTimeStampe = MapUtils.getLong(agentProperties, AgentHandshakePropertyType.START_TIMESTAMP.getName());
		
		if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId) || startTimeStampe == null || startTimeStampe <= 0) {
			logger.warn("ApplicationName({}) and AgnetId({}) and startTimeStampe({}) may not be null.", applicationName, agentId);
			return StringUtils.EMPTY;
		}
		
		profilerContents.append(applicationName);
		profilerContents.append(":");
		profilerContents.append(agentId);
		profilerContents.append(":");
		profilerContents.append(startTimeStampe);
		
		return profilerContents.toString();
	}

	private String addIfAbsentContents(String contents, String addContents) {
		String[] allContents = contents.split(PROFILER_SEPERATOR);
		
		for (String eachContent : allContents) {
			if (StringUtils.equals(eachContent.trim(), addContents.trim())) {
				return contents;
			}
		}
		
		return contents + PROFILER_SEPERATOR + addContents;
	}
	
	private String removeIfExistContents(String contents, String removeContents) {
		StringBuilder newContents = new StringBuilder(contents.length());
		
		String[] allContents = contents.split(PROFILER_SEPERATOR);

		Iterator<String> stringIterator = Arrays.asList(allContents).iterator();
		
		while (stringIterator.hasNext()) {
			String eachContent = stringIterator.next();
			
			if (StringUtils.isBlank(eachContent)) {
				continue;
			}
			
			if (!StringUtils.equals(eachContent.trim(), removeContents.trim())) {
				newContents.append(eachContent);

				if (stringIterator.hasNext()) {
					newContents.append(PROFILER_SEPERATOR);
				}
			}
		}

		return newContents.toString();
	}
	
}
