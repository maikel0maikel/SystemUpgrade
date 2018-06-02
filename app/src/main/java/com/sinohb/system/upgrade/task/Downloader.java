package com.sinohb.system.upgrade.task;

import com.sinohb.logger.LogTools;
import com.sinohb.logger.utils.IOUtils;
import com.sinohb.logger.utils.LogUtils;
import com.sinohb.system.upgrade.database.DatabaseFoctory;
import com.sinohb.system.upgrade.entity.DownloadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader implements Runnable {
    private static final String TAG = "Downloader";
    private int mTheadId;
    private File mFile;
    private long downloadSize;
    private String url;
    private boolean isPause = false;
    private boolean isCancel = false;
    private Object sync = new Object();
    private boolean isFinish = false;
    private DownloadProcessListener mProcessListener;

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        synchronized (sync) {
            isPause = pause;
            sync.notify();
        }

    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public Downloader(String url, File file, long downloadSize, int id, DownloadProcessListener processListener) {
        this.mFile = file;
        this.downloadSize = downloadSize;
        this.mTheadId = id;
        this.url = url;
        this.mProcessListener = processListener;
    }

    @Override
    public void run() {
        DownloadInfo info = DatabaseFoctory.getInstance().getDownloadInfo(url, mTheadId);//dao.query(url.toString(), id);//查询记录当中没有下完的任务
        long finishSize = 0;
        if (info != null) {
            finishSize = info.getStartDownLoadIndex();
        } else {
            info = new DownloadInfo();
            info.setThreadId(mTheadId);
            info.setmUrl(url);
            info.setStartDownLoadIndex(0l);
            DatabaseFoctory.getInstance().insert(info);
        }
        long start = finishSize + (mTheadId - 1) * downloadSize;// 开始位置 = 已下载量
        finishSize = start;
        long end = downloadSize * mTheadId - 1;
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        InputStream in = null;
        try {
            URL downloadUrl = new URL(url);
            connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
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
                        info.setStartDownLoadIndex(finishSize);
                        DatabaseFoctory.getInstance().update(info);
                        LogTools.e(TAG, "进入暂停");
                        synchronized (sync) {
                            try {
                                sync.wait();//暂停时该线程进入等待状态，并释放dao的锁
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            downloadUrl = new URL(url);//重新连接服务器，在wait时可能丢失了连接，如果不加这一段代码会出现connection。。。。。peer的错误
                            connection = (HttpURLConnection) downloadUrl.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(5000);
                            connection.setRequestProperty("Range", "bytes=" + finishSize + "-" + end);
                            raf.seek(finishSize);
                            in = connection.getInputStream();
                            LogTools.e(TAG, "恢复下载");
                        }
                    }
                    if (isCancel) {
                        info.setStartDownLoadIndex(0l);
                        DatabaseFoctory.getInstance().update(info);
                        isFinish = true;
                        LogTools.e(TAG, "取消下载");
                        if (mProcessListener!=null){
                            mProcessListener.onTaskCancel(this);
                        }
                        return;
                    }
                }
            }
            isFinish = true;
            DatabaseFoctory.getInstance().delete(url, mTheadId);
            LogTools.e(TAG, "下载任务完成：" + Thread.currentThread().getName());
            if (mProcessListener!=null){
                mProcessListener.onTaskFinished(this);
            }
        } catch (IOException e) {
            LogTools.e(TAG, "下载失败：" + e.getMessage());
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(raf);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private synchronized void update(long size) {
        if (mProcessListener != null) {
            mProcessListener.onDoneSize(size);
        }
    }

    public interface DownloadProcessListener {
        void onDoneSize(long size);
        void onTaskFinished(Downloader downloader);
        void onTaskCancel(Downloader downloader);
    }
}
