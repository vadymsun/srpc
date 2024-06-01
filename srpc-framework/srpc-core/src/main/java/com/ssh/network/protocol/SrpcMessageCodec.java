package com.ssh.network.protocol;

import com.ssh.network.message.Message;
import com.ssh.network.message.SrpcRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class SrpcMessageCodec extends ByteToMessageCodec<Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        // 魔数 4字节
        byteBuf.writeBytes(new byte[]{1,1,1,1});

        // 版本号
        byteBuf.writeByte(ProtocolContent.VERSION);

        // 序列化方式
        byteBuf.writeByte(ProtocolContent.JDK_SERIALIZE);

        // 消息类型
        byteBuf.writeByte(message.getMessageType());

        // 字节填充
        byteBuf.writeByte(1);
        // 序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message);
        byte[] bytes = bos.toByteArray();

        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magicNum = byteBuf.readInt();

        byte version = byteBuf.readByte();

        byte serializeType = byteBuf.readByte();

        byte messageType = byteBuf.readByte();

        byte b = byteBuf.readByte();

        int length = byteBuf.readInt();
        // 把指定长度的字节流读取到指定的字节数组中
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes, 0, length);
        // 反序列化
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));

        Message message = (Message) ois.readObject();
        list.add(message);
    }
}
