package com.nhn.pinpoint.profiler.sender;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class RetryQueueTest {
    @Test
    public void size() {

        RetryQueue retryQueue = new RetryQueue(1, 1);
        retryQueue.add(new RetryMessage(0, new byte[0]));
        retryQueue.add(new RetryMessage(0, new byte[0]));

        Assert.assertEquals(1, retryQueue.size());

    }

    @Test
    public void size2() {

        RetryQueue retryQueue = new RetryQueue(1, 1);
        RetryMessage retryMessage = retryQueue.get();
        Assert.assertNull(retryMessage);
    }

    @Test
    public void maxRetryTest() {

        RetryQueue retryQueue = new RetryQueue(3, 2);
        RetryMessage retryMessage = new RetryMessage(0, new byte[0]);
        retryMessage.fail();
        retryMessage.fail();


        retryQueue.add(retryMessage);
        retryQueue.add(retryMessage);

        Assert.assertEquals(retryQueue.size(), 0);
    }

    @Test
    public void add() {

        RetryQueue retryQueue = new RetryQueue(3, 2);
        retryQueue.add(new RetryMessage(0, new byte[0]));
        // 하나 넣고.실패한 메시지 넣으면 반이상이라서 버려야됨.
        RetryMessage retryMessage = new RetryMessage(0, new byte[0]);
        retryMessage.fail();
        retryQueue.add(retryMessage);

        Assert.assertEquals(retryQueue.size(), 1);
    }
}
