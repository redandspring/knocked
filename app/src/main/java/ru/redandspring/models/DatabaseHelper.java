package ru.redandspring.models;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Alexander on 29.03.2016.
 */
public abstract class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

    public static String LOG_TAG = "TAG-DB";
    // имя базы данных
    private static final String DATABASE_NAME = "mydatabase.db";
    // версия базы данных
    private static final int DATABASE_VERSION = 2;
    // база данных
    protected static SQLiteDatabase db;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        open();
    }

    private final void open(){

        if (db == null) {

            try {
                db = this.getWritableDatabase();
            } catch (SQLiteException ex) {
                db = this.getReadableDatabase();
            }
        }
    }
}