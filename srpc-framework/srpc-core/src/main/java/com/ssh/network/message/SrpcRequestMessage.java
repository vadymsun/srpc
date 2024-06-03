package com.ssh.network.message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class SrpcRequestMessage extends Message {

    // 接口的全限定名
    private String interfaceName;

    // 方法名
    private String methodName;

    // 参数类型列表
    private Class<?>[] argTypes;

    // 参数值
    private Object[] args;

    //返回值类型
    private Class<?> returnType;


    @Override
    public int getMessageType() {
        return RPC_REQUEST_MESSAGE;
    }
}
