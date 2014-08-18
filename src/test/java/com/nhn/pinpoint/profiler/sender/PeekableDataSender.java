package com.nhn.pinpoint.profiler.sender;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.thrift.TBase;

/**
 * @author hyungil.jeong
 */
public class PeekableDataSender<T extends TBase<?, ?>> implements DataSender, Iterable<T> {

    private final Queue<T> queue = new ConcurrentLinkedQueue<T>();

    public T peek() {
        return this.queue.peek();
    }

    public T poll() {
        return this.queue.poll();
    }

    public int size() {
        return this.queue.size();
    }

    public void clear() {
        this.queue.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return this.queue.iterator();
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        // deepCopy 안 함. 실제로도 네트워크 전송이 늦게되면 다른 datasender 들도 
        // send 할 객체들의 레퍼런스를 그대로 가지고 있기 때문.
        @SuppressWarnings("unchecked")
        T dataToAdd = (T)data;
        return this.queue.offer(dataToAdd);
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isNetworkAvailable() {
        return false;
    }

    @Override
    public String toString() {
        return this.queue.toString();
    }

}
