package com.nhn.pinpoint.profiler;

import java.util.List;

/**
 * 
 * @author koo.taejin
 */
public class HeartBitStateContext {

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
		if (prevEventTimeMillis <= eventTimeMillis) {
			synchronized (this) {
				boolean isChange = changeState(this.state, HeartBitState.NEED_REQUEST);
				if (isChange) {
					prevEventTimeMillis = eventTimeMillis;
				}
				return isChange;
			}
		}
		return false;
	}
	
	boolean changeStateToNeedNotRequest(long eventTimeMillis) {
		if (prevEventTimeMillis < eventTimeMillis) {
			synchronized (this) {
				boolean isChange = changeState(this.state, HeartBitState.NEED_NOT_REQUEST);
				if (isChange) {
					prevEventTimeMillis = eventTimeMillis;
				}
				return isChange;
			}
		}
		return false;
	}

	boolean changeStateToFinish() {
		synchronized (this) {
			return changeState(this.state, HeartBitState.FINISH);
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
