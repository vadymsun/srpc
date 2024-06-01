package com.ssh.network.initializer;

import com.ssh.network.handler.SrpcRequestMessageHandler;
import com.ssh.network.handler.SrpcResponseMessageHandler;
import com.ssh.network.protocol.SrpcMessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;

public class NettyBootstrapInitializer {
    @Getter
    private static final Bootstrap bootstrap = new Bootstrap();

    // 配置 netty bootstrap
    // todo 在静态代码块中进行配置存在什么问题？ 只能配置一次，后续无法修改
    static {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(10);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // todo 配置@sharable，避免每次创建channel的时候都new新的上下文无关的handler
                        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(102400, 8, 4, 0, 0));
                        socketChannel.pipeline().addLast(new LoggingHandler());
                        socketChannel.pipeline().addLast(new SrpcMessageCodec());
                        socketChannel.pipeline().addLast(new SrpcResponseMessageHandler());
                    }
                });
    }
    private NettyBootstrapInitializer(){}

}
