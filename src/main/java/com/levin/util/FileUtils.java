package com.levin.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 文件工具类
 *
 * @author Levin
 */
public class FileUtils {
    /**
     * 获取项目根路径
     */
    public static String getAppPath() {
        try {
            return new File("").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return System.getProperty("user.dir");
    }

    /**
     * 将字符串写入到文件
     */
    public static void writeFile(String path, String content) {
        try {
            File file = new File(path);
            if (file.exists() || file.createNewFile()) {
                OutputStream os = new FileOutputStream(file, true);
                byte[] bytes = content.getBytes();
                os.write(bytes);
                os.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
