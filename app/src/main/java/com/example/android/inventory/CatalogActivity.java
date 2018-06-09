package com.example.android.inventory;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventory.data.MerchandiseDbHelper;

import static com.example.android.inventory.data.MerchandiseContract.MerchandiseEntry;

public class CatalogActivity extends AppCompatActivity {

    /**
     * Database helper that will provide us access to the database
     */
    private MerchandiseDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new MerchandiseDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display database information on screen.
     */
    private void displayDatabaseInfo() {
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                MerchandiseEntry._ID,
                MerchandiseEntry.COLUMN_PRODUCT_NAME,
                MerchandiseEntry.COLUMN_PRICE,
                MerchandiseEntry.COLUMN_QUANTITY,
                MerchandiseEntry.COLUMN_SUPPLIER_NAME,
                MerchandiseEntry.COLUMN_SUPPLIER_PHONE};

        // Perform a query on the merchandise table
        Cursor cursor = db.query(
                MerchandiseEntry.TABLE_NAME,
                projection,            // The columns to return
                null,         // The columns for the WHERE clause
                null,      // The values for the WHERE clause
                null,         // Don't group the rows
                null,          // Don't filter by row groups
                null);        // The sort order

        TextView displayView = findViewById(R.id.merchandise_text_view);

        try {
            // Create a header in the Text View that looks like this:
            //
            // The merchandise table contains <number of rows in Cursor>.
            // _id - product name - price - quantity - supplier name - supplier phone
            //
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.setText("The merchandise table contains " + cursor.getCount() + " products.\n\n");
            displayView.append(MerchandiseEntry._ID + " - " +
                    MerchandiseEntry.COLUMN_PRODUCT_NAME + " - " +
                    MerchandiseEntry.COLUMN_PRICE + " - " +
                    MerchandiseEntry.COLUMN_QUANTITY + " - " +
                    MerchandiseEntry.COLUMN_SUPPLIER_NAME + " - " +
                    MerchandiseEntry.COLUMN_SUPPLIER_PHONE + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(MerchandiseEntry._ID);
            int productColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_SUPPLIER_PHONE);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentProduct = cursor.getString(productColumnIndex);
                double currentPrice = cursor.getDouble(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplier = cursor.getString(supplierColumnIndex);
                String currentPhone = cursor.getString(phoneColumnIndex);
                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - " +
                        currentProduct + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentSupplier + " - " +
                        currentPhone));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }
}

