package com.sinohb.system.upgrade.task;

import com.sinohb.logger.LogTools;
import com.sinohb.logger.utils.IOUtils;
import com.sinohb.system.upgrade.database.DatabaseFoctory;
import com.sinohb.system.upgrade.entity.DownloadInfo;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FTPDownloader extends BaseDownloader {
    private static final String TAG = "FTPDownloader";
    private String url;
    private File mFile;
    private long mDownloadSize;
    private int mThreadId;
    private DownloadProcessListener mListener;
    private boolean isPause = false;
    private boolean isCancel = false;
    private Object sync = new Object();

    public FTPDownloader(String url, File file, long downloadSize, int id, DownloadProcessListener processListener) {
        this.url = url;
        this.mFile = file;
        this.mThreadId = id;
        this.mDownloadSize = downloadSize;
        this.mListener = processListener;
    }

    @Override
    public void run() {
        DownloadInfo info = DatabaseFoctory.getInstance().getDownloadInfo(url, mThreadId);//dao.query(url.toString(), id);//查询记录当中没有下完的任务
        long finishSize = 0;
        if (info != null) {
            finishSize = info.getStartDownLoadIndex();
        } else {
            info = new DownloadInfo();
            info.setThreadId(mThreadId);
            info.setmUrl(url);
            info.setStartDownLoadIndex(0l);
            DatabaseFoctory.getInstance().insert(info);
        }
        long start = finishSize + (mThreadId - 1) * mDownloadSize;// 开始位置 = 已下载量
        finishSize = start;
        long end = mDownloadSize * mThreadId - 1;
        RandomAccessFile raf = null;
        FTPClient client = null;
        InputStream in = null;
        try {
            raf = new RandomAccessFile(mFile, "rws");
            raf.seek(start);
            client.setRestartOffset(start);
            byte[] buf = new byte[1024 * 8];
            int len;
            client = (FTPClient) ((com.sinohb.system.upgrade.task.factory.DownloadTask) mListener).connect();
            in = client.retrieveFileStream(url);
            while ((len = in.read(buf)) != -1) {
                if (finishSize + len >= end) {
                    len = (int) (end - finishSize);
                    raf.write(buf, 0, len);
                    update(len);
                    break;
                } else {
                    raf.write(buf, 0, len);
                    finishSize += len;
                    update(len);
                }
                if (isPause) {
                    info.setStartDownLoadIndex(finishSize);
                    DatabaseFoctory.getInstance().update(info);
                    LogTools.e(TAG, "进入暂停");
                    synchronized (sync) {
                        try {
                            sync.wait();//暂停时该线程进入等待状态，并释放dao的锁
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        client = (FTPClient) ((com.sinohb.system.upgrade.task.factory.DownloadTask) mListener).connect();
                        raf.seek(finishSize);
                        client.setRestartOffset(finishSize);
                        in = client.retrieveFileStream(url);
                        LogTools.e(TAG, "恢复下载");
                    }
                }
                if (isCancel) {
                    info.setStartDownLoadIndex(0l);
                    DatabaseFoctory.getInstance().update(info);
                    LogTools.e(TAG, "取消下载");
                    if (mListener != null) {
                        mListener.onTaskCancel(this);
                    }
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    private synchronized void update(long size) {
        if (mListener != null) {
            mListener.onDoneSize(size);
        }
    }

}
