package com.navercorp.pinpoint.web.scatter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DragAreaTest {

    @Test
    public void normalize() {
        DragArea normalize = DragArea.normalize(2, 1, 20, 10);

        assertThat(normalize)
                .extracting(DragArea::getXHigh, DragArea::getXLow, DragArea::getYHigh, DragArea::getYLow)
                .containsExactly(2L, 1L, 20L, 10L);
    }
}