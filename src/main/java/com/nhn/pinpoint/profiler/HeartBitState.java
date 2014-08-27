package com.nhn.pinpoint.profiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum HeartBitState {

	// 상태를 잘게 나누는게 의미가 없음 (reconnect등 필요없음)
	// NONE, NEED_REQUEST, NEED_NOT_REQUEST로 나눔 
	// 성능이나 메모리적으로 문제가 되면 Constant나 static으로 빼는게 좋을듯 
	NONE {

		@Override
		public List<HeartBitState> getChangeAvailableStateList() {
			List<HeartBitState> avaiableStateList = new ArrayList<HeartBitState>();
			avaiableStateList.add(NEED_REQUEST);
			avaiableStateList.add(NEED_NOT_REQUEST);
			return avaiableStateList;
		}
		
	}, 
	
	NEED_REQUEST {

		@Override
		public List<HeartBitState> getChangeAvailableStateList() {
			List<HeartBitState> avaiableStateList = new ArrayList<HeartBitState>();
			avaiableStateList.add(NEED_REQUEST);
			avaiableStateList.add(NEED_NOT_REQUEST);
			avaiableStateList.add(FINISH);
			return avaiableStateList;
		}
		
	},
	
	NEED_NOT_REQUEST {

		@Override
		public List<HeartBitState> getChangeAvailableStateList() {
			List<HeartBitState> avaiableStateList = new ArrayList<HeartBitState>();
			avaiableStateList.add(NEED_REQUEST);
			avaiableStateList.add(NEED_NOT_REQUEST);
			avaiableStateList.add(FINISH);
			return avaiableStateList;
		}
		
	}, 
	
	FINISH {

		@Override
		public List<HeartBitState> getChangeAvailableStateList() {
			return Collections.EMPTY_LIST;
		}
		
	};
	
	public abstract List<HeartBitState> getChangeAvailableStateList();
}
