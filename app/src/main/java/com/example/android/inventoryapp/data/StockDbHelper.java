package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.StockItemContract.StockEntry;

/**
 * Created by BogdanMihalca(@MBC) on 7/23/2017.
 */

public class StockDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = StockDbHelper.class.getSimpleName();
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "stock.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    public StockDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + StockEntry.TABLE_NAME + " ("
                + StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StockEntry.PRODUCT_TITLE + " TEXT NOT NULL, "
                + StockEntry.BRAND + " TEXT NOT NULL, "
                + StockEntry.IMAGE + " BLOB, "
                + StockEntry.QUANTITY + " INTEGER DEFAULT 0,"
                + StockEntry.PRICE + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_STOCK_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
