package com.ssh.network.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class SrpcFrameDecoder extends LengthFieldBasedFrameDecoder {

    public SrpcFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }
    public SrpcFrameDecoder() {
        super(
                ProtocolContent.MAX_FRAME_LENGTH,
                ProtocolContent.LENGTH_FIELD_OFFSET,
                ProtocolContent.LENGTH_FIELD_LENGTH
        );
    }
}
