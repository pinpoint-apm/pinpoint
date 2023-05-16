package com.navercorp.pinpoint.web.scatter;

import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.util.Objects;
import java.util.function.Predicate;

public class ElpasedTimeDotPredicate implements Predicate<Dot> {
    private final long high;
    private final long low;

    public static ElpasedTimeDotPredicate newDragAreaDotPredicate(DragArea dragArea) {
        Objects.requireNonNull(dragArea, "dragArea");
        return new ElpasedTimeDotPredicate(dragArea.getYHigh(), dragArea.getYLow());
    }

    public ElpasedTimeDotPredicate(long high, long low) {
        this.high = high;
        this.low = low;
    }

    @Override
    public boolean test(Dot dot) {
        final int elapsedTime = dot.getElapsedTime();
        if (elapsedTime <= high && elapsedTime >= low) {
            return true;
        }
        return false;
    }
}
