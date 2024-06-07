package com.ssh.Imp;

import com.ssh.anotation.SrpcService;
import com.ssh.interfaces.HelloService;

@SrpcService
public class HelloServiceImp implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
