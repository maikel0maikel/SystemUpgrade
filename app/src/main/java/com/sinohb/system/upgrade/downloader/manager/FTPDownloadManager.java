package com.sinohb.system.upgrade.downloader.manager;

import com.sinohb.logger.LogTools;
import com.sinohb.logger.utils.IOUtils;
import com.sinohb.system.upgrade.constant.UpgradeConstants;
import com.sinohb.system.upgrade.entity.UpgradeEntity;
import com.sinohb.system.upgrade.utils.JsonUtils;
import com.sinohb.system.upgrade.utils.VersionUtils;
import com.sinohb.system.upgrade.utils.VertifyUtils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class FTPDownloadManager extends BaseDownloadManager {

    private static final String USER_NAME = "liujh";
    private static final String PASSWORD = "centos";
    private String remoteFilePath;

    public FTPDownloadManager(String url) {
        super(url);
    }

    @Override
    public UpgradeEntity call() {
        UpgradeEntity upgradeEntity = null;
        try {
            upgradeEntity = getDownloadInfo();
            if (upgradeEntity == null) {
                return null;
            }
            notifyUpdateInfo(upgradeEntity);
            int localVersionCode = VersionUtils.getVersion(VersionUtils.getOemVersion());
            int remoteVersionCode = VersionUtils.getVersion(upgradeEntity.getVersion());
            if (remoteVersionCode > localVersionCode) {
                mFileSize = upgradeEntity.getFileSize();
                LogTools.p(TAG, "下载大小：" + mFileSize);
                if (mFileSize == 0) {
                    downloadFailure("下载大小为0");
                    return upgradeEntity;
                }

                File mDownloadFile = new File(UpgradeConstants.DOWNLOAD_PATH, upgradeEntity.getFileName());
                if (!mDownloadFile.getParentFile().exists()) {
                    mDownloadFile.getParentFile().mkdirs();
                }
                if (mDownloadFile.exists()) {
                    String localMd5 = null;
                    try {
                        localMd5 = VertifyUtils.getMD5(mDownloadFile.getAbsolutePath());
                    } catch (NoSuchAlgorithmException e) {
                        LogTools.p(TAG, e, "get md5 error ");
                    }
                    if (localMd5 != null && localMd5.length() > 0 &&
                            localMd5.equalsIgnoreCase(upgradeEntity.getMD5()) &&
                            mFileSize == mDownloadFile.length()) {
                        LogTools.p(TAG, "本地存在要下载的文件并且md5校验通过不再下载");
                        notifyUpgrade();
                        return upgradeEntity;
                    }else {
                        LogTools.p(TAG,"本地文件存在md5校验不通过或者文件下载未完成需下载 localMd5="+localMd5+",localFileSize = "+mDownloadFile.length());
                    }
                }
                startDownload(upgradeEntity.getDownloadUrl(), mDownloadFile);
            } else {
                noNewVersion();
                LogTools.p(TAG, "没有更新localVersionCode" + localVersionCode + ",remoteVersionCode:" + remoteVersionCode);
            }
        } catch (IOException e) {
            downloadFailure(e.getMessage());
            LogTools.e(TAG, e, "下载失败：" + url);
        }
        return upgradeEntity;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    @Override
    public Object connect(String url) throws IOException {
        String[] rex = url.split(":");
        if (rex.length < 3) {
            throw new IOException("rex length less then 3" + Arrays.toString(rex));
        }
        String servicePath = rex[1].replace("//", "");
        int index = rex[2].indexOf('/');
        int port = Integer.parseInt(rex[2].substring(0, index));
        remoteFilePath = rex[2].substring(index);
        return realConnect(servicePath, port);
    }

    @Override
    public UpgradeEntity getDownloadInfo() throws IOException {
        UpgradeEntity upgradeEntity = null;
        FTPClient ftpClient = null;
        InputStream ins = null;
        BufferedReader reader = null;
        ftpClient = (FTPClient) connect(url);
        // 先判断服务器文件是否存在
        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
        FTPFile[] files = ftpClient.listFiles(remoteFilePath);
        if (files.length == 0) {
            downloadFailure("file is not exist");
            LogTools.e(TAG, "该文件不存在地址：" + url);
            return upgradeEntity;
        }
        ins = ftpClient.retrieveFileStream(remoteFilePath);
        reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        line = builder.toString();
        LogTools.p(TAG,"服务器信息："+line);
        upgradeEntity = JsonUtils.parse(line, UpgradeEntity.class);
        if (upgradeEntity == null) {
            downloadFailure("从服务器获取json解析出错");
            LogTools.p(TAG, "upgradeEntity为空" + line);
            return upgradeEntity;
        }
        IOUtils.closeQuietly(ins);
        IOUtils.closeQuietly(reader);
        if (ftpClient != null) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return upgradeEntity;
    }

    private FTPClient realConnect(String server, int port) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("UTF-8");
        int reply;
        ftpClient.setConnectTimeout(15 * 1000);
        ftpClient.connect(server, port);
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
        }
        return ftpClient;
    }

}
