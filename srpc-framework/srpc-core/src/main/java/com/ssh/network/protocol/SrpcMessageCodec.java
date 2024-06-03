package com.ssh.network.protocol;

import com.ssh.Constants;
import com.ssh.exceptions.NetworkException;
import com.ssh.network.message.Message;
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
        byteBuf.writeByte(ProtocolContent.JDK_SERIALIZE);

        // 消息类型
        byteBuf.writeByte(message.getMessageType());

        // 压缩方式
        byteBuf.writeByte(1);

        // 序列化
        byte[] bytes = messageToByteArray(message);
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
        Message message = byteArrayToMessage(bytes);
        list.add(message);
    }



    /**
     * 将对象序列化
     * @param message
     * @return
     */
    private byte[] messageToByteArray(Message message){
        // todo 提供多种序列化方案
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(message);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("序列化失败！");
            throw new RuntimeException(e);
        }
    }

    /**
     * 反序列化消息实体
     * @param bytes
     * @return
     */
    private Message byteArrayToMessage(byte[] bytes){
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (Message) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化失败！");
            throw new RuntimeException(e);
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
