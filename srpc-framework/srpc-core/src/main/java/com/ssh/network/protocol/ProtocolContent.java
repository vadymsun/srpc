package com.ssh.network.protocol;

public class ProtocolContent {

    public static final byte[] MAGIC_NUMBER = "srpc".getBytes();

    public static final int VERSION = 1;

    public static final int JDK_SERIALIZE = 1;

    public static final int HESSIAN_SERIALIZE = 2;





    public static final int MAX_FRAME_LENGTH = 10240;
    public static final int LENGTH_FIELD_OFFSET = 8;
    public static final int LENGTH_FIELD_LENGTH= 4;


}
