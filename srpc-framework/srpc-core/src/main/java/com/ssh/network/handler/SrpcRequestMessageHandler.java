package com.ssh.network.handler;

import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.network.message.ResponseState;
import com.ssh.network.message.SrpcRequestMessage;
import com.ssh.network.message.SrpcResponseMessage;
import com.ssh.protection.Restrictor;
import com.ssh.protection.RestrictorFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
@Slf4j
public class SrpcRequestMessageHandler extends SimpleChannelInboundHandler<SrpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SrpcRequestMessage srpcRequestMessage) throws Exception {
        log.debug("服务方收到调用请求,调用接口{}", srpcRequestMessage.getInterfaceName());

        // 服务端对每个ip进行限流访问
        String clientHost = channelHandlerContext.channel().remoteAddress().toString();
        if(!SRPCBootstrap.RESTRICTOR_MAP.containsKey(clientHost)){
            SRPCBootstrap.RESTRICTOR_MAP.put(clientHost, RestrictorFactory.createRestrictor());
        }
        // 获取当前调用方的限流器
        Restrictor curRestrictor =  SRPCBootstrap.RESTRICTOR_MAP.get(clientHost);
        if(!curRestrictor.isAllowed()){
            // 限流
            SrpcResponseMessage responseMessage = new SrpcResponseMessage(ResponseState.FAILED);
            responseMessage.setRequestId(srpcRequestMessage.getRequestId());
            log.debug("服务方限流 {}", responseMessage);
            channelHandlerContext.writeAndFlush(responseMessage);
        }else{
            // 调用本地方法并封装返回
            Object object = SRPCBootstrap.SERVER_MAP.get(srpcRequestMessage.getInterfaceName());
            try {
                // 利用反射执行目标方法并获取结果
                Class<?> aClass = object.getClass();
                Method method = aClass.getMethod(
                        srpcRequestMessage.getMethodName(),
                        srpcRequestMessage.getArgTypes());
                Object result = method.invoke(object, srpcRequestMessage.getArgs());
                // 封装rpc响应消息
                SrpcResponseMessage responseMessage = new SrpcResponseMessage(ResponseState.SUCCESS, result);
                responseMessage.setRequestId(srpcRequestMessage.getRequestId());
                log.debug("服务方 方法运行成功 {}", responseMessage);
                channelHandlerContext.writeAndFlush(responseMessage);
            }catch (Exception e){
                SrpcResponseMessage responseMessage = new SrpcResponseMessage(ResponseState.FAILED, e);
                responseMessage.setRequestId(srpcRequestMessage.getRequestId());
                System.out.println(e);
                log.debug("服务方 方法调用发生异常 {}", responseMessage);
                channelHandlerContext.writeAndFlush(responseMessage);
            }
        }



    }
}
