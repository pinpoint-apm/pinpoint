package com.navercorp.pinpoint.web.calltree.span;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

public class SpanAlignDepthTest {

    
    @Test
    public void test() {
        // sync
        //   async
        // sync
        List<SpanEventBo> list = new ArrayList<SpanEventBo>();
        list.add(makeSpanEventBo(false, (short)0, 1));
        list.add(makeSpanEventBo(true, (short)0, 1));
        list.add(makeSpanEventBo(false, (short)1, -1));
        printDepth(list);

        
        // sync
        //   async
        //     async
        //       async
        list.clear();
        list.add(makeSpanEventBo(false, (short)0, 1));
        list.add(makeSpanEventBo(true, (short)0, 1));
        list.add(makeSpanEventBo(true, (short)1, 2));
        list.add(makeSpanEventBo(true, (short)0, 1));
        printDepth(list);

        
        // sync
        //   async
        //     async
        // sync
        list.clear();
        list.add(makeSpanEventBo(false, (short)0, 1));
        list.add(makeSpanEventBo(true, (short)0, 1));
        list.add(makeSpanEventBo(true, (short)0, 1));
        list.add(makeSpanEventBo(false, (short)1, -1));
        printDepth(list);
    }
    
    
    private void printDepth(List<SpanEventBo> list) {
        SpanAlignDepth depth = new SpanAlignDepth(0);
        for(SpanEventBo event : list) {
            int currentDepth = depth.getDepth(event);
            for(int i = 0; i < currentDepth; i++) {
                System.out.print("#");
            }
            System.out.println(currentDepth);
        }
    }
    
    

    private SpanEventBo makeSpanEventBo(final boolean async, final short sequence, final int depth) {
        SpanEventBo event = new SpanEventBo();
        event.setAsyncId(async ? 1 : -1);
        event.setSequence(sequence);
        event.setDepth(depth);

        return event;
    }
}