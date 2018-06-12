package com.sinohb.system.upgrade.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.UpgradeAppclition;
import com.sinohb.system.upgrade.entity.DownloadEntity;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager implements DatabaseManagerable {

    private static final String TAG = "DatabaseManager";
    DatabaseHelper databaseHelper = new DatabaseHelper(UpgradeAppclition.getContext());

    @Override
    public boolean insert(DownloadEntity info) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.execSQL("insert into " + DatabaseHelper.TABLE_NAME + "(url,threadId,startIndex) values(?,?,?)", new Object[]{info.getmUrl(), info.getThreadId(), info.getDownloadStartIndex()});
            //db.close();
            return true;
        } catch (Exception e) {
            LogTools.e(TAG, e);
        }
        return false;
    }

    @Override
    public boolean update(DownloadEntity info) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_NAME + "  SET startIndex=? WHERE url=? AND threadId=?", new Object[]{ info.getDownloadStartIndex(),info.getmUrl(), info.getThreadId()});
           // db.close();
            return true;
        } catch (Exception e) {
            LogTools.e(TAG, e);
        }
        return false;
    }

    @Override
    public boolean delete(String url, int threadId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        try {
            db.execSQL("Delete from " + DatabaseHelper.TABLE_NAME + "  WHERE url=? AND threadId=?", new Object[]{url, threadId});
            //db.close();
            return true;
        } catch (Exception e) {
            LogTools.e(TAG, e);
        }
        return false;
    }

    @Override
    public boolean delete(String url) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.execSQL("Delete from " + DatabaseHelper.TABLE_NAME + "  WHERE url=? ", new Object[]{url});
            //db.close();
            return true;
        } catch (Exception e) {
            LogTools.e(TAG, e);
        }
        return false;
    }

    @Override
    public boolean clear() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.execSQL("Delete from " + DatabaseHelper.TABLE_NAME);
            //db.close();
            return true;
        } catch (Exception e) {
            LogTools.e(TAG, e);
        }
        return false;
    }

    @Override
    public DownloadEntity getDownloadInfo(String url, int threadId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT url, threadId, startIndex FROM " + DatabaseHelper.TABLE_NAME + "  WHERE url=? AND threadId=?", new String[]{url, String.valueOf(threadId)});
        if (cursor != null) {
            if (cursor.moveToNext()) {
                DownloadEntity downloadEntity = new DownloadEntity();
                downloadEntity.setmUrl(cursor.getString(0));
                downloadEntity.setThreadId(cursor.getInt(1));
                downloadEntity.setDownloadStartIndex(cursor.getLong(2));
                return downloadEntity;
            }
            cursor.close();
        }
       // db.close();

        return null;
    }

    @Override
    public List<DownloadEntity> getDownloadInfos(String url) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT url, threadId, startIndex FROM " + DatabaseHelper.TABLE_NAME + "  WHERE url=? ", new String[]{url});
        if (cursor != null) {
            int count = cursor.getCount();
            List<DownloadEntity> downloadEntities = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                DownloadEntity downloadEntity = new DownloadEntity();
                downloadEntity.setmUrl(cursor.getString(0));
                downloadEntity.setThreadId(cursor.getInt(1));
                downloadEntity.setDownloadStartIndex(cursor.getLong(2));
                downloadEntities.add(downloadEntity);
            }
            cursor.close();
            return downloadEntities;
        }
        //db.close();
        return null;
    }

}
