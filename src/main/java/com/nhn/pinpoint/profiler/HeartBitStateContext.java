package com.nhn.pinpoint.profiler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author koo.taejin
 */
public class HeartBitStateContext {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 클래스로 감싸 두지 않음 
	private HeartBitState state = HeartBitState.NONE;
	private long prevEventTimeMillis;
	
	public HeartBitStateContext() {
		this.prevEventTimeMillis = System.currentTimeMillis();
	}
	
	boolean needRequest() {
		synchronized (this) {
			if (state == HeartBitState.NEED_REQUEST || state == HeartBitState.NONE) {
				return true;
			} else {
				return false;
			}
		}
	}

	// 메시지 성공시를 제외하고는 이걸로 변경하면 안됨
	boolean changeStateToNeedRequest(long eventTimeMillis) {
		logger.info("{} will change to NEED_REQUEST state.", this.getClass().getSimpleName());
		
		if (prevEventTimeMillis <= eventTimeMillis) {
			synchronized (this) {
				boolean isChange = changeState(this.state, HeartBitState.NEED_REQUEST);
				if (isChange) {
					prevEventTimeMillis = eventTimeMillis;
				}
				logger.info("{} change to NEED_REQUEST state ({}) .",this.getClass().getSimpleName(), isChange);
				return isChange;
			}
		}
		return false;
	}
	
	boolean changeStateToNeedNotRequest(long eventTimeMillis) {
		logger.info("{} will change to NEED_NOT_REQUEST state.", this.getClass().getSimpleName());

		if (prevEventTimeMillis < eventTimeMillis) {
			synchronized (this) {
				boolean isChange = changeState(this.state, HeartBitState.NEED_NOT_REQUEST);
				if (isChange) {
					prevEventTimeMillis = eventTimeMillis;
				}
				logger.info("{} change to NEED_NOT_REQUEST state ({}) .", this.getClass().getSimpleName(), isChange);
				return isChange;
			}
		}
		return false;
	}

	boolean changeStateToFinish() {
		logger.info("{} will change to FINISH state.", this.getClass().getSimpleName());
		
		synchronized (this) {
			boolean isChange =  changeState(this.state, HeartBitState.FINISH);
			logger.info("{} change to FINISH state ({}) .",this.getClass().getSimpleName(), isChange);
			return isChange;
		}
	}
	
	private boolean changeState(HeartBitState current, HeartBitState next) {
		synchronized (this) {
			List<HeartBitState> changeAvaialableStateList = current.getChangeAvailableStateList();

			if (changeAvaialableStateList.contains(next)) {
				return compareAndSet(current, next);
			} else {
				return false;
			}
		}
	}
	
	private boolean compareAndSet(HeartBitState current, HeartBitState next) {
		synchronized (this) {
			if (this.state == current) {
				this.state = next;
				return true;
			} else {
				return false;
			}
		}
	}

	public HeartBitState getState() {
		return state;
	}
	
}
