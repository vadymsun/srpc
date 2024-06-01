package com.ssh.bootstrap;


import com.ssh.network.handler.SrpcRequestMessageHandler;
import com.ssh.network.handler.SrpcResponseMessageHandler;
import com.ssh.network.message.SrpcRequestMessage;
import com.ssh.network.protocol.SrpcMessageCodec;
import com.ssh.registry.Registry;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SRPCBootstrap {
    // 单例模式 每个服务只允许拥有一个启动器实例
    public static SRPCBootstrap srpcBootstrap = new SRPCBootstrap();

    // 服务名称
    private String appName;


    private ProtocolConfig protocolConfig;

    private Registry registry;


    // 服务端本地维护的 全类名与对象之间的映射
    public static final Map<String, ServiceConfig<?>> serverMap = new ConcurrentHashMap<>();


    // 客户端维护的 服务地址和channel的映射
    private static final Map<String, Channel> channels = new ConcurrentHashMap<>();

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


    /**
     * j连接zookeeper
     * @param registryConfig
     * @return
     */
    public SRPCBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
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
    public SRPCBootstrap service(ServiceConfig<?> service) {
        registry.publish(service);
        serverMap.put(service.getInterfaceName(), service);
        return srpcBootstrap;
    }

    public SRPCBootstrap service(List<ServiceConfig<?>> services) {
        return srpcBootstrap;
    }


    /**
     * 启动netty 等待服务调用方的请求
     */
    public void start() {
        // 启动netty服务
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup(10);
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(102400, 8, 4, 0, 0));
                        socketChannel.pipeline().addLast(new LoggingHandler());
                        socketChannel.pipeline().addLast(new SrpcMessageCodec());
                        socketChannel.pipeline().addLast(new SrpcRequestMessageHandler());
                    }

                });

        try {
            Channel channel = serverBootstrap.bind(8081).sync().channel();
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
     * @return
     */
    public SRPCBootstrap reference(ReferenceConfig<?> referenceConfig) {

        ClassLoader classLoader = referenceConfig.getInterfaceConsumed().getClassLoader();
        Class<?>[] interfaces = new Class[]{referenceConfig.getInterfaceConsumed()};

        Object reference = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 1. 发现服务：从注册中心用负责均衡算法选择一个服务
                String serverHost = registry.discover(referenceConfig.getInterfaceConsumed().getName());
                if (serverHost == "") {
                    throw new RuntimeException();
                }
                String ip = serverHost.split(":")[0];
                int port = Integer.valueOf(serverHost.split(":")[1]);
                System.out.println("connect" + ip + port);
                // 2。获取netty连接
                Channel channel;
                if(!serverMap.containsKey(serverHost)){
                    NioEventLoopGroup group = new NioEventLoopGroup();
                    ChannelFuture channelFuture = new Bootstrap()
                            .group(group)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<Channel>() {

                                @Override
                                protected void initChannel(Channel channel) throws Exception {
                                    channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                                            102400, 8,
                                            4, 0, 0));
                                    channel.pipeline().addLast(new LoggingHandler());
                                    channel.pipeline().addLast(new SrpcMessageCodec());
                                    channel.pipeline().addLast(new SrpcResponseMessageHandler());
                                }
                            }).connect(ip, port)
                            .sync();
                    Channel cur_channel = channelFuture.channel();
                    channels.put(serverHost, cur_channel);

                }
                channel = channels.get(serverHost);
                // 3. 封装并发送消息
                SrpcRequestMessage srpcRequestMessage = new SrpcRequestMessage(referenceConfig.getInterfaceConsumed().getName(),
                        method.getName(),
                        method.getParameterTypes(),
                        args,
                        method.getReturnType());
                channel.writeAndFlush(srpcRequestMessage);
                DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());
                SrpcResponseMessageHandler.PROMISES.put(1, promise);


                // 4。等待promise结束
                promise.await();

                return null;
            }
        });
        referenceConfig.setReference(reference);

        return srpcBootstrap;
    }
}
