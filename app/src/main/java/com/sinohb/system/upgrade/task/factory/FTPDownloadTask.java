package com.sinohb.system.upgrade.task.factory;

import com.sinohb.system.upgrade.entity.DownloadInfo;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;


public class FTPDownloadTask extends DownloadTask {

    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "12345abc";
    private String servicePath;
    private int port;
    private String remoteFilePath;

    public FTPDownloadTask(String url) {
        super(url);
    }

    @Override
    public DownloadInfo call() throws Exception {
        DownloadInfo downloadInfo = null;
        FTPClient ftpClient = (FTPClient) connect();
        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(remoteFilePath);
        if (files.length == 0) {
            downloadFailure("file is not exist");
            return downloadInfo;
        }
        fileName = files[0].getName();
        mFileSize = files[0].getSize();
        downloadInfo = new DownloadInfo();
        downloadInfo.setmFileName(fileName);
        downloadInfo.setmFileSize(mFileSize);
        startDownload();
        return downloadInfo;
    }

    @Override
    public  Object connect() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("UTF-8");
        int reply;
        String[] rex = url.split(":");
        servicePath = rex[0].replace("ftp://", "");
        int index = rex[1].indexOf('/');
        port = Integer.parseInt(rex[1].substring(0, index));
        remoteFilePath = rex[1].substring(index + 1);
        ftpClient.connect(servicePath, port);
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
        return ftpClient;
    }

}
