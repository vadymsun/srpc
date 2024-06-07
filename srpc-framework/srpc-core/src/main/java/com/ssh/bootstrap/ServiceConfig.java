package com.ssh.bootstrap;

/**
 *
 */
public class ServiceConfig {

    private Class<?> interfaceProvider;

    private Object reference;

    public void setInterface(Class<?> serviceClass) {
        interfaceProvider = serviceClass;
    }

    public void setRef(Object serviceImp) {
        reference = serviceImp;
    }

    public String getInterfaceName(){
        return interfaceProvider.getName();
    }

    public Object getReference(){
        return reference;
    }
}
