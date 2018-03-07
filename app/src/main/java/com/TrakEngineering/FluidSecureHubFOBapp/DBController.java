package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DBController extends SQLiteOpenHelper {
    private static final String LOGCAT = null;

    public DBController(Context applicationcontext) {
        super(applicationcontext, "FuelSecureTrak.db", null, 1);
        Log.d(LOGCAT, "Created");
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        String query = "CREATE TABLE Tbl_FSTrak ( Id INTEGER PRIMARY KEY, jsonData TEXT, authString TEXT)";
        database.execSQL(query);

        //table for updateTranasaction Status
        String query1 = "CREATE TABLE Tb2_FSTrak ( Id INTEGER PRIMARY KEY, jsonData TEXT, authString TEXT)";
        database.execSQL(query1);

        String query2 = "CREATE TABLE Tbl_FSTransStatus ( Id INTEGER PRIMARY KEY, transId TEXT, transStatus TEXT)";
        database.execSQL(query2);

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        String query,query1;
        query = "DROP TABLE IF EXISTS Tbl_FSTrak";
        database.execSQL(query);
        onCreate(database);

        query1 = "DROP TABLE IF EXISTS Tb2_FSTrak";
        database.execSQL(query1);
        onCreate(database);
    }

    public long insertTransactions(HashMap<String, String> queryValues) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("jsonData", queryValues.get("jsonData"));
        values.put("authString", queryValues.get("authString"));

        long insertedID=database.insert("Tbl_FSTrak", null, values);
        database.close();

        return  insertedID;
    }

    public int updateTransactions(HashMap<String, String>  queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("jsonData", queryValues.get("jsonData"));
        values.put("authString", queryValues.get("authString"));

        return database.update("Tbl_FSTrak", values, "Id" + " = ?", new String[] { queryValues.get("sqliteId") });
    }

    public void deleteTransStatusByTransID(String TransID) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  Tbl_FSTransStatus where transId='" + TransID + "'";
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
    }

    public long insertTransStatus(HashMap<String, String> queryValues) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("transId", queryValues.get("transId"));
        values.put("transStatus", queryValues.get("transStatus"));

        long insertedID=database.insert("Tbl_FSTransStatus", null, values);

        database.close();

        return  insertedID;
    }

    public void deleteTransactions(String id) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  Tbl_FSTrak where Id='" + id + "'";
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
    }


    public ArrayList<HashMap<String, String>> getAllTransaction() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM Tbl_FSTrak";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Id", cursor.getString(0));
                map.put("jsonData", cursor.getString(1));
                map.put("authString", cursor.getString(2));

                wordList.add(map);
            } while (cursor.moveToNext());
        }
        return wordList;
    }

    //=========================Update Transaction Status================

    public void insertIntoUpdateTranStatus (HashMap<String, String> queryValues) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("jsonData", queryValues.get("jsonData"));
        values.put("authString", queryValues.get("authString"));

        database.insert("Tb2_FSTrak", null, values);
        database.close();
    }

    public void deleteTranStatus(String id) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery1 = "DELETE FROM  Tb2_FSTrak where Id='" + id + "'";
        Log.d("query1", deleteQuery1);
        database.execSQL(deleteQuery1);
    }


    public ArrayList<HashMap<String, String>> getAllUpdateTranStatus() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery1 = "SELECT * FROM Tb2_FSTrak";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery1, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Id", cursor.getString(0));
                map.put("jsonData", cursor.getString(1));
                map.put("authString", cursor.getString(2));

                wordList.add(map);
            } while (cursor.moveToNext());
        }
        return wordList;
    }

    //=========================================

}
