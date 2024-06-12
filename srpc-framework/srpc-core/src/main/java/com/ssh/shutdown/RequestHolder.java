package com.ssh.shutdown;

import lombok.Getter;
import lombok.Setter;


public class RequestHolder {
    private volatile Boolean isClose = false;

    public void close(){
        isClose = true;
    }

    public Boolean isClosed(){
        return isClose;
    }
}
