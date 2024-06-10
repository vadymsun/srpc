package com.ssh.network.serialize.imp;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.ssh.config.Configuration;
import com.ssh.network.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HessianOutput hessianOutput = new HessianOutput(bos);
            hessianOutput.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("序列化失败！");
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            HessianInput hi = new HessianInput(bis);
            return  (T) hi.readObject();
        } catch (IOException e) {
            log.error("反序列化失败！");
            throw new RuntimeException(e);
        }
    }
}
