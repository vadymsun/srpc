package com.ssh.shutdown;

import java.util.concurrent.atomic.AtomicInteger;


public class RequestProcessingCounter {
    private final AtomicInteger count;

    public RequestProcessingCounter(){
        count = new AtomicInteger(0);
    }

    public RequestProcessingCounter(int count){
        this.count = new AtomicInteger(count);
    }

    public void increase(){
        count.incrementAndGet();
    }

    public void decrease(){
        count.decrementAndGet();
    }

    public int getCount(){
        return count.get();
    }
}
