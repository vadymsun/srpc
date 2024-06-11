package com.ssh.anotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    // 重试次数
    int tryTimes() default 3;

    // 重试等待时间 ms
    int waitTime() default 2000;
}
