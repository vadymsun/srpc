package com.ssh.bootstrap;


import com.ssh.anotation.SrpcService;
import com.ssh.loadbalance.LoadBalancer;
import com.ssh.loadbalance.imp.RandomLoadBalancer;
import com.ssh.loadbalance.imp.RoundLoadBalancer;
import com.ssh.network.handler.SrpcRequestMessageHandler;
import com.ssh.network.protocol.SrpcFrameDecoder;
import com.ssh.network.protocol.SrpcMessageCodec;
import com.ssh.network.serialize.SerializerFactory;
import com.ssh.proxy.handler.SrpcConsumerInvocationHandler;
import com.ssh.registry.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SRPCBootstrap {
    public static int PORT = 8081;
    public static String CUR_IP = "127.0.0.1";

    // 单例模式 每个服务只允许拥有一个启动器实例
    public static SRPCBootstrap srpcBootstrap = new SRPCBootstrap();

    // 服务名称
    private String appName;


    private ProtocolConfig protocolConfig;

    @Getter
    private Registry registry;

    @Getter
    private LoadBalancer loadBalancer;

    @Getter
    private int serializerType = SerializerFactory.JDK_SERIALIZER;


    // 服务端本地维护的 全类名与对象之间的映射
    public static final Map<String, ServiceConfig> serverMap = new ConcurrentHashMap<>();


    // 客户端维护的 服务地址和channel的映射
    public static final Map<String, Channel> channels = new ConcurrentHashMap<>();

    public static final Map<Long, CompletableFuture<Object>> waitingCalls = new ConcurrentHashMap<>();

    private SRPCBootstrap(){

    }

    public static SRPCBootstrap getInstance(){
        return srpcBootstrap;
    }

    /**
     * 配置服务提供者的名称
     * @param appName
     * @return
     */
    public SRPCBootstrap application(String appName) {
        this.appName = appName;
        return srpcBootstrap;
    }
    public SRPCBootstrap serialize(int serializerType) {
        this.serializerType = serializerType;
        return srpcBootstrap;
    }

    /**
     * j连接zookeeper
     * @param registryConfig
     * @return
     */
    public SRPCBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        loadBalancer = new RoundLoadBalancer();
        return srpcBootstrap;
    }

    /**
     * 协议名称
     * @param protocolConfig
     * @return
     */
    public SRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        return srpcBootstrap;
    }

    /**
     * 注册服务 - 在zookeeper中创建节点
     * @param service
     * @return
     */
    private SRPCBootstrap service(ServiceConfig service) {
        registry.publish(service);
        serverMap.put(service.getInterfaceName(), service);
        return srpcBootstrap;
    }




    /**
     * 启动netty 等待服务调用方的请求
     */
    public void start() {
        // 扫描包，获取包下所有类文件的全限定名
        String packageName = "com.ssh.Imp";
        List<String> classNames = getAllClass(packageName);

        // 实例化所有带有注解标记的类
        List<Class<?>> classes = new ArrayList<>();
        for (String className : classNames){
            try {
                Class<?> clazz = Class.forName(className);
                if(clazz.getAnnotation(SrpcService.class) != null){
                    classes.add(clazz);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        // 发布
        for (Class<?> clazz : classes){
            // 获取该类实现的所有接口
            Class<?>[] interfaces = clazz.getInterfaces();
            // 发布
            for (Class<?> anInterface : interfaces){
                ServiceConfig serviceConfig = new ServiceConfig();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(clazz);
                service(serviceConfig);
                log.debug("成功发布服务{}", anInterface);
            }
        }


        // 启动netty服务
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup(10);
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new SrpcFrameDecoder());
                        socketChannel.pipeline().addLast(new LoggingHandler());
                        socketChannel.pipeline().addLast(new SrpcMessageCodec());
                        socketChannel.pipeline().addLast(new SrpcRequestMessageHandler());
                    }

                });

        try {
            Channel channel = serverBootstrap.bind(PORT).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            boss.shutdownGracefully();
            workers.shutdownGracefully();
        }


    }


    private List<String> getAllClass(String packageName) {

        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        System.out.println(url);
        if(url == null){
            throw new RuntimeException("配置的包路径不存在！");
        }
        String absolutePath = url.getPath();
        System.out.println(absolutePath);
        ArrayList<String> classNames = new ArrayList<>();
        // 递归获取所有的类
        recursionFile(absolutePath, classNames, basePath);
        return classNames;

    }

    private void recursionFile(String absolutePath, ArrayList<String> classNames, String basePath) {
        File file = new File(absolutePath);
        if(file.isDirectory()){
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null){
                return;
            }
            for(File child : children){
                recursionFile(child.getAbsolutePath(), classNames, basePath);
            }
        }else {
            classNames.add(getClassNameByAbsolutePath(absolutePath, basePath));
        }
    }
    private String getClassNameByAbsolutePath(String absolutePath,String basePath) {
        // E:\project\ydlclass-yrpc\yrpc-framework\yrpc-core\target\classes\com\ydlclass\serialize\Serializer.class
        // com\ydlclass\serialize\Serializer.class --> com.ydlclass.serialize.Serializer
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        System.out.println(fileName);
        return fileName;
    }



    /**
     * 调用者获取目标接口的代理类
     *
     * @param referenceConfig
     */
    public void reference(ReferenceConfig<?> referenceConfig) {
        ClassLoader classLoader = referenceConfig.getInterfaceConsumed().getClassLoader();
        Class<?>[] interfaces = new Class[]{referenceConfig.getInterfaceConsumed()};
        SrpcConsumerInvocationHandler srpcConsumerInvocationHandler = new SrpcConsumerInvocationHandler(registry, referenceConfig);
        // 生成目标接口的代理类
        Object reference = Proxy.newProxyInstance(classLoader, interfaces, srpcConsumerInvocationHandler);
        referenceConfig.setReference(reference);

    }




}
