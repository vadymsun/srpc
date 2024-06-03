package com.ssh.exceptions;

public class SerializerNotFoundException extends RuntimeException{
    public SerializerNotFoundException(){

    }
    public SerializerNotFoundException(String msg){
        super(msg);
    }
}
