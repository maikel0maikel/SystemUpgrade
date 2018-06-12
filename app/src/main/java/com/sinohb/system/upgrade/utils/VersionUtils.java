package com.sinohb.system.upgrade.utils;


import com.sinohb.logger.LogTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VersionUtils {
    private static final String TAG = "VersionUtils";

    private VersionUtils() {
    }

    public static String getOemVersion() {
        String oemVersion = "";
        String path = "/oem/version.txt";
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader
                            = new InputStreamReader(instream, "utf-8");
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line = "";
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        oemVersion += line;
                    }
                    inputreader.close();
                    instream.close();       //关闭输入流
                }
            } catch (java.io.FileNotFoundException e) {
                LogTools.p(TAG, "The File doesn't not exist.");
            } catch (IOException e) {
                LogTools.p(TAG, e.getMessage());
            }
        }
        return oemVersion;
    }

    public static int getVersion(String oemVersion) {
        //huabao-sq-h-v126-20180330
        String[] rex = oemVersion.split("-");
        String version = "0";
        for (String v : rex) {
            if (v.startsWith("v")) {
                version = v.substring(1);
                break;
            }
        }
        int v = 0;
        try {
            v = Integer.parseInt(version);
        }catch (Exception e){
            LogTools.e(TAG,e,"版本获取失败");
        }
        return v;
    }

}
