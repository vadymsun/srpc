package com.ssh.network.message;

import java.io.Serializable;

public abstract class Message implements Serializable {

    public static final int RPC_REQUEST_MESSAGE = 1;
    public static final int RPC_RESPONSE_MESSAGE = 2;

    public abstract int getMessageType();

}
