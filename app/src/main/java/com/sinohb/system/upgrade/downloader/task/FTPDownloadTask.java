package com.sinohb.system.upgrade.downloader.task;

import com.sinohb.logger.LogTools;
import com.sinohb.logger.utils.IOUtils;
import com.sinohb.system.upgrade.database.DatabaseFoctory;
import com.sinohb.system.upgrade.downloader.DownloadProcessListener;
import com.sinohb.system.upgrade.entity.DownloadEntity;
import com.sinohb.system.upgrade.downloader.manager.BaseDownloadManager;
import com.sinohb.system.upgrade.downloader.manager.FTPDownloadManager;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FTPDownloadTask extends BaseDownloadTask {

    public FTPDownloadTask(String url, File file, long downloadSize, int id, DownloadProcessListener processListener, long fileSize) {
        super(url, file, downloadSize, id, processListener, fileSize);
    }


    @Override
    public void run() {
        DownloadEntity info = getDownloadStartAndEnd();//dao.query(url.toString(), id);//查询记录当中没有下完的任务
        long start = info.getDownloadStartIndex();// 开始位置 = 已下载量
        long finishSize = start;
        long end = info.getDownloadEndIndex();
        update(info.getDoneSize());
        LogTools.p(TAG, "start:" + start + ",end:" + end);
        RandomAccessFile raf = null;
        FTPClient client = null;
        InputStream in = null;
        try {
            raf = new RandomAccessFile(mFile, "rws");
            raf.seek(start);
            byte[] buf = new byte[1024 * 8];
            int len;
            client = (FTPClient) ((BaseDownloadManager) mProcessListener).connect(url);
            client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            client.enterLocalPassiveMode();
            client.setRestartOffset(start);
            String remotePath = ((FTPDownloadManager) mProcessListener).getRemoteFilePath();
            LogTools.p(TAG, "remotePath:" + remotePath);
            in = client.retrieveFileStream(remotePath);
            long downloadSize = end - start;
            long doneSize = 0;
            int progress;
            while ((len = in.read(buf)) != -1) {
                if (finishSize + len > end) {
                    len = (int) (end - finishSize) + 1;
                    raf.write(buf, 0, len);
                    update(len);
                    finishDownload();
                    return;
                }
                raf.write(buf, 0, len);
                finishSize += len;
                update(len);
                doneSize += len;
                progress = (int) (100 * doneSize / downloadSize);
                if (progress >= 10) {
                    updateProgress(info, finishSize);
                    doneSize = 0;
                }
                if (isPause) {
                    updateProgress(info, finishSize);
                    LogTools.p(TAG, "进入暂停");
                    synchronized (sync) {
                        try {
                            sync.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        client = (FTPClient) ((BaseDownloadManager) mProcessListener).connect(url);
                        client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                        client.enterLocalPassiveMode();
                        raf.seek(finishSize);
                        client.setRestartOffset(finishSize);
                        in = client.retrieveFileStream(remotePath);
                        LogTools.p(TAG, "恢复下载");
                    }
                }
                if (isCancel) {
                    cancelDownload(info);
                    return;
                }
                if (stop) {
                    updateProgress(info, finishSize);
                    LogTools.e(TAG, "停止下载");
                    return;
                }
            }
            finishDownload();
        } catch (FileNotFoundException e) {
            LogTools.e(TAG, e, "下载任务失败 url=" + url);
            updateProgress(info,finishSize);
            downloadNetWorkError(e.getMessage());
        } catch (IOException e) {
            LogTools.e(TAG, e, "下载任务失败 url=" + url);
            updateProgress(info,finishSize);
            downloadNetWorkError(e.getMessage());
        } catch (Exception e) {
            LogTools.e(TAG, e, "下载任务失败 url=" + url);
            updateProgress(info,finishSize);
            downloadNetWorkError(e.getMessage());
        } finally {
            IOUtils.closeQuietly(raf);
            IOUtils.closeQuietly(in);
            if (client != null) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }



}
