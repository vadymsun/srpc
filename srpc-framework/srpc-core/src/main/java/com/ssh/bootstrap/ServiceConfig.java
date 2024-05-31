package com.ssh.bootstrap;

public class ServiceConfig <T>{

    private Class<T> interfaceProvider;

    private T reference;

    public void setInterface(Class<T> serviceClass) {
        interfaceProvider = serviceClass;
    }

    public void setRef(T serviceImp) {
        reference = serviceImp;
    }

    public String getInterfaceName(){
        return interfaceProvider.getName();
    }

    public T getReference(){
        return reference;
    }
}
