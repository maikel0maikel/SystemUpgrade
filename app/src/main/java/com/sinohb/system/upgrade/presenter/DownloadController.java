package com.sinohb.system.upgrade.presenter;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.sinohb.logger.LogTools;
import com.sinohb.logger.utils.FileUtils;
import com.sinohb.system.upgrade.UpgradeAppclition;
import com.sinohb.system.upgrade.constant.UpgradeConstants;
import com.sinohb.system.upgrade.downloader.task.VertifyTask;
import com.sinohb.system.upgrade.entity.UpgradeEntity;
import com.sinohb.system.upgrade.pool.ThreadPool;
import com.sinohb.system.upgrade.downloader.DownloadListener;
import com.sinohb.system.upgrade.downloader.manager.BaseDownloadManager;
import com.sinohb.system.upgrade.downloader.manager.SimpleDownloadFactory;
import com.sinohb.system.upgrade.utils.JsonUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class DownloadController implements DownloadPresenter.Controller, DownloadListener {
    private static final String TAG = "DownloadController";
    private BaseDownloadManager downloadTask;
    private DownloadPresenter.View view;
    private UpdateHandler mHandler;
    private boolean isTaskStart = false;
    private boolean isPause = false;
    private String storePath;
    private int mTryTimes = 0;
    private String remoteMd5;

    @Override
    public boolean isTaskStart() {
        return isTaskStart;
    }

    @Override
    public boolean isPause() {
        return downloadTask == null ? false : downloadTask.isPause();
    }

    @Override
    public void update() {
        sendUpdateBroadcast();
        destroyView();
    }

    private void destroyView() {
        if (view != null) {
            view.destroy();
        }
    }

    private void sendUpdateBroadcast() {
        if(storePath == null){
            storePath = downloadTask.getDownloadFilePath();
        }
        if (storePath == null||storePath.length() ==0){
            LogTools.p(TAG,"路径为空删除文件夹并重新尝试");
            FileUtils.delete(new File(UpgradeConstants.DOWNLOAD_PATH));
            onFailure("storePath is null");
            return;
        }
        Intent updateIntent = new Intent(UpgradeConstants.ACTION_PACKAGE);
        updateIntent.putExtra(UpgradeConstants.ACTION_PACKAGE_EXTRA_PATH, storePath);
        updateIntent.putExtra(UpgradeConstants.ACTION_PACKAGE_EXTRA_NOW, true);
        UpgradeAppclition.getContext().sendBroadcast(updateIntent);
    }

    public DownloadController(DownloadPresenter.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void start() {
        mHandler = new UpdateHandler(this);
        reset();
    }

    @Override
    public void onDestroy() {
        if (downloadTask != null) {
            if (isPause) {
                downloadTask.resume();
            }
        }
        downloadTask.stop();
        ThreadPool.getPool().destroy();
        mHandler.removeCallbacksAndMessages(null);
        isTaskStart = false;
    }

    @Override
    public void pause() {
        if (isTaskStart && !isPause && downloadTask != null) {
            downloadTask.pause();
            isPause = true;
        }
    }

    @Override
    public void resume() {
        if (isTaskStart && isPause && downloadTask != null) {
            downloadTask.resume();
            isPause = false;
        }

    }

    @Override
    public void cancel() {
        if (isTaskStart && !isPause && downloadTask != null) {
            downloadTask.cancel();
            //reset();
        }
    }

    @Override
    public void download(String url) {
        if (isTaskStart) {
            LogTools.e(TAG, "任务没有去下载：isTaskStart=" + isTaskStart);
            return;
        }
        startDownload(url);
    }

    private void startDownload(String url) {
        if (downloadTask == null) {
            downloadTask = SimpleDownloadFactory.createTask(url);
            downloadTask.setListener(this);
        }
        realStartDownloadTask();
    }

    private void realStartDownloadTask() {
        if (downloadTask == null) {
            LogTools.e(TAG, "任务为空请检查");
            return;
        }
        isTaskStart = true;
        ThreadPool.getPool().execute(new FutureTask<UpgradeEntity>(downloadTask) {
            @Override
            protected void done() {
                super.done();
                try {
                    UpgradeEntity entity = get();
                    if (entity != null) {
                        remoteMd5 = entity.getMD5();
                        LogTools.p(TAG, "从服务器获取的下载信息:" + JsonUtils.toJson(entity));
                    } else {
                        LogTools.p(TAG, "从服务器获取的下载信息:null");
                    }
                } catch (InterruptedException e) {
                    LogTools.p(TAG, e, "从服务器获取的下载信息出错");
                } catch (ExecutionException e) {
                    LogTools.p(TAG, e, "从服务器获取的下载信息出错");
                }
            }
        });
    }

    @Override
    public void taskCanceled() {
        LogTools.e(TAG, "taskCanceled");
        mHandler.sendEmptyMessage(UpdateHandler.MSG_TASK_CANCELED);
        reset();
    }

    private void reset() {
        isTaskStart = false;
        isPause = false;
        mTryTimes = 0;
    }

    @Override
    public void onFinish(String downloadFilePath) {
        LogTools.e(TAG, "onFinish");
        storePath = downloadFilePath;
        vertifyFile(downloadFilePath);
    }

    private void vertifyFile(final String downloadFilePath) {
        ThreadPool.getPool().execute(new FutureTask<String>(new VertifyTask(downloadFilePath)) {
            @Override
            protected void done() {
                super.done();
                String MD5 = "";
                try {
                    MD5 = get();
                    LogTools.p(TAG, "MD5=" + MD5);
                    if (MD5 != null && MD5.length() > 0 && MD5.equalsIgnoreCase(remoteMd5)) {
                        mHandler.sendEmptyMessage(UpdateHandler.MSG_VERTIFY_MD5_OK);
                        LogTools.p(TAG, "md5校验通过弹出升级框");
                    } else {
                        File file = new File(downloadFilePath);
                        LogTools.p(TAG, "md5校验不通过删除下载文件重新开始下载大小：" + file.length() + ",路径：downloadFilePath=" + downloadFilePath);
                        file.delete();
                        //reset();
                        onFailure("md5校验不通过");
                    }
                } catch (InterruptedException e) {
                    LogTools.e(TAG, e, "获取文件md5失败路径：" + downloadFilePath);
                    onFailure("获取文件md5失败");
                } catch (ExecutionException e) {
                    LogTools.e(TAG, e, "获取文件md5失败路径：" + downloadFilePath);
                    onFailure("获取文件md5失败");
                }
            }
        });
    }

    @Override
    public void onFailure(String error) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        mTryTimes++;
        if (mTryTimes == 3) {
            mHandler.sendEmptyMessage(UpdateHandler.MSG_DOWNLOAD_FAILURE);
            reset();
            LogTools.e(TAG, "尝试3次下载失败退出下载服务" + error);
        } else {
            LogTools.p(TAG, "下载失败尝试第" + mTryTimes + "次");
            realStartDownloadTask();
        }
    }

    @Override
    public synchronized void onProgress(int progress) {
        LogTools.i(TAG, "onProgress:" + progress);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_PROGRESS, progress).sendToTarget();
    }

    @Override
    public void onUpgradeInfo(UpgradeEntity upgradeEntity) {
        if (upgradeEntity != null) {
//            onFileName(upgradeEntity.getFileName());
//            onFileSize(upgradeEntity.getFileSize());
            mHandler.obtainMessage(UpdateHandler.MSG_DOWNLOAD_INFO, upgradeEntity).sendToTarget();
        }

    }

    private void onFileSize(long size) {
        float fileSize = size / (1024.0f * 1024.0f);
        LogTools.p(TAG, "fileSize:" + fileSize);
        String resultSize = String.format("%.5f", fileSize);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_SIZE, resultSize).sendToTarget();
    }

    private void onFileName(String name) {
        LogTools.e(TAG, "onFileName:" + name);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_NAME, name).sendToTarget();
    }

    @Override
    public void onTaskComplete() {
        mHandler.sendEmptyMessage(UpdateHandler.MSG_TASK_FINISHED);
        reset();
    }

    @Override
    public void onTaskCancled() {
        mHandler.sendEmptyMessage(UpdateHandler.MSG_TASK_CANCELED);
        LogTools.p(TAG, "onTaskCancled");
        reset();
    }

    @Override
    public void onNoNewVersion() {
        destroyView();
        reset();
    }

    @Override
    public void onDirectUpdate(String filePath) {
        storePath = filePath;
        mHandler.sendEmptyMessage(UpdateHandler.MSG_DIRECT_UPDATE);
    }

    private static class UpdateHandler extends Handler {
        static final int MSG_FILE_NAME = 1;
        static final int MSG_FILE_SIZE = 2;
        static final int MSG_FILE_PROGRESS = 3;
        static final int MSG_TASK_CANCELED = 4;
        static final int MSG_TASK_FINISHED = 5;
        static final int MSG_DOWNLOAD_COMPLETE = 6;
        static final int MSG_DOWNLOAD_FAILURE = 7;
        static final int MSG_DOWNLOAD_INFO = 8;
        static final int MSG_VERTIFY_MD5_OK = 9;
        static final int MSG_DIRECT_UPDATE = 10;
        private WeakReference<DownloadController> controllerWeakReference;

        UpdateHandler(DownloadController controller) {
            controllerWeakReference = new WeakReference<>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (controllerWeakReference == null) {
                LogTools.e(TAG, "controllerWeakReference is null");
                return;
            }
            DownloadController controller = controllerWeakReference.get();
            if (controller == null) {
                LogTools.e(TAG, "controller is null");
                return;
            }
            switch (msg.what) {
                case MSG_FILE_NAME:
                    String fileName = (String) msg.obj;
                    controller.view.notifyFileName(fileName);
                    break;
                case MSG_FILE_SIZE:
                    String fileSize = (String) msg.obj;
                    controller.view.notifyFileSize(fileSize);
                    break;
                case MSG_FILE_PROGRESS:
                    int progress = (int) msg.obj;
                    controller.view.downloadProcess(progress);
                    break;
                case MSG_TASK_CANCELED:
                    controller.view.notifyTaskCanceled();
                    break;
                case MSG_TASK_FINISHED:
                    controller.view.complete();
                    break;
                case MSG_DOWNLOAD_COMPLETE:
                    controller.view.complete();
                    break;
                case MSG_DOWNLOAD_FAILURE:
                    controller.view.failure();
                    break;
                case MSG_DOWNLOAD_INFO:
                    UpgradeEntity upgradeEntity = (UpgradeEntity) msg.obj;
                    controller.view.notifyUpgradeInfo(upgradeEntity);
                    break;
                case MSG_VERTIFY_MD5_OK:
                    controller.view.notifyVertifyOK();
                    controller.reset();
                    break;
                case MSG_DIRECT_UPDATE:
                    controller.view.updateDirectly();
                    controller.reset();
                    break;
            }
        }
    }
}
