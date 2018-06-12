package com.sinohb.system.upgrade.downloader.task;

import com.sinohb.logger.LogTools;
import com.sinohb.logger.utils.IOUtils;
import com.sinohb.system.upgrade.database.DatabaseFoctory;
import com.sinohb.system.upgrade.downloader.DownloadProcessListener;
import com.sinohb.system.upgrade.entity.DownloadEntity;
import com.sinohb.system.upgrade.downloader.manager.BaseDownloadManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

public class HttpDownloadTask extends BaseDownloadTask {

    public HttpDownloadTask(String url, File file, long downloadSize, int id, DownloadProcessListener processListener, long fileSize) {
        super(url, file, downloadSize, id, processListener, fileSize);
    }

    @Override
    public void run() {
        DownloadEntity info = getDownloadStartAndEnd();
        long start = info.getDownloadStartIndex();// 开始位置 = 已下载量
        long finishSize = start;
        long end = info.getDownloadEndIndex();
        update(info.getDoneSize());
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        InputStream in = null;
        try {
            connection = (HttpURLConnection) ((BaseDownloadManager) mProcessListener).connect(url);
            connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
            raf = new RandomAccessFile(mFile, "rws");
            raf.seek(start);
            int code = connection.getResponseCode();
            if (code == 206) {
                in = connection.getInputStream();//获取输入流，写入文件
                byte[] buf = new byte[1024 * 8];
                int len;
                while ((len = in.read(buf)) != -1) {
                    raf.write(buf, 0, len);
                    finishSize += len;
                    update(len);
                    if (isPause) {
                        info.setDownloadStartIndex(finishSize);
                        DatabaseFoctory.getInstance().update(info);
                        LogTools.e(TAG, "进入暂停");
                        synchronized (sync) {
                            try {
                                sync.wait();//暂停时该线程进入等待状态，并释放dao的锁
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //重新连接服务器，在wait时可能丢失了连接，如果不加这一段代码会出现connection。。。。。peer的错误
                            connection = (HttpURLConnection) ((BaseDownloadManager) mProcessListener).connect(url);
                            connection.setRequestProperty("Range", "bytes=" + finishSize + "-" + end);
                            raf.seek(finishSize);
                            in = connection.getInputStream();
                            LogTools.e(TAG, "恢复下载");
                        }
                    }
                    if (isCancel) {
                        cancelDownload(info);
                        return;
                    }
                    if (stop) {
                        info.setDownloadStartIndex(finishSize);
                        DatabaseFoctory.getInstance().update(info);
                        LogTools.e(TAG, "停止下载");
                        return;
                    }
                }
            }
            finishDownload();
        } catch (IOException e) {
            LogTools.e(TAG, e, "download failure url=" + url);
            downloadFailure(e.getMessage());
        } catch (Exception e) {
            LogTools.e(TAG, e, "download failure url=" + url);
            downloadFailure(e.getMessage());
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(raf);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


}
