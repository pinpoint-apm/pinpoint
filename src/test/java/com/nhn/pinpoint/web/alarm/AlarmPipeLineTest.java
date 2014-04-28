package com.nhn.pinpoint.web.alarm;

import org.junit.Assert;
import org.junit.Test;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmSendFilter;

public class AlarmPipeLineTest {

	@Test
	public void normalTest1() {
		AlarmPipeLine pipeLine = new DefaultAlarmPipeLine();

		AlarmFilter checkFilter = new SuccessCheckFilter();
		SendFilter sendFilter = new SendFilter();

		pipeLine.addLast(checkFilter);
		pipeLine.addLast(sendFilter);

		pipeLine.execute(null);

		Assert.assertTrue(sendFilter.isDoSend());
	}
	
	@Test
	public void normalTest2() {
		AlarmPipeLine pipeLine = new DefaultAlarmPipeLine();

		AlarmFilter checkFilter = new FailCheckFilter();
		SendFilter sendFilter = new SendFilter();

		pipeLine.addLast(checkFilter);
		pipeLine.addLast(sendFilter);

		pipeLine.execute(null);

		Assert.assertFalse(sendFilter.isDoSend());
	}
	
	@Test
	public void normalTest3() {
		AlarmPipeLine pipeLine = new DefaultAlarmPipeLine();

		AlarmFilter checkFilter1 = new SuccessCheckFilter();
		AlarmFilter checkFilter2 = new FailCheckFilter();
		SendFilter sendFilter = new SendFilter();

		pipeLine.addLast(checkFilter1);
		pipeLine.addLast(checkFilter2);
		
		pipeLine.addLast(sendFilter);

		pipeLine.execute(null);

		Assert.assertFalse(sendFilter.isDoSend());
	}
	
	@Test
	public void normalTest4() {
		AlarmPipeLine pipeLine = new DefaultAlarmPipeLine();

		AlarmFilter checkFilter1 = new SuccessCheckFilter();
		AlarmFilter checkFilter2 = new FailCheckFilter();
		SendFilter sendFilter = new SendFilter();

		pipeLine.addLast(sendFilter);
		pipeLine.addLast(checkFilter1);
		pipeLine.addLast(checkFilter2);
		

		pipeLine.execute(null);

		Assert.assertFalse(sendFilter.isDoSend());
	}

	class SuccessCheckFilter extends AlarmCheckFilter {
		@Override
		protected boolean check(AlarmEvent event) {
			return true;
		}
	}

	class FailCheckFilter extends AlarmCheckFilter {
		@Override
		protected boolean check(AlarmEvent event) {
			return false;
		}
	}

	class SendFilter extends AlarmSendFilter {

		private boolean doSend = false;

		public SendFilter() {
		}

		@Override
		protected boolean send(AlarmEvent event) {
			doSend = true;
			return true;
		}

		public boolean isDoSend() {
			return doSend;
		}

	}

}
