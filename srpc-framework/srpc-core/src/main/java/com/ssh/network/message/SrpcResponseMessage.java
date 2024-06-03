package com.ssh.network.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class SrpcResponseMessage extends Message {

    private String message;

    private Object returnValue;

    private Integer state;


    public SrpcResponseMessage(ResponseState responseState, Object returnValue){
        this.message = responseState.getMsg();
        this.state = responseState.getState();
        this.returnValue = returnValue;
    }


    @Override
    public int getMessageType() {
        return RPC_RESPONSE_MESSAGE;
    }
}
