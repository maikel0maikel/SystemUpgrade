package com.sinohb.system.upgrade.database;

import com.sinohb.system.upgrade.entity.DownloadInfo;

import java.util.List;

public interface DatabaseManagerable {

    boolean insert(DownloadInfo info);

    boolean update(DownloadInfo info);

    boolean delete(String url, int threadId);

    boolean delete(String url);

    boolean clear();

    DownloadInfo getDownloadInfo(String url, int threadId);

    List<DownloadInfo> getDownloadInfos(String url);


}
