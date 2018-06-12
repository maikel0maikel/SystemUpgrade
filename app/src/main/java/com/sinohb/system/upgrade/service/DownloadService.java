package com.sinohb.system.upgrade.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.entity.UpgradeEntity;
import com.sinohb.system.upgrade.presenter.DownloadController;
import com.sinohb.system.upgrade.presenter.DownloadPresenter;
import com.sinohb.system.upgrade.utils.NetWorkUtils;
import com.sinohb.system.upgrade.view.UpgradeDialog;

public class DownloadService extends Service implements DownloadPresenter.View {
    private NetWorkReceiver receiver;
    private static final String TAG = "DownloadService";
    private DownloadPresenter.Controller mPresenter;
    private UpgradeDialog upgradeDialog;
    private String remoteMd5;
    private static final String DOWNLOAD_URL = "ftp://183.62.139.91:40000/upgrade/HibosAndroidProject/SQ-L/SQ-L.txt";

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        registReceiver();
    }

    private void init() {
        new DownloadController(this);
        upgradeDialog = new UpgradeDialog(this,mPresenter);
    }

    private void registReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetWorkReceiver();
        registerReceiver(receiver, filter);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogTools.p(TAG,"服务已经销毁");
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        if (upgradeDialog != null && upgradeDialog.isShowing()) {
            upgradeDialog.dismiss();
        }
    }

    @Override
    public void downloadProcess(int process) {

    }

    @Override
    public void complete() {
        LogTools.p(TAG,"下载完成");
    }

    @Override
    public void failure() {
        LogTools.p(TAG,"下载失败");
    }

    @Override
    public void notifyFileName(String fileName) {
        LogTools.p(TAG,"下载的文件名："+fileName);
    }

    @Override
    public void notifyFileSize(String size) {
        LogTools.p(TAG,"下载的文件大小："+size);
    }

    @Override
    public void notifyTaskCanceled() {
        LogTools.p(TAG,"任务取消");
    }

    @Override
    public void notifyUpgradeInfo(UpgradeEntity entity) {
        if (entity != null) {
            upgradeDialog.setVersion("" + entity.getVersion());
            float fileSize = entity.getFileSize() / (1024.0f * 1024.0f);
            String resultSize = String.format("%.3f", fileSize);
            upgradeDialog.setVersionSize("" + resultSize + "M");
            upgradeDialog.setVersion(entity.getVersion());
            upgradeDialog.setUpdateContent(entity.getReleaseNotes());
            remoteMd5 = entity.getMD5();
        }else {
            LogTools.p(TAG,"notifyUpgradeInfo 获取为空");
        }
    }

    @Override
    public void notifyMD5(String md5) {
        if (md5 != null && md5.length() > 0 && md5.equalsIgnoreCase(remoteMd5)) {
            upgradeDialog.show();
        } else {
            LogTools.p(TAG,"md5校验不通过");
        }
    }

    @Override
    public void destroy() {
        stopSelf();
        LogTools.p(TAG,"销毁服务");
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void setPresenter(DownloadPresenter.Controller presenter) {
        mPresenter = presenter;
        mPresenter.start();
        LogTools.p(TAG,"setPresenter开始任务");
        startTask(NetWorkUtils.isNetWorkAvailable(this));
    }

    class NetWorkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                LogTools.p(TAG, "网络状态发生变化");
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    boolean isConnect = networkInfo != null && networkInfo.isConnected();
                    startTask(isConnect);
                } else {
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    Network[] networks = connMgr.getAllNetworks();
                    boolean isConnect = false;
                    for (int i = 0; i < networks.length; i++) {
                        NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                            LogTools.e(TAG, networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
                            isConnect = true;
                            break;
                        }
                    }
                    startTask(isConnect);
                }
            }
        }
    }

    private synchronized void startTask(boolean isConnect) {
        if (isConnect && !mPresenter.isTaskStart()) {
            LogTools.p(TAG, "网络已连接开始下载:" + DOWNLOAD_URL);
            mPresenter.download(DOWNLOAD_URL);
        } else if (mPresenter.isTaskStart()) {
            LogTools.p(TAG, "网络断开停止下载 isConnect:" + isConnect+",isTaskStart:"+mPresenter.isTaskStart());
            mPresenter.pause();
        }
    }
}
