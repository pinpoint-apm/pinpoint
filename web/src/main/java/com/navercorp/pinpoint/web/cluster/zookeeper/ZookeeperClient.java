package com.nhn.pinpoint.web.cluster.zookeeper;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.cluster.zookeeper.exception.AuthException;
import com.nhn.pinpoint.web.cluster.zookeeper.exception.BadOperationException;
import com.nhn.pinpoint.web.cluster.zookeeper.exception.ConnectionException;
import com.nhn.pinpoint.web.cluster.zookeeper.exception.PinpointZookeeperException;
import com.nhn.pinpoint.web.cluster.zookeeper.exception.TimeoutException;
import com.nhn.pinpoint.web.cluster.zookeeper.exception.UnknownException;

/**
 * @author koo.taejin <kr14910>
 */
public class ZookeeperClient {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 쥬키퍼 클라이언트는 스레드 세이프함
	private final ZookeeperClusterManager manager;

	private final ZooKeeper zookeeper;
	private final AtomicBoolean clientState = new AtomicBoolean(true);

	// 데이터를 이친구가 다가지고 있어야 할 거 같은데;
	public ZookeeperClient(String hostPort, int sessionTimeout, ZookeeperClusterManager manager) throws KeeperException, IOException, InterruptedException {
		this.manager = manager;
		zookeeper = new ZooKeeper(hostPort, sessionTimeout, this.manager); // server
	}
	
	/**
	 * path의 가장마지막에 있는 node는 생성하지 않는다. 
	 * 
	 * @throws PinpointZookeeperException 
	 * @throws InterruptedException 
	 */
	public void createPath(String path) throws PinpointZookeeperException, InterruptedException {
		checkState();

		int pos = 1;
		do {
			pos = path.indexOf('/', pos + 1);

			if (pos == -1) {
				pos = path.length();
				return;
			}

			try {
				String subPath = path.substring(0, pos);
				if (zookeeper.exists(subPath, false) != null) {
					continue;
				}

				zookeeper.create(subPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} catch (KeeperException exception) {
				if (exception.code() != Code.NODEEXISTS) {
					handleException(exception);
				} 
			}

		} while (pos < path.length());
	}

	// 정확히 동일한 노드가 생성되어 있는지 확인하려면
	// 내부의 컨텐츠 검사도 해야됨
	public String createNode(String znodePath, byte[] data, CreateMode createMode) throws PinpointZookeeperException, InterruptedException {
		checkState();

		try {
			if (zookeeper.exists(znodePath, false) != null) {
				return znodePath;
			}

			String pathName = zookeeper.create(znodePath, data, Ids.OPEN_ACL_UNSAFE, createMode);
			return pathName;
		} catch (KeeperException exception) {
			if (exception.code() != Code.NODEEXISTS) {
				handleException(exception);
			} 
		}
		return znodePath;
	}
	
	public void delete(String path) throws PinpointZookeeperException, InterruptedException {
		checkState();

		try {
			zookeeper.delete(path, -1);
		} catch (KeeperException exception) {
			if (exception.code() != Code.NONODE) {
				handleException(exception);
			} 
		}
	}

	public boolean exists(String path) throws PinpointZookeeperException, InterruptedException {
		checkState();

		try {
			Stat stat = zookeeper.exists(path, false);
			if (stat == null) {
				return false;
			}
		} catch (KeeperException exception) {
			if (exception.code() != Code.NODEEXISTS) {
				handleException(exception);
			} 
		}
		return true;
	}

	private void checkState() throws PinpointZookeeperException {
		if (!this.manager.isConnected() || !clientState.get()) {
			throw new ConnectionException("instance must be connected.");
		}
	}

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
			case NONODE:
				throw new BadOperationException(keeperException.getMessage(), keeperException);
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
					logger.debug(ignore.getMessage(), ignore);
				}
			}
		}
	}

}
