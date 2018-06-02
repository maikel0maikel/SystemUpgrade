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

public class DownloadService extends Service {
    private NetWorkReceiver receiver;
    private static final String TAG = "DownloadService";

    @Override
    public void onCreate() {
        super.onCreate();
        registReceiver();
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
                    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {

                    }
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
                    for (int i = 0; i < networks.length; i++) {
                        NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                            LogTools.e(TAG, networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
                            return;
                        }
                    }
                }
            }
        }
    }

}
