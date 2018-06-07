package com.sinohb.system.upgrade.net.retrofit;

import com.sinohb.system.upgrade.entity.Test;
import com.sinohb.system.upgrade.entity.UpgradeInfo;

import retrofit2.Callback;

public interface RetrofitApiable {

    void getUpgradeInfo(Callback<UpgradeInfo> callback);
    void   getFixrate(String access_key,Callback<Test> callback);
}
