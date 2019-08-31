package com.google.firebase.codelab.mlkit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by ProgrammingKnowledge on 4/3/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "vehicle.db";
    public static final String TABLE_NAME = "vehicle_table";
    public static final String COL_1 = "number";
    public static final String COL_2 = "lastnumbers";
    public static final String COL_3 = "flag";
    public static final String COL_4 = "Name";
    public static final String COL_5 = "Address";
    public static final String COL_6 = "Contact";
    public static final String COL_7 = "dates";

    private SQLiteDatabase db;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (" + COL_1 + " TEXT PRIMARY KEY, " + COL_2 + " INTEGER, " + COL_3 + " INTEGER DEFAULT 1,"+ COL_4 + " TEXT, " + COL_5 + " TEXT, " + COL_6 + " TEXT, " + COL_7 + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public int insert(String table, ContentValues values) {
        return (int) db.insert(table, null, values);
    }

//    public void open(){
//        if(null == db || !db.isOpen()){
//            try{
//                db = this.getWritableDatabase();
//            }catch (SQLiteException sqLiteException){
//                System.out.println(sqLiteException.getMessage()+"devang");
//            }
//        }
//    }
//
//    public void close(){
//        if(db != null){
//            db.close();
//        }
//    }

    public boolean insertData(String number, int lastnumbers, int flag, String field1, String field2, String field3, String dates) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,number);
        contentValues.put(COL_2,lastnumbers);
        contentValues.put(COL_3,flag);
        contentValues.put(COL_4,field1);
        contentValues.put(COL_5,field2);
        contentValues.put(COL_6,field3);
        contentValues.put(COL_7,dates);


        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    public boolean updatefields(String field1, String field2, String field3,String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_4,field1);
        contentValues.put(COL_5,field2);
        contentValues.put(COL_6,field3);
        db.update(TABLE_NAME, contentValues, "number = ?",new String[] { number });
        return true;
    }

    //method to update flag from new to old i.e 1 to 0
    public boolean updateFlag(String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_3, 0);

        db.update(TABLE_NAME, contentValues, "number = ?",new String[] {number});
        return true;
    }

    //method to add list of dates into dates column
    public boolean updateDates(String number, String dates)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_7, dates);

        db.update(TABLE_NAME, contentValues, "number = ?",new String[] {number});
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }

    public Cursor numberOfV(int lastnumbers){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + TABLE_NAME + " where " + COL_2 + " = " +lastnumbers;
        Cursor cursor = db.rawQuery(query, null);

        return cursor;
    }

//    public boolean deleteCol(String col)
//    {
//        SQLiteDatabase db= this.getWritableDatabase();
//        String query = "PRAGMA foreign_keys=off;" +
//                "BEGIN TRANSACTION;" +
//                "ALTER TABLE " + TABLE_NAME + " RENAME TO _TABLE_old;" +
//                "create table " + TABLE_NAME +" (number TEXT PRIMARY KEY, lastnumbers INTEGER, flag INTEGER DEFAULT 1, field1 TEXT, field2 TEXT, field3 TEXT, dates TEXT)";
//
//    }

    public boolean emptyColumn(String col){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(col, "");

        db.update(TABLE_NAME, contentValues, "number <> ?",new String[] {""});
        return true;
    }

    public boolean checkIfExists(String ns){
        SQLiteDatabase db= this.getWritableDatabase();
        String query = "Select * from " + TABLE_NAME + " where " + COL_1 + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{ns});
        System.out.println(cursor.getCount()+"checkcursor");
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;


    }
}
