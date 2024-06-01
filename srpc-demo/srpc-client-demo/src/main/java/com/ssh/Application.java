package com.ssh;

import com.ssh.bootstrap.ReferenceConfig;
import com.ssh.bootstrap.RegistryConfig;
import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.interfaces.HelloService;

public class Application {
    public static void main(String[] args) {
        ReferenceConfig<HelloService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterfaceConsumed(HelloService.class);

        SRPCBootstrap.getInstance()
                .application("first-dubbo-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(referenceConfig);

        HelloService service = referenceConfig.getReference();
        String message = service.sayHello("sung");
        System.out.println("Receive result ======> " + message);


    }
}
