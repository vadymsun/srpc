package com.ssh.network.message;

import lombok.Getter;

@Getter
public enum ResponseState {


    SUCCESS(200,"Success"),
    SERVER_CLOSING(300, "server is closing, try another server!"),
    RESTRICTED(600, "Server is busy!"),
    FAILED(500, "Failed");

    private ResponseState(Integer state,String msg){
        this.msg = msg;
        this.state = state;
    }


    private String msg;
    private Integer state;
}
