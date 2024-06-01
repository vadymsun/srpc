package com.ssh.network.handler;

import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.bootstrap.ServiceConfig;
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
        log.debug("{}", srpcRequestMessage);
        ServiceConfig<?> serviceConfig = SRPCBootstrap.serverMap.get(srpcRequestMessage.getInterfaceName());
        // 利用反射获取方法
        Method method = serviceConfig.getReference().getClass().getMethod(srpcRequestMessage.getMethodName(), srpcRequestMessage.getArgTypes());
        Object object = method.invoke(serviceConfig.getReference(), srpcRequestMessage.getArgs());
        SrpcResponseMessage responseMessage = new SrpcResponseMessage(1,"成功", object);
        log.debug("{}", responseMessage);
        channelHandlerContext.writeAndFlush(responseMessage);
    }
}
