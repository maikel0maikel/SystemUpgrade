package com.sinohb.system.upgrade.database;

import com.sinohb.system.upgrade.entity.DownloadInfo;

import java.util.List;

public class DatabaseFoctory {
    private static DatabaseFoctory factory = null;
    private DatabaseManagerable manager;
    public static DatabaseFoctory getInstance(){
        if (factory == null){
            synchronized (DatabaseFoctory.class){
                if (factory == null){
                    factory = new DatabaseFoctory();
                }
            }
        }
        return factory;
    }

    private DatabaseFoctory(){
        manager = new DatabaseManager();
    }

    public boolean insert(DownloadInfo info) {
        return manager.insert(info);
    }

    public boolean update(DownloadInfo info) {
        return manager.update(info);
    }

    public boolean delete(String url, int threadId) {
        return manager.delete(url,threadId);
    }

    public boolean delete(String url) {
        return manager.delete(url);
    }

    public boolean clear() {
        return manager.clear();
    }

    public DownloadInfo getDownloadInfo(String url, int threadId) {
        return manager.getDownloadInfo(url,threadId);
    }

    public List<DownloadInfo> getDownloadInfos(String url) {
        return manager.getDownloadInfos(url);
    }
}
