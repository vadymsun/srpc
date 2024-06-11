package com.ssh.protection;

public class RestrictorFactory {

    public static Restrictor createRestrictor(int capacity, int rate){
        return new TokenBucketRestrictor(capacity, rate);
    }
    // todo  创建一个单独的应用来设置每个节点的限流配置
    public static Restrictor createRestrictor(){
        return new TokenBucketRestrictor(10, 10);
    }
}
