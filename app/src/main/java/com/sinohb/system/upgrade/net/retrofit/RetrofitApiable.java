package com.sinohb.system.upgrade.net.retrofit;

import com.sinohb.system.upgrade.entity.UpgradeEntity;

import retrofit2.Callback;

public interface RetrofitApiable {

    void getUpgradeInfo(Callback<UpgradeEntity> callback);
   // void   getFixrate(String access_key,Callback<Test> callback);
}
