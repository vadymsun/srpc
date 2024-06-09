package com.ssh.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    /**
     * 获取一个包下的所有类的全限定名
     * @param packageName
     * @return
     */
    public static List<String> getAllClass(String packageName) {
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        System.out.println(url);
        if(url == null){
            throw new RuntimeException("配置的包路径不存在！");
        }
        String absolutePath = url.getPath();
        System.out.println(absolutePath);
        ArrayList<String> classNames = new ArrayList<>();
        // 递归获取所有的类
        recursionFile(absolutePath, classNames, basePath);
        return classNames;

    }

    public static void recursionFile(String absolutePath, ArrayList<String> classNames, String basePath) {
        File file = new File(absolutePath);
        if(file.isDirectory()){
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null){
                return;
            }
            for(File child : children){
                recursionFile(child.getAbsolutePath(), classNames, basePath);
            }
        }else {
            classNames.add(getClassNameByAbsolutePath(absolutePath, basePath));
        }
    }
    public static String getClassNameByAbsolutePath(String absolutePath,String basePath) {
        // E:\project\ydlclass-yrpc\yrpc-framework\yrpc-core\target\classes\com\ydlclass\serialize\Serializer.class
        // com\ydlclass\serialize\Serializer.class --> com.ydlclass.serialize.Serializer
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        System.out.println(fileName);
        return fileName;
    }
}
