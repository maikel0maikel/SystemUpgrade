package com.sinohb.system.upgrade.net.retrofit;

import com.sinohb.system.upgrade.entity.UpgradeEntity;

public class RetrofitFactory {
    private static RetrofitFactory factory = new RetrofitFactory();
    private RetrofitApiable retrofitApiable;
    private RetrofitFactory(){retrofitApiable = new RetrofitApiImpl();}
    public static RetrofitFactory getFactory(){
        return factory;
    }
    public  void getUpgradeInfo(retrofit2.Callback<UpgradeEntity> callback){
        retrofitApiable.getUpgradeInfo(callback);
    }
//    public void   getFixrate(String access_key,retrofit2.Callback<Test> callback){
//        retrofitApiable.getFixrate(access_key,callback);
//    }
}
