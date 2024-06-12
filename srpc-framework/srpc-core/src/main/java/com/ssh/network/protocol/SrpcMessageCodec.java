package com.ssh.network.protocol;

import com.ssh.Constants;
import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.exceptions.NetworkException;
import com.ssh.network.message.Message;
import com.ssh.network.message.SrpcRequestMessage;
import com.ssh.network.message.SrpcResponseMessage;
import com.ssh.network.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.util.List;

@Slf4j
public class SrpcMessageCodec extends ByteToMessageCodec<Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        // 魔数 4字节
        byteBuf.writeBytes(ProtocolContent.MAGIC_NUMBER);

        // 版本号
        byteBuf.writeByte(ProtocolContent.VERSION);

        // 序列化方式
        int serializerType = SRPCBootstrap.getInstance().getConfiguration().getSerializerType();
        byteBuf.writeByte(serializerType);

        // 消息类型
        byteBuf.writeByte(message.getMessageType());

        // 压缩方式
        byteBuf.writeByte(1);

        // 序列化
        byte[] bytes = SerializerFactory.getSerializer(serializerType).serialize(message);
        if(message.getMessageType() == Message.RPC_RESPONSE_MESSAGE){
            SRPCBootstrap.getInstance().getRequestProcessingCounter().decrease();
            log.debug("处理完一个请求，还剩{}个",SRPCBootstrap.getInstance().getRequestProcessingCounter().getCount());
        }

        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {


        byte[] magicNum = new byte[4];
        byteBuf.readBytes(magicNum);
        if (!checkMagicNum(magicNum)) {
            log.error("消息类型不匹配！");
            throw new NetworkException();
        }

        byte version = byteBuf.readByte();
        if(version > ProtocolContent.VERSION){
            log.error("协议版本错误！");
            throw new NetworkException();
        }

        byte serializeType = byteBuf.readByte();

        byte messageType = byteBuf.readByte();

        byte compressType = byteBuf.readByte();

        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes, 0, length);

        // 反序列化
        if ((int)messageType == Message.RPC_REQUEST_MESSAGE){
            SrpcRequestMessage message = SerializerFactory.getSerializer(serializeType).deserialize(bytes, SrpcRequestMessage.class);
            list.add(message);
        } else if ((int)messageType == Message.RPC_RESPONSE_MESSAGE) {
            SrpcResponseMessage message = SerializerFactory.getSerializer(serializeType).deserialize(bytes, SrpcResponseMessage.class);
            list.add(message);
        }

    }



    private boolean checkMagicNum(byte[] magicNum) {
        for (int i = 0; i < 4; i++) {
            if(magicNum[i] != ProtocolContent.MAGIC_NUMBER[i]){
                return false;
            }
        }
        return true;
    }
}
