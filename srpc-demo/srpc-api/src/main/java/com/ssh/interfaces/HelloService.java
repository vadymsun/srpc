package com.ssh.interfaces;

import com.ssh.anotation.Retry;

public interface HelloService {
     @Retry
     String sayHello(String name);
}
