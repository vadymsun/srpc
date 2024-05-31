package com.ssh.Imp;

import com.ssh.interfaces.HelloService;

public class HelloServiceImp implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
