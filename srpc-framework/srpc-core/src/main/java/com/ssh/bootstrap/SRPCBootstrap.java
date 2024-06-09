package com.ssh.bootstrap;


import com.ssh.anotation.SrpcService;
import com.ssh.config.Configuration;
import com.ssh.loadbalance.LoadBalancer;
import com.ssh.loadbalance.imp.RoundLoadBalancer;
import com.ssh.network.handler.SrpcRequestMessageHandler;
import com.ssh.network.protocol.SrpcFrameDecoder;
import com.ssh.network.protocol.SrpcMessageCodec;
import com.ssh.proxy.handler.SrpcConsumerInvocationHandler;
import com.ssh.registry.Registry;
import com.ssh.registry.RegistryFactory;
import com.ssh.util.FileUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SRPCBootstrap {



    // 服务端本地维护的 全类名与对象之间的映射
    public static final Map<String, Object> SERVER_MAP = new ConcurrentHashMap<>();

    // 客户端维护的 服务地址和channel的映射
    public static final Map<String, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();

    // 调用方维护的当前正在等待服务方响应的远程调用
    public static final Map<Long, CompletableFuture<Object>> WAITING_CALLS = new ConcurrentHashMap<>();


    // 注册中心
    @Getter
    private Registry registry;


    // 负载均衡器
    @Getter
    private LoadBalancer loadBalancer;

    // 配置信息
    @Getter
    private Configuration configuration = new Configuration();


    //============================================== 单例模式 ===========================================================
    private static final SRPCBootstrap srpcBootstrap = new SRPCBootstrap();
    private SRPCBootstrap(){}

    public static SRPCBootstrap getInstance(){
        return srpcBootstrap;
    }

    // =================================================================================================================


    //============================================== 服务方启动配置 =======================================================
    public SRPCBootstrap application(String appName) {
        configuration.setAppName(appName);
        return srpcBootstrap;
    }
    public SRPCBootstrap setSerializeType(int serializerType) {
        configuration.setSerializerType(serializerType);
        return srpcBootstrap;
    }

    public SRPCBootstrap registry(RegistryFactory registryFactory) {
        this.registry = registryFactory.getRegistry();
        loadBalancer = new RoundLoadBalancer();
        return srpcBootstrap;
    }

    public SRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        return srpcBootstrap;
    }
    // =================================================================================================================


    /**
     * 注册服务 - 在zookeeper中创建节点
     *
     * @param service
     */
//    private void registService(ServiceConfig service) {
//        registry.publish(service);
//        SERVER_MAP.put(service.getInterfaceProvider().getName(), service);
//    }


    /**
     * 启动netty 等待调用方的请求
     */
    public void start() {
        // 扫描包，获取包下所有类文件的全限定名
        String packageName = configuration.getScanPath();
        List<String> classNames = FileUtil.getAllClass(packageName);

        // 通过类名称获取类对象
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
        try {
            for (Class<?> clazz : classes) {
                Object object = clazz.newInstance();
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    // 发布到注册中心
                    registry.publish(anInterface.getName());
                    // 把实例化后的对象存入本地
                    SERVER_MAP.put(anInterface.getName(), object);
                    log.debug("成功发布服务{}", anInterface);
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // 启动netty服务
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup(configuration.getNettyWorkerNum());
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
            Channel channel = serverBootstrap.bind(configuration.getPort()).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            boss.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }


    /**
     * 调用者获取目标接口的代理类
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
