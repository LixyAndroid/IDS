package com.levin.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件类型：图片 excel文件
     */
    public enum fileType {
        image, excel
    }

    /**
     * 不允许实例化
     */
    private FileUtils() {

    }

    /**
     * 上传文件
     */
    public static String upload(MultipartFile file, String contentType) {
        String realPath = getRootPath() + File.separator + "upload";
        String path = File.separator + new SimpleDateFormat("yyyyMMdd").format(new Date()) + File.separator + UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        if (contentType.equals(fileType.image.toString())) {
            realPath += File.separator + "image" + path;
        } else if (contentType.equals(fileType.excel.toString())) {
            realPath += File.separator + "excel" + path;
        } else {
            realPath += File.separator + "file" + path;
        }

        File destFile = createFile(realPath);
        try {
            file.transferTo(destFile);
            //org.apache.commons.io.FileUtils.moveFile(file, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return realPath;
    }

    public static File uploadFile(MultipartFile file, String contentType) {
        String realPath = getRootPath() + File.separator + "upload";
        String path = File.separator + new SimpleDateFormat("yyyyMMdd").format(new Date()) + File.separator + UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        if (contentType.equals(fileType.image.toString())) {
            realPath += File.separator + "image" + path;
        } else if (contentType.equals(fileType.excel.toString())) {
            realPath += File.separator + "excel" + path;
        } else {
            realPath += File.separator + "file" + path;
        }

        File destFile = createFile(realPath);
        try {
            file.transferTo(destFile);
            //org.apache.commons.io.FileUtils.moveFile(file, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return destFile;
    }

    /**
     * 创建目录
     */
    public static File createDir(String dirPath) {
        File dir;
        try {
            dir = new File(dirPath);
            if (!dir.exists()) {
                org.apache.commons.io.FileUtils.forceMkdir(dir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dir;
    }

    /**
     * 创建文件
     */
    public static File createFile(String filePath) {
        File file;
        try {
            file = new File(filePath);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                org.apache.commons.io.FileUtils.forceMkdir(parentDir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    /**
     * 复制目录（不会复制空目录）
     */
    public static void copyDir(String srcPath, String destPath) {
        try {
            File srcDir = new File(srcPath);
            File destDir = new File(destPath);
            if (srcDir.exists() && srcDir.isDirectory()) {
                org.apache.commons.io.FileUtils.copyDirectoryToDirectory(srcDir, destDir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 复制文件
     */
    public static void copyFile(String srcPath, String destPath) {
        try {
            File srcFile = new File(srcPath);
            File destDir = new File(destPath);
            if (srcFile.exists() && srcFile.isFile()) {
                org.apache.commons.io.FileUtils.copyFileToDirectory(srcFile, destDir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除目录
     */
    public static void deleteDir(String dirPath) {
        try {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                org.apache.commons.io.FileUtils.deleteDirectory(dir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文件
     */
    public static void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                org.apache.commons.io.FileUtils.forceDelete(file);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 重命名文件
     */
    public static void renameFile(String srcPath, String destPath) {
        File srcFile = new File(srcPath);
        if (srcFile.exists()) {
            File newFile = new File(destPath);
            boolean result = srcFile.renameTo(newFile);
            if (!result) {
                throw new RuntimeException("重命名文件出错！" + newFile);
            }
        }
    }


    /**
     * 获取真实文件名（去掉文件路径）
     */
    public static String getRealFileName(String fileName) {
        return FilenameUtils.getName(fileName);
    }

    /**
     * 判断文件是否存在
     */
    public static boolean checkFileExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * 获取应用根路径
     *
     * @return
     */
    public static String getRootPath() {
        return System.getProperty("user.dir");
    }
}
