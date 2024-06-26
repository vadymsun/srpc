package com.ssh.proxy;

import java.util.concurrent.atomic.AtomicLong;

public class RequestIdGenerator {
    public static AtomicLong id  =new AtomicLong(0L);

    public static Long getRequestId(){
         return id.incrementAndGet();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(getRequestId());
                }
            }).start();
        }
    }


}
