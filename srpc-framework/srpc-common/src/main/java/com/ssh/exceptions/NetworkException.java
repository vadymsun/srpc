package com.ssh.exceptions;

public class NetworkException extends RuntimeException{
    public NetworkException(){

    }
    public NetworkException(String msg){
        super(msg);
    }

    public NetworkException(Throwable cause){
        super(cause);
    }
}
