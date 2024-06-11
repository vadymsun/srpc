package com.ssh.proxy.handler;

import com.ssh.anotation.Retry;
import com.ssh.bootstrap.ReferenceConfig;
import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.exceptions.NetworkException;
import com.ssh.network.initializer.NettyBootstrapInitializer;
import com.ssh.network.message.SrpcRequestMessage;
import com.ssh.proxy.RequestIdGenerator;
import com.ssh.registry.Registry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SrpcConsumerInvocationHandler implements InvocationHandler {

    private final Registry registry;
    private final ReferenceConfig<?> referenceConfig;

    public SrpcConsumerInvocationHandler(Registry registry, ReferenceConfig<?> referenceConfig) {
        this.registry = registry;
        this.referenceConfig = referenceConfig;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取方法注解
        Retry annotation = method.getAnnotation(Retry.class);
        // 配置重试参数
        int tryTimes = 0;
        int waitTime = 0;
        if(annotation != null){
            tryTimes = annotation.tryTimes();
            waitTime = annotation.waitTime();
        }

        while(true) {
            try {
                // 1. 发现服务：从注册中心用负责均衡算法选择一个服务
                String serverHost = SRPCBootstrap.getInstance().getLoadBalancer().getServerHost(referenceConfig.getInterfaceConsumed().getName());
                if (serverHost.isEmpty()) {
                    throw new RuntimeException();
                }

                // 2。获取netty连接 先从channel缓存中读取，如果缓存中没有就创建于一个channel并加入到缓存
                Channel channel = getChannel(serverHost);

                // 3. 封装rpc请求消息，并发送给服务方
                long requestId = RequestIdGenerator.getRequestId();
                SrpcRequestMessage srpcRequestMessage = new SrpcRequestMessage(
                        referenceConfig.getInterfaceConsumed().getName(),
                        method.getName(),
                        method.getParameterTypes(),
                        args,
                        method.getReturnType());
                srpcRequestMessage.setRequestId(requestId);
                ChannelFuture channelFuture = channel.writeAndFlush(srpcRequestMessage);

                // 4 挂起调用请求，阻塞当前线程最多10秒等待服务方发送结果
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                SRPCBootstrap.WAITING_CALLS.put(requestId, completableFuture);

                return completableFuture.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.debug("调用{}时发生异常，剩余重试次数{}", method.getName(), tryTimes);
                if(tryTimes-- <= 0){
                    Thread.sleep(waitTime);
                    break;
                }

            }
        }
        throw new RuntimeException("进行远程调用时失败");
    }

    /**
     * 获取netty channel
     * @param serverHost
     * @return
     * @throws InterruptedException
     */
    private Channel getChannel(String serverHost) throws InterruptedException {
        String ip = serverHost.split(":")[0];
        int port = Integer.parseInt(serverHost.split(":")[1]);
        if(!SRPCBootstrap.CHANNEL_CACHE.containsKey(serverHost)){
            log.debug("创建新channel");
            Channel channel = NettyBootstrapInitializer.getBootstrap().connect(ip,port).sync().channel();
            SRPCBootstrap.CHANNEL_CACHE.put(serverHost, channel);
        }
        Channel channel = SRPCBootstrap.CHANNEL_CACHE.get(serverHost);
        if(channel == null){
            throw new NetworkException("客户端获取netty channel异常");
        }
        return channel;
    }
}
