package com.ssh;

import com.ssh.bootstrap.ReferenceConfig;
import com.ssh.registry.RegistryFactory;
import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.interfaces.HelloService;

public class Application {
    public static void main(String[] args) {
        ReferenceConfig<HelloService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterfaceConsumed(HelloService.class);

        SRPCBootstrap.getInstance()
                .application("first-dubbo-consumer")
                .registry(new RegistryFactory("zookeeper://127.0.0.1:2181"))
                .reference(referenceConfig);

        HelloService service = referenceConfig.getReference();
        for (int i = 0; i < 5; i++) {
            String message = service.sayHello("孙守荟"+"序号"+i);
            System.out.println("Receive result ======> " + message);

        }


    }
}
