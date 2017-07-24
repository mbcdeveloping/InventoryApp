package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by BogdanMihalca(@MBC) on 7/23/2017.
 */

public class StockItemContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_STOCK = "stock";

    public StockItemContract() {
    }

    public static final class StockEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STOCK);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;
        /**
         * Name of database table for items
         */
        public final static String TABLE_NAME = "stock";
        /**
         * Unique ID number for the pet (only for use in the database table).(INTEGER)
         */
        public final static String _ID = BaseColumns._ID;
        /**
         * The title of the product(STRING)
         */
        public final static String PRODUCT_TITLE = "product_title";
        /**
         * The brand of the product(STRING)
         */
        public final static String BRAND = "brand";
        /**
         * the image  path of the product(BLOB)
         */
        public final static String IMAGE = "image";
        /**
         * quantity (INTEGER)
         */
        public final static String QUANTITY = "quantity";
        /**
         * sale price(REAL)
         */
        public final static String PRICE = "price";
    }
}
