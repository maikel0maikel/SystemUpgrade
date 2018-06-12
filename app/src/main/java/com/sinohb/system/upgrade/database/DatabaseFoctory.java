package com.sinohb.system.upgrade.database;

import com.sinohb.system.upgrade.entity.DownloadEntity;

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

    public boolean insert(DownloadEntity info) {
        return manager.insert(info);
    }

    public boolean update(DownloadEntity info) {
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

    public DownloadEntity getDownloadInfo(String url, int threadId) {
        return manager.getDownloadInfo(url,threadId);
    }

    public List<DownloadEntity> getDownloadInfos(String url) {
        return manager.getDownloadInfos(url);
    }
}
