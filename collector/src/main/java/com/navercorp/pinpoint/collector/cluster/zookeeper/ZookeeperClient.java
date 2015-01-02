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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.AuthException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.BadOperationException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.ConnectionException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.NoNodeException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.TimeoutException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.UnknownException;

/**
 * @author koo.taejin
 */
public class ZookeeperClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	// Zookeeper clients are thread    safe
	private final ZooKeeper zo    keeper;
	private final AtomicBoolean clientState = new AtomicBoo       ean(true);
	
	private final ZookeeperEvent    atcher watcher;

	public ZookeeperClient(String hostPort, int sessionTimeout, ZookeeperEventWatcher watcher) throws KeeperException, IOException, In       erruptedException {       		this.watcher = watcher;
		zookeeper = new ZooKeeper(hostPort, sessionT              eout, this.watcher); // server
	}
	
	/**
	 * do     o     create the final node in the given     ath.
	 * 
	 * @throws Pinpoint    o    keeperException
	 * @throws InterruptedException 
	 */
	public void createPath(String path        throws PinpointZook        perException, InterruptedException {
		createPath(path, false);
	}

	public void createPath(String path, boolean c       eateEndNod       ) throws                 inpointZookeeperException, I          terruptedE             ception {
		                               heckState();

		                nt pos = 1;                                                                      		do {
             		pos = path.indexOf('/', pos + 1);

		                                        if (pos == -1) {
				pos = path.length();
			}

			try {
				if (pos =           path.length()) {
					if (!cre             teEndNode) {
						return;
					}
                			}
				
				Str                            ng subPath = path.        bstring(0, pos);
				if (zookeeper.exists(subPath, false) != null) {
					continue;
				}

				zookeeper.create       subPath, n       w          byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PE             SISTENT);                   			} catch (KeeperException exception) {
				if (exception.code() != Code.NODEEXISTS)
					hand       eException(exception);
				} 


		} while (pos < pa             h.length())
	}

	public String createNode(String znodePath, byte[] data) throws PinpointZookeeperExc       ption, Int       r          uptedException {
		checkState();

		tr        {
			if (zookeeper.exists(znodeP          th, false) != null) {                   				return znodePath;
			}

			String p        hName = zookeeper.create(znodePath, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			return pathN       me;
		} ca       c           (KeeperException exception) {
			hand                               eException(exception);

		return znodePath;
	}
	
	public          byte[] getData(String                path) throws PinpointZookeeperException, InterruptedException {
		checkState();

		t       y {
			ret       r           zookeeper.getData(pa       h, false, null);
		} catch (Keepe          Exception exception) {
			handle             xception(exception)
		}
		
		throw new UnknownException("UnknownException.");
	}

	public void setData(St       ing path,        y          e[] data) throws PinpointZookeeperEx          eption, Inter             upted                xception {
		checkState();

	          try {
			if (zookeeper.exists(path,              alse) == null) {
		                      	re        rn;
			}

			zookeeper.setData(path, data, -1);
		} catch (       eeperException ex          eption) {
			handleException(exception);
		}
	}
	
	publ             c void delete(String path)        hrows PinpointZookeeperException, InterruptedEx          eption
		          heckState();

		try {
			zookeeper.delete(path, -1);
		} catch (KeeperException exception) {
			if (exception.code() !        Code.NONO       E           {
				handleException(exception);
			} 
		}
	}

	public boolean                   exists(String path) throws PinpointZook          eperException,        nterruptedException {
		checkStat          ();

		try {
			Stat stat = zook             eper.exists(path, f                            lse);
			if (stat             null) {
				return false;
			}
		} catch (KeeperException exception) {
			if (ex       eption.code()        = Code.NODEEXISTS) {
				handleException(e    cept    on);
			} 
		}
		return true;
	}

	private void checkState() throws PinpointZookeeperException
		if (!isConn       cted()) {
			throw new ConnectionException("instance must be c       nnected.");
		}
	}

	public boolean isConnected(        {
		if (!watcher.i        onnected() || !clientState.get()) {
			return false;
		}
		
		return true;
	}
	
	public List<St       ing> getChildrenNode(String p          th, boolean wa          ch) throws Pin             ointZookeeperException, InterruptedException {
		checkState();

		try          {
			List<          tring> chi          dNodeL             st = zookeeper.getChildren(path, watch, null);
			
			logger.in          o("ChildNode          List = {}"           childNodeList);
			ret          rn child          odeList;
	             } catch (KeeperException exception) {
			if (exception.code() != Code.N          NODE)
				handleException(exception);
			}
		}
		
		return Collections.empty          ist();
	}
	
//	p             blic byte[] getData(String path) throws KeeperException, Interrupt          dE             ception {
//		checkState();
//
//		return zookeeper.getData(path,                    alse, null);
/       	}
//
//	public List<String> getChildrenN          de(String path) th                            ows Ke             perException, InterruptedExcept                on {
//		checkState();
//
//		List<String> childNodeList = zookeeper.getChildren(path, false);
//		logger.info("ChildNode List = {                               ", childNodeList);
//		return childNodeList;
//	}

	private void handleException(KeeperException keeperException) throws PinpointZookeeperException {
		switch (keeperException.code()) {
			case CONNECTIONLOSS:
			case SESSIONEXPIRED:
				throw new ConnectionException(keeperException.getMessage(), keeperException);
			case AUTHFAILED:
			case INVALIDACL:
			case NOAUTH:
				throw new AuthException(keeperException.getMessage(), keeperException);
			case BADARGUMENTS:
			case BADVERSION:
			case NOCHILDRENFOREPHEMERALS:
			case NOTEMPTY:
			case NODEEXISTS:
				throw new BadOperationException(keeperException.getMessage(), keeperException);
			case NONODE:
			    throw new NoNodeException(keeperException.getMessage(), keeperException);
			case OPERATIONTIMEOUT:
				throw new TimeoutException(keeperException.getMessage(), keeperException);
			default:
				throw new UnknownException(keeperException.getMessage(), keeperException);
			}
	}
	
	public void close() {
		if (clientState.compareAndSet(true, false)) {
			if (zookeeper != null) {
				try {
					zookeeper.close();
				} catch (InterruptedException ignore) {
					logger.info("Interrupted zookeeper.close(). Caused:" + ignore.getMessage(), ignore);
                    Thread.currentThread().interrupt();
				}
			}
		}
	}

}
