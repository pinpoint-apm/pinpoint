package com.navercorp.pinpoint.common.profiler;

public interface Stoppable {
    void stop();

   static void stopQuietly(Stoppable stoppable) {
       if (stoppable != null) {
           try {
               stoppable.stop();
           } catch (Exception ignore) {
           }
       }
   }
}
