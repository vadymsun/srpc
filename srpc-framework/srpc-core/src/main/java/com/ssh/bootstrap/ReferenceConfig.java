package com.ssh.bootstrap;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReferenceConfig <T>{

    // 调用方调用的接口
    private Class<T> interfaceConsumed;

    // 动态代理生成的代理对象
    private T reference;
    public void setReference(Object object){
        reference = (T) object;
    }


}
