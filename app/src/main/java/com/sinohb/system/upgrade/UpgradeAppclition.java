package com.sinohb.system.upgrade;

import com.sinohb.logger.SystemApplication;

public class UpgradeAppclition extends SystemApplication{
    @Override
    public void onCreate() {
        super.onCreate();
        getLogger().setDebug(true);
    }
}
