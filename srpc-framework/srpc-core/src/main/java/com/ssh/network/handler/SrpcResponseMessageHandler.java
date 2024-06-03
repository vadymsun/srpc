package com.ssh.network.handler;

import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.network.message.ResponseState;
import com.ssh.network.message.SrpcRequestMessage;
import com.ssh.network.message.SrpcResponseMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SrpcResponseMessageHandler extends SimpleChannelInboundHandler<SrpcResponseMessage> {
    // public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SrpcResponseMessage srpcResponseMessage) throws Exception {
        log.debug("{}",srpcResponseMessage);
        CompletableFuture<Object> completableFuture = SRPCBootstrap.waitingCalls.remove(1L);
        if(srpcResponseMessage.getState().equals(ResponseState.SUCCESS.getState())){
            completableFuture.complete(srpcResponseMessage.getReturnValue());
        }else {
            completableFuture.completeExceptionally((Throwable) srpcResponseMessage.getReturnValue());
        }
     /*   Promise<Object> promise = PROMISES.remove(srpcResponseMessage.getSequenceId());
        if (promise != null) {
            Object returnValue = srpcResponseMessage.getReturnValue();
            promise.setSuccess(returnValue);

        }*/
    }
}
