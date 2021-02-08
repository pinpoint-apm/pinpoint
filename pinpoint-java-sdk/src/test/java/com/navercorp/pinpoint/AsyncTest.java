package com.navercorp.pinpoint;

import com.navercorp.pinpoint.sdk.concurrent.AsyncExecutors;
import com.navercorp.pinpoint.sdk.concurrent.AsyncRun;

import java.io.IOException;
import java.util.concurrent.*;

public class AsyncTest {

    ExecutorService executorService = AsyncExecutors.newFixedThreadPool(8);


    public static void main(String[] args) {

        AsyncTest asyncTest = new AsyncTest();

//        asyncTest.test1();

//        asyncTest.test2();

        asyncTest.test3();



    }


    public void test3(){
        ScheduledExecutorService scheduledExecutorService = AsyncExecutors.newScheduledThreadPool(1);

//        scheduledExecutorService.schedule(()->System.out.println("hello world"),5,TimeUnit.SECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("hello world");
            }
        },1,5,TimeUnit.SECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("hello world2.......");
            }
        },1,2,TimeUnit.SECONDS);

    }

    public void test2(){



        ExecutorService executorService = AsyncExecutors.newFixedThreadPool(8);
        executorService.submit(new AsyncRun() {
            @Override
            public Object asyncRun() {
                int count = count();
                System.out.println("count:" + count);
                return count;
            }
        }.getCallable());
        Object o = null;
//        try {
//            o = submit.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        System.out.println("run......:"+o);
        try {
            int read = System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void test1(){


        executorService.execute(new AsyncRun() {
            @Override
            public Object asyncRun() {
                int count = count();
                System.out.println("count:" + count);
                return count;
            }
        }.getRunnable());


//        AsyncRun re = ()->count();

        System.out.println("run......");
        try {
            int read = System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }



        executorService.execute(new Runnable() {
            @Override
            public void run() {
                count();
            }
        });


        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("hello world");
            }
        };
    }



    static int count() {
        int i = 0;
        for (; i < 5; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("" + i);
        }
        return i;
    }
}
