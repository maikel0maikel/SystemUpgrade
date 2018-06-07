package com.sinohb.system.upgrade.net.okhttp;

import java.io.File;
import java.util.Map;

import okhttp3.Callback;

public class OkhttpFactory {
    private static OkhttpFactory factory = new OkhttpFactory();
    private IOkhttpManager okhttpManager;
    private OkhttpFactory(){okhttpManager = new OkhttpManagerImpl();}
    public static OkhttpFactory getFactory(){
        return factory;
    }
    public void doGet(String url, Callback callback){
        okhttpManager.doGet(url,callback);
    }

    public void doPost(String url, Map<String, String> params, Callback callback){
        okhttpManager.doPost(url,params,callback);
    }

    public void uploadPic(String url, File file, String fileName){
        okhttpManager.uploadPic(url,file,fileName);
    }

    public  void doPostJson(String url, String jsonParams, Callback callback){
        okhttpManager.doPostJson(url,jsonParams,callback);
    }
}
