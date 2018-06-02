package com.sinohb.system.upgrade.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "upgrade.db";
    private static final int VERSION = 1;
    public static final String TABLE_NAME = "downloadInfo";

    public DatabaseHelper(Context context){
        this(context,DB_NAME,null,VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE if not exists "+TABLE_NAME+"(url text,threadId integer,startIndex integer,primary key(url,threadId))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
//
//    public static class Columns{
//        public static final String URL = "url";
//        public static final String THEAD_ID = "threadId";
//        public static final String FINISH_SIZE = "startIndex";
//    }

}
