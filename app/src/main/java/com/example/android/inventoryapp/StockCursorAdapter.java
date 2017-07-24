package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.StockItemContract.StockEntry;


/**
 * Created by BogdanMihalca(@MBC) on 7/23/2017.
 */

public class StockCursorAdapter extends CursorAdapter {

    //constructor
    public StockCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView titleTextView = (TextView) view.findViewById(R.id.product_title);
        TextView brandTextView = (TextView) view.findViewById(R.id.product_brand);
        TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
        TextView stockTextView = (TextView) view.findViewById(R.id.product_stock);
        ImageView productImage = (ImageView) view.findViewById(R.id.product_image);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        int rowIdIndex = cursor.getColumnIndex(StockEntry._ID);
        int titleColumnIndesx = cursor.getColumnIndex(StockEntry.PRODUCT_TITLE);
        int brandColumnIndex = cursor.getColumnIndex(StockEntry.BRAND);
        int imageColumnIndex = cursor.getColumnIndex(StockEntry.IMAGE);
        int quanityColumnIndex = cursor.getColumnIndex(StockEntry.QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(StockEntry.PRICE);

        byte[] Image = cursor.getBlob(imageColumnIndex);
        String title = cursor.getString(titleColumnIndesx);
        String brand = cursor.getString(brandColumnIndex);
        final int quantity = cursor.getInt(quanityColumnIndex);
        String price = cursor.getString(priceColumnIndex);
        final int ID = cursor.getInt(rowIdIndex);

        titleTextView.setText(title);
        brandTextView.setText(brand);
        stockTextView.setText(String.valueOf(quantity));
        String formatedPrice = price + " $";
        priceTextView.setText(formatedPrice);

        Bitmap bitmap = BitmapFactory.decodeByteArray(Image, 0, Image.length);
        productImage.setImageBitmap(bitmap);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentItemUri = ContentUris.withAppendedId(StockEntry.CONTENT_URI, ID);
                sale(context, quantity, currentItemUri);
            }
        });


    }


    private void sale(Context context, int quantity, Uri itemUri) {
        if (quantity == 0) {
            Log.v("StockCursorAdapter", "quantity cannot be reduced");
        } else {
            ContentValues values = new ContentValues();
            int q = quantity - 1;
            values.put(StockEntry.QUANTITY, q);
            int rowAffected = context.getContentResolver().update(itemUri, values, null, null);
            if (rowAffected == 0) {
                Log.v("StockCursorAdapter", "no rows changed");

            } else {
                Log.v("StockCursorAdapter", "rows changed = " + rowAffected);

            }

        }

    }


}
