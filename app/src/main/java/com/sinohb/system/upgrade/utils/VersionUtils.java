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
}
