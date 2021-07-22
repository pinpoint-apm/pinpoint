package com.navercorp.pinpoint.web.scatter;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.junit.Assert;
import org.junit.Test;


public class ElpasedTimeDotPredicateTest {

    @Test
    public void newDragAreaDotPredicate() {
        ElpasedTimeDotPredicate dragAreaDotPredicate = new ElpasedTimeDotPredicate(100, 0);
        Assert.assertTrue(dragAreaDotPredicate.test(newDot(0)));
        Assert.assertTrue(dragAreaDotPredicate.test(newDot(1)));
        Assert.assertTrue(dragAreaDotPredicate.test(newDot(100)));

        Assert.assertFalse(dragAreaDotPredicate.test(newDot(200)));
        Assert.assertFalse(dragAreaDotPredicate.test(newDot(-1)));
    }

    private Dot newDot(int elapsedTime) {
        TransactionId transactionId = new TransactionId("agent", 0, 1);
        return new Dot(transactionId, 0, elapsedTime, 0, "agentId");
    }
}