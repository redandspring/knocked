package ru.redandspring.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Alexander on 11.04.2016.
 */
public class OrderModel extends DatabaseHelper {

    // имя таблицы
    public static final String DATABASE_TABLE = "orders";

    // названия столбцов
    public static final String ORDER_ID_COLUMN = "order_id";
    public static final String MESSAGE_COLUMN = "message";

    public class RowOrderModel {
        public Integer orderID;
        public String message;

        public RowOrderModel(Integer orderID, String message) {
            this.orderID = orderID;
            this.message = message;
        }
    }

    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + MESSAGE_COLUMN
            + " text not null, " + ORDER_ID_COLUMN + " integer not null unique);";

    private static final String SQL_FIND_ALL_LAST = "SELECT * FROM "
            + DATABASE_TABLE + " ORDER BY " + ORDER_ID_COLUMN + " DESC "
            + " LIMIT 30";

    public OrderModel(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlitedb) {
        sqlitedb.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlitedb, int oldVersion, int newVersion) {
        // Запишем в журнал
        Log.w(LOG_TAG, "Обновляемся с версии " + oldVersion + " на версию " + newVersion);

        // Удаляем старую таблицу и создаём новую
        sqlitedb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        // Создаём новую таблицу
        onCreate(sqlitedb);
    }

    public void insert(Integer ID, String txt){
        ContentValues values = new ContentValues();
        values.put(ORDER_ID_COLUMN, ID);
        values.put(MESSAGE_COLUMN, txt);
        db.insert(DATABASE_TABLE, null, values);
    }

    public RowOrderModel findOrderById(Integer ID){

        Cursor cursor = db.query(DATABASE_TABLE,
                new String[]{ORDER_ID_COLUMN, MESSAGE_COLUMN},
                ORDER_ID_COLUMN + " = ?",
                new String[]{Integer.toString(ID)},
                null,
                null,
                null);

        try {
            if (cursor.getCount() == 0) return null;

            cursor.moveToFirst();
            return putRow(cursor);

        }
        finally {
            cursor.close();
        }

    }

    public ArrayList<RowOrderModel> findOrdersLastAll(){

        ArrayList<RowOrderModel> list = new ArrayList<>();

        Cursor cursor = db.rawQuery(SQL_FIND_ALL_LAST, new String[]{});

        try {
            if (cursor.getCount() == 0) return null;

            // cursor.moveToFirst();
            while (cursor.moveToNext()) {
                list.add(putRow(cursor));
            }
            return list;
        }
        finally {
            cursor.close();
        }
    }

    private RowOrderModel putRow(Cursor cursor){
        return new RowOrderModel(
                cursor.getInt(cursor.getColumnIndex(ORDER_ID_COLUMN)),
                cursor.getString(cursor.getColumnIndex(MESSAGE_COLUMN))
        );
    }
}
