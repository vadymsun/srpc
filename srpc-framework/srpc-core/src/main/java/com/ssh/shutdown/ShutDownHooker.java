package com.ssh.shutdown;

import com.ssh.bootstrap.SRPCBootstrap;

public class ShutDownHooker extends Thread{

    @Override
    public void run() {
        // 设置挡板，阻止新请求进入
        SRPCBootstrap.getInstance().getRequestHolder().close();

        // 等待处理完所有未完成的请求
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (SRPCBootstrap.getInstance().getRequestProcessingCounter().getCount() == 0
                    || System.currentTimeMillis() - start > 10000) {
                break;
            }
        }

    }

    public static void main(String[] args) {
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM is shutting down, performing cleanup...");
            performCleanup();
            System.out.println("Cleanup completed.");
        }));

        System.out.println("Application is running. Press Ctrl+C to exit.");

        try {
            // 模拟应用程序的运行
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void performCleanup() {
        // 执行清理操作，比如关闭资源、保存数据等
        System.out.println("Performing cleanup tasks...");
    }
}
