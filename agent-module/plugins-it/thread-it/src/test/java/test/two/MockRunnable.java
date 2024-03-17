package test.two;


public class MockRunnable implements Runnable {
    public MockRunnable() {
    }

    @Override
    public void run() {
        System.out.println("runnable-----------------");
    }
};
