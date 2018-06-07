package com.sinohb.system.upgrade.task;

import com.sinohb.system.upgrade.constant.UpgradeConstants;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FTPTask implements Runnable {

    FTPClient ftpClient;
    private static final String HOST_NAME = "172.16.19.83";
    private static final int PORT = 23;
    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "12345abc";
    private static final String URL_PATH = "/apkdownload/music/";

    public FTPTask() {
        ftpClient = new FTPClient();
    }

    private void connectFTP() throws IOException {
        ftpClient.setControlEncoding("UTF-8");
        int reply;
        ftpClient.connect(HOST_NAME, PORT);
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        }
        ftpClient.login(USER_NAME, PASSWORD);
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            FTPClientConfig config = new FTPClientConfig(ftpClient
                    .getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftpClient.configure(config);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        }
    }

    @Override
    public void run() {
        try {
            connectFTP();
            // 先判断服务器文件是否存在
            FTPFile[] files = ftpClient.listFiles(URL_PATH);
            if (files.length == 0) {
                return;
            }
            String fileName = files[0].getName();
            long size = files[0].getSize();
            File file = new File(UpgradeConstants.DOWNLOAD_PATH, fileName);
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            long localSize = 0;
            if (file.exists()) {
                localSize = file.length(); // 如果本地文件存在，获取本地文件的长度
                if (localSize == size) {
                    return;
                }
            }
            OutputStream out = new FileOutputStream(file, true);
            ftpClient.setRestartOffset(localSize);
            InputStream input = ftpClient.retrieveFileStream(URL_PATH+"/"+fileName);
            byte[] b = new byte[1024];
            int length = 0;
            long doneSize = localSize;
            while ((length = input.read(b)) != -1) {
                out.write(b, 0, length);
                doneSize  += length;
            }
            out.flush();
            out.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
