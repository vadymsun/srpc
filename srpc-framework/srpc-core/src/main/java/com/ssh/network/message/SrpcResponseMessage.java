package com.ssh.network.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class SrpcResponseMessage extends Message {
    private int SequenceId;

    private String message;

    private Object returnValue;

    @Override
    public int getMessageType() {
        return RPC_RESPONSE_MESSAGE;
    }
}
