package com.sinohb.system.upgrade.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VertifyUtils {

    public static void VertifyFile(String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest degest = MessageDigest.getInstance("MD5");
        FileInputStream inputStream = new FileInputStream(filePath);
        DigestInputStream dis = new DigestInputStream(inputStream, degest);//对于大文件或者网络文件使用输入流的形式要比字节数组方便很多也节省内存
        byte[] buffer = new byte[1024 * 8];
        ByteArrayOutputStream fileOutput = new ByteArrayOutputStream();
        while ((dis.read(buffer)) != -1) {
            //跟读普通输入流是一样的，原理就是需要将输入流读完后，再调用digest方法才能获取整个文件的MD5
            fileOutput.write(buffer);
        }
        String fileContent = fileOutput.toString();//读取到的文件的内容
        byte[] sumary = degest.digest();
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < sumary.length; i++) {
            String tmp = Integer.toHexString(sumary[i] & 0xff);
            if (tmp.length() == 1)//如果这个字节的值小于16，那么转换的就只有一个字符，所以需要手动添加一个字符“0”，
                tmp = "0" + tmp;
            strBuffer.append(tmp);
        }
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }
    public static String getMD5(String filePath) throws NoSuchAlgorithmException, IOException {

        InputStream in = new FileInputStream(new File(filePath));

        StringBuffer md5 = new StringBuffer();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = in.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }

        byte[] mdbytes = md.digest();

        // convert the byte to hex format
        for (int i = 0; i < mdbytes.length; i++) {
            md5.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return md5.toString().toUpperCase();
    }


    public static String getFileMD5(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024*8];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(filePath);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
