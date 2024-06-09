package com.ssh.network.handler;

import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.network.message.ResponseState;
import com.ssh.network.message.SrpcRequestMessage;
import com.ssh.network.message.SrpcResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
@Slf4j
public class SrpcRequestMessageHandler extends SimpleChannelInboundHandler<SrpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SrpcRequestMessage srpcRequestMessage) throws Exception {
        log.debug("服务方收到调用请求,调用接口{}", srpcRequestMessage.getInterfaceName());
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
