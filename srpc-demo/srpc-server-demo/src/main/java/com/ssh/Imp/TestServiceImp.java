package com.ssh.Imp;

import com.ssh.anotation.SrpcService;
import com.ssh.interfaces.TestService;

@SrpcService
public class TestServiceImp implements TestService {

    @Override
    public int sum(int a, int b) {
        return  a + b;
    }
}
