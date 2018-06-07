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
import com.sinohb.system.upgrade.presenter.DownloadController;
import com.sinohb.system.upgrade.presenter.DownloadPresenter;
import com.sinohb.system.upgrade.view.UpgradeDialog;

public class DownloadService extends Service implements DownloadPresenter.View {
    private NetWorkReceiver receiver;
    private static final String TAG = "DownloadService";
    private DownloadPresenter.Controller mPresenter;
    private UpgradeDialog upgradeDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        registReceiver();
        new DownloadController(this);
        upgradeDialog = new UpgradeDialog(this);
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
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }

    }

    @Override
    public void downloadProcess(int process) {

    }

    @Override
    public void complete() {
        upgradeDialog.show();
    }

    @Override
    public void failure() {

    }

    @Override
    public void notifyFileName(String fileName) {
    }

    @Override
    public void notifyFileSize(float size) {

    }

    @Override
    public void notifyTaskCanceled() {

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
    }

    class NetWorkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                LogTools.e(TAG, "网络状态发生变化");
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    boolean isConnect = networkInfo != null && networkInfo.isConnectedOrConnecting();
                    startTask(isConnect);
//                    NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//                    NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//                    if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                        LogTools.e(TAG, "WIFI已连接,移动数据已连接");
//                    } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
//                        LogTools.e(TAG, "WIFI已连接,移动数据已断开");
//                    } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                        LogTools.e(TAG, "WIFI已断开,移动数据已连接");
//                    } else {
//                        LogTools.e(TAG, "WIFI已断开,移动数据已断开");
//                    }
                } else {
                    //获得ConnectivityManager对象
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    Network[] networks = connMgr.getAllNetworks();
                    StringBuilder sb = new StringBuilder();
                    //通过循环将网络信息逐个取出来
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

        private void startTask(boolean isConnect) {
            if (isConnect) {
                mPresenter.download("http://downloadz.dewmobile.net/Official/Kuaiya482.apk");
            } else if (mPresenter.isTaskStart()) {
                mPresenter.pause();
            }
        }
    }

}
