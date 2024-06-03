package com.ssh.network.serialize;

import com.ssh.exceptions.SerializerNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {

    public static final int JDK_SERIALIZER = 1;
    public static final Map<Integer, Serializer> mp = new HashMap<>();

    static {
        mp.put(JDK_SERIALIZER, new JdkSerializer());
    }

    public static Serializer getSerializer(int type) {
        if(!mp.containsKey(type)){
            throw new SerializerNotFoundException("不支持指定的序列化方式！");
        }
        return mp.get(type);
    }
}
