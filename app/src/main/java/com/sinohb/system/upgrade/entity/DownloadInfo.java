package com.sinohb.system.upgrade.entity;

public class DownloadInfo {

    private long mStartDownLoadIndex;//已经下载大小

    private String mFileName;//文件名

    private String mUrl;//下载地址

    private int mFileSize;//文件大小

    private int mThreadId;

    public long getStartDownLoadIndex() {
        return mStartDownLoadIndex;
    }

    public void setStartDownLoadIndex(long mStartDownLoadIndex) {
        this.mStartDownLoadIndex = mStartDownLoadIndex;
    }

    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public int getmFileSize() {
        return mFileSize;
    }

    public void setmFileSize(int mFileSize) {
        this.mFileSize = mFileSize;
    }

    public int getThreadId() {
        return mThreadId;
    }

    public void setThreadId(int mThreadId) {
        this.mThreadId = mThreadId;
    }
}
