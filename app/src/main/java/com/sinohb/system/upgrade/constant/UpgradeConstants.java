package com.sinohb.system.upgrade.constant;

import android.os.Environment;

public class UpgradeConstants {

    public final static String ACTION_PACKAGE = "com.marsir.upgrade.PACKAGE";	//广播名称
    public final static String ACTION_PACKAGE_EXTRA_PATH = "path";			//广播参数，String，文件路径
    public final static String ACTION_PACKAGE_EXTRA_NOW = "now";			//广播参数，Bool，是否立即重启，flase为关机时升级

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/HBSystem/download/";
    private UpgradeConstants(){}
}
