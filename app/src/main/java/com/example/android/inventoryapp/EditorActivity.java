package com.example.android.inventoryapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.StockItemContract;

import java.io.ByteArrayOutputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ImageView mProductImage;
    private EditText mProductTitle;
    private EditText mProductBrand;
    private EditText mProductPrice;
    private EditText mProductsInStock;
    private View mOrderMore;
    private Button mPlussButton;
    private Button mMinusButton;

    private Uri mCurrentItemUri;
    private boolean mItemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        mProductImage = (ImageView) findViewById(R.id.product_image);
        mProductTitle = (EditText) findViewById(R.id.product_title_input);
        mProductBrand = (EditText) findViewById(R.id.product_brand_input);
        mProductPrice = (EditText) findViewById(R.id.product_price_input);
        mProductsInStock = (EditText) findViewById(R.id.product_stock_input);
        mPlussButton = (Button) findViewById(R.id.btn_pluss);
        mMinusButton = (Button) findViewById(R.id.btn_minus);
        mOrderMore = findViewById(R.id.order_more);

        mProductTitle.setOnTouchListener(mTouchListener);
        mProductBrand.setOnTouchListener(mTouchListener);
        mProductImage.setOnTouchListener(mTouchListener);
        mProductsInStock.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);


        if (mCurrentItemUri == null) {
            setTitle("Add an item");
            mOrderMore.setVisibility(View.INVISIBLE);
            mProductPrice.setText("0");
            mProductsInStock.setText("0");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle("Edit item");
            getSupportLoaderManager().initLoader(1, null, this);
        }

        mProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePhoto.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePhoto, 1);
                }
            }
        });
        mPlussButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mProductsInStock.getText().toString()) + 1;
                mProductsInStock.setText(String.valueOf(quantity));
                mItemHasChanged = true;
            }
        });
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mProductsInStock.getText().toString());
                if (quantity - 1 >= 0) {
                    quantity = quantity - 1;
                    mProductsInStock.setText(String.valueOf(quantity));
                    mItemHasChanged = true;
                }
            }
        });

        mOrderMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mProductTitle.getText().toString();
                String brand = mProductBrand.getText().toString();
                String quantity = mProductsInStock.getText().toString();
                String message = "I would like o order " + title + "\n -by: " + brand + "  x" + quantity;
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, "Product order");
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            mProductImage.setImageBitmap(photo);
            mItemHasChanged = true;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteItem() {
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_success),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();

        }
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveItem() {

        Drawable imageImage = mProductImage.getDrawable();
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageImage);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] imageByte = bos.toByteArray();

        String title = mProductTitle.getText().toString().trim();
        String brand = mProductBrand.getText().toString().trim();
        String price = mProductPrice.getText().toString().trim();
        int quantity = Integer.parseInt(mProductsInStock.getText().toString().trim());

        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(title) && TextUtils.isEmpty(brand)) {
            return;
        }

        ContentValues values = new ContentValues();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(EditorActivity.this, "you have to enter  title!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            values.put(StockItemContract.StockEntry.PRODUCT_TITLE, title);
            values.put(StockItemContract.StockEntry.IMAGE, imageByte);
            if (TextUtils.isEmpty(brand)) {
                Toast.makeText(EditorActivity.this, "you have to enter brand!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                values.put(StockItemContract.StockEntry.BRAND, brand);
                if (TextUtils.isEmpty(price)) {
                    values.put(StockItemContract.StockEntry.PRICE, "1");
                } else {
                    values.put(StockItemContract.StockEntry.PRICE, price);
                }
                if (quantity < 0 || quantity > 999) {
                    Toast.makeText(EditorActivity.this, "you have to enter a valid for the quantity(0-999)", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    values.put(StockItemContract.StockEntry.QUANTITY, quantity);
                }

            }
        }
        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(StockItemContract.StockEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "There was an error saving the item",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item successfully saved!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "There was a problem updating the  information",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item updated successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                StockItemContract.StockEntry._ID,
                StockItemContract.StockEntry.PRODUCT_TITLE,
                StockItemContract.StockEntry.BRAND,
                StockItemContract.StockEntry.IMAGE,
                StockItemContract.StockEntry.QUANTITY,
                StockItemContract.StockEntry.PRICE};
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int titleColumnIndesx = cursor.getColumnIndex(StockItemContract.StockEntry.PRODUCT_TITLE);
            int brandColumnIndex = cursor.getColumnIndex(StockItemContract.StockEntry.BRAND);
            int imageColumnIndex = cursor.getColumnIndex(StockItemContract.StockEntry.IMAGE);
            int quanityColumnIndex = cursor.getColumnIndex(StockItemContract.StockEntry.QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(StockItemContract.StockEntry.PRICE);

            byte[] Image = cursor.getBlob(imageColumnIndex);
            String title = cursor.getString(titleColumnIndesx);
            String brand = cursor.getString(brandColumnIndex);
            int quantity = cursor.getInt(quanityColumnIndex);
            String price = cursor.getString(priceColumnIndex);

            mProductTitle.setText(title);
            mProductBrand.setText(brand);
            Bitmap bitmap = BitmapFactory.decodeByteArray(Image, 0, Image.length);
            mProductImage.setImageBitmap(bitmap);
            mProductsInStock.setText(String.valueOf(quantity));
            mProductPrice.setText(price);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
