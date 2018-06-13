package com.example.android.inventory;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.MerchandiseContract.MerchandiseEntry;
import com.example.android.inventory.data.MerchandiseDbHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity {

    // Find all relevant views that we will need to read user input from
    @BindView(R.id.product_name)
    EditText mProductName;

    @BindView(R.id.price)
    EditText mPrice;

    @BindView(R.id.quantity)
    EditText mQuantity;

    @BindView(R.id.supplier_name)
    EditText mSupplier;

    @BindView(R.id.phone)
    EditText mPhone;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // bind the view using butterknife
        ButterKnife.bind(this);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertItem();
                //finish this activity
                finish();
            }
        });
    }

    private void insertItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productName = mProductName.getText().toString().trim();
        double price = Double.parseDouble(mPrice.getText().toString());
        int quantity = Integer.parseInt(mQuantity.getText().toString());
        String supplier = mSupplier.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();

        // Create database helper
        MerchandiseDbHelper dbHelper = new MerchandiseDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and merchandise attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(MerchandiseEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(MerchandiseEntry.COLUMN_PRICE, price);
        values.put(MerchandiseEntry.COLUMN_QUANTITY, quantity);
        values.put(MerchandiseEntry.COLUMN_SUPPLIER_NAME, supplier);
        values.put(MerchandiseEntry.COLUMN_SUPPLIER_PHONE, phone);

        // Insert a new row for merchandise in the database, returning the ID of that new row.
        long newRowId = db.insert(MerchandiseEntry.TABLE_NAME, null, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, com.example.android.inventory.R.string.error_saving_product, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, getString(com.example.android.inventory.R.string.product_saved) + newRowId, Toast.LENGTH_SHORT).show();
        }
    }
}
