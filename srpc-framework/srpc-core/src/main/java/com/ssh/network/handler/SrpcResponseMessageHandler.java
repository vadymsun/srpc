package com.ssh.network.handler;

import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.network.message.ResponseState;
import com.ssh.network.message.SrpcResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class SrpcResponseMessageHandler extends SimpleChannelInboundHandler<SrpcResponseMessage> {
    // public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SrpcResponseMessage srpcResponseMessage) throws Exception {
        log.debug("收到请求id为{}的响应",srpcResponseMessage.getRequestId());
        // 根据响应的id从挂起的请求中取出响应
        CompletableFuture<Object> completableFuture = SRPCBootstrap.WAITING_CALLS.remove(srpcResponseMessage.getRequestId());
        // 根据响应的结果设置 completableFuture
        if(srpcResponseMessage.getState().equals(ResponseState.SUCCESS.getState())){
            completableFuture.complete(srpcResponseMessage.getReturnValue());
        }else if(srpcResponseMessage.getState().equals(ResponseState.FAILED.getState())) {
            completableFuture.completeExceptionally((Throwable) srpcResponseMessage.getReturnValue());
        } else if (srpcResponseMessage.getState().equals(ResponseState.RESTRICTED.getState())) {
            completableFuture.completeExceptionally(new RuntimeException());
        }
    }
}
