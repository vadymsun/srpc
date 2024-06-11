package com.ssh.protection;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TokenBucketRestrictor implements Restrictor{

    // 令牌桶中令牌的数量
    private int tokenCount;

    // 令牌桶容量
    private final int capacity;

    // 每秒增加的令牌数量
    private final int rate;

    // 上次添加令牌的时间
    private long lastTimeAdd;

    public TokenBucketRestrictor(int capacity, int rate){
        this.tokenCount = capacity;
        this.capacity = capacity;
        this.rate = rate;
        this.lastTimeAdd = System.currentTimeMillis();
    }

    /**
     * 判断是否放行
     * @return
     */
    public synchronized boolean isAllowed(){
        // 向桶中添加令牌
        long cur_time = System.currentTimeMillis();
        long time_pass = (cur_time - lastTimeAdd);
        // 如果能够至少放入一个令牌
        if(time_pass > 1000/rate){
            int add_count = (int)time_pass * rate / 1000;
            tokenCount = Math.min(capacity, tokenCount + add_count);
            lastTimeAdd = cur_time;
        }
        // 判断是否放行
        if(tokenCount > 0){
            tokenCount--;
            return true;
        }
        log.debug("收到请求过多，请求被拦截！");
        return false;
    }
    public static void main(String[] args) {
        TokenBucketRestrictor rateLimiter = new TokenBucketRestrictor(10,10);
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean allowRequest = rateLimiter.isAllowed();
            System.out.println("allowRequest = " + allowRequest);
        }
    }

}
