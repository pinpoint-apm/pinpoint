package com.navercorp.pinpoint.collector.receiver.grpc.service;

public interface StreamCloseOnError {
     StreamCloseOnError FALSE = new StreamCloseOnError() {
          @Override
          public boolean onError(Throwable throwable) {
               return false;
          }

          @Override
          public String toString() {
               return "StreamCloseOnError:false";
          }
     };

     StreamCloseOnError TRUE = new StreamCloseOnError() {
          @Override
          public boolean onError(Throwable throwable) {
               return true;
          }

          @Override
          public String toString() {
               return "StreamCloseOnError:true";
          }
     };

     boolean onError(Throwable throwable);
}
