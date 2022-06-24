package com.navercorp.pinpoint.web.scatter;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ElpasedTimeDotPredicateTest {

    @Test
    public void newDragAreaDotPredicate() {
        ElpasedTimeDotPredicate dragAreaDotPredicate = new ElpasedTimeDotPredicate(100, 0);
        Assertions.assertTrue(dragAreaDotPredicate.test(newDot(0)));
        Assertions.assertTrue(dragAreaDotPredicate.test(newDot(1)));
        Assertions.assertTrue(dragAreaDotPredicate.test(newDot(100)));

        Assertions.assertFalse(dragAreaDotPredicate.test(newDot(200)));
        Assertions.assertFalse(dragAreaDotPredicate.test(newDot(-1)));
    }

    private Dot newDot(int elapsedTime) {
        TransactionId transactionId = new TransactionId("agent", 0, 1);
        return new Dot(transactionId, 0, elapsedTime, 0, "agentId");
    }
}