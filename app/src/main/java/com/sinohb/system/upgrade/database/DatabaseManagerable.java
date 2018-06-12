package com.sinohb.system.upgrade.database;

import com.sinohb.system.upgrade.entity.DownloadEntity;

import java.util.List;

public interface DatabaseManagerable {

    boolean insert(DownloadEntity info);

    boolean update(DownloadEntity info);

    boolean delete(String url, int threadId);

    boolean delete(String url);

    boolean clear();

    DownloadEntity getDownloadInfo(String url, int threadId);

    List<DownloadEntity> getDownloadInfos(String url);


}
