package com.ssh.network.serialize;

import com.ssh.exceptions.SerializerNotFoundException;
import com.ssh.network.serialize.imp.HessianSerializer;
import com.ssh.network.serialize.imp.JdkSerializer;
import com.ssh.network.serialize.imp.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {

    public static final int JDK_SERIALIZER = 1;
    public static final int HESSIAN_SERIALIZER = 2;
    public static final int JSON_SERIALIZER =4;
    public static final Map<Integer, Serializer> mp = new HashMap<>();

    static {
        mp.put(JDK_SERIALIZER, new JdkSerializer());
        mp.put(HESSIAN_SERIALIZER, new HessianSerializer());
        mp.put(JSON_SERIALIZER, new JsonSerializer());
    }

    public static Serializer getSerializer(int type) {
        if(!mp.containsKey(type)){
            throw new SerializerNotFoundException("不支持指定的序列化方式！");
        }
        return mp.get(type);
    }
}
