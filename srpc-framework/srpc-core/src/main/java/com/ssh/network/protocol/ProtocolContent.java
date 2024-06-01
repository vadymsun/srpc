package com.ssh.network.protocol;

public class ProtocolContent {

    public static final byte[] MAGIC_NUMBER = "srpc".getBytes();

    public static final int VERSION = 1;

    public static final int JDK_SERIALIZE = 1;

    public static final int HESSIAN_SERIALIZE = 2;


}
