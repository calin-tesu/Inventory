package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.inventory.data.MerchandiseContract.MerchandiseEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the merchandise data loader
     */
    private static final int EXISTING_MERCHANDISE_LOADER = 0;

    /**
     * Content URI for the existing merchandise (null if it's a new product)
     */
    private Uri mCurrentMerchandiseUri;

    // Find all relevant views that we will need to read user input from
    private EditText mProductName;

    private EditText mPrice;

    private EditText mQuantity;

    private EditText mSupplier;

    private EditText mPhone;

    /**
     * Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
     */
    private boolean mMerchandiseHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mMerchandiseHasChanged boolean to true.
     */
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMerchandiseHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new merchandise or editing an existing one.
        Intent intent = getIntent();
        mCurrentMerchandiseUri = intent.getData();

        // If the intent DOES NOT contain a merchandise content URI, then we know that we are
        // creating a new product.
        if (mCurrentMerchandiseUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a merchandise that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing merchandise, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the merchandise data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_MERCHANDISE_LOADER, null, this);
        }

        mProductName = findViewById(R.id.product_name);
        mPrice = findViewById(R.id.price);
        mQuantity = findViewById(R.id.quantity);
        mSupplier = findViewById(R.id.supplier_name);
        mPhone = findViewById(R.id.phone);

        ImageButton btnMakePhoneCall = findViewById(R.id.btn_make_phone_call);
        ImageButton btnAddQuantity = findViewById(R.id.btn_plus_quantity);
        ImageButton btnRemoveQuantity = findViewById(R.id.btn_minus_quantity);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductName.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mSupplier.setOnTouchListener(mTouchListener);
        mPhone.setOnTouchListener(mTouchListener);
        btnAddQuantity.setOnTouchListener(mTouchListener);
        btnRemoveQuantity.setOnTouchListener(mTouchListener);

        // Setup onClickListeners on the buttons
        btnMakePhoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mPhone.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        btnAddQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = mQuantity.getText().toString();
                if (TextUtils.isEmpty(quantity)) {
                    mQuantity.setText("1");
                } else {
                    int quantity_value = Integer.parseInt(mQuantity.getText().toString().trim());
                    quantity_value = quantity_value + 1;
                    mQuantity.setText(String.valueOf(quantity_value));
                }
            }
        });

        btnRemoveQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = mQuantity.getText().toString().trim();
                int quantity_value = Integer.parseInt(quantity);
                if (TextUtils.isEmpty(quantity)) {
                    mQuantity.setText("0");
                } else if (quantity_value > 0) {
                    quantity_value = quantity_value - 1;
                    mQuantity.setText(String.valueOf(quantity_value));
                } else {
                    Toast.makeText(EditorActivity.this, getResources().getText(R.string.out_of_stock),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor_activity, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new merchandise, hide the "Delete" menu item.
        if (mCurrentMerchandiseUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (verifyInputFields()) {
                    // Save pet to database
                    saveMerchandise();
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mMerchandiseHasChanged) {
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

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the merchandise hasn't changed, continue with handling back button press
        if (!mMerchandiseHasChanged) {
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


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MerchandiseEntry._ID,
                MerchandiseEntry.COLUMN_PRODUCT_NAME,
                MerchandiseEntry.COLUMN_PRICE,
                MerchandiseEntry.COLUMN_QUANTITY,
                MerchandiseEntry.COLUMN_SUPPLIER_NAME,
                MerchandiseEntry.COLUMN_SUPPLIER_PHONE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentMerchandiseUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of merchandise attributes that we're interested in
            int productColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String product = cursor.getString(productColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);

            // Update the views on the screen with the values from the database
            mProductName.setText(product);
            mPrice.setText(Double.toString(price));
            mQuantity.setText(Integer.toString(quantity));
            mSupplier.setText(supplier);
            mPhone.setText(phone);
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductName.setText("");
        mPrice.setText("");
        mQuantity.setText("");
        mSupplier.setText("");
        mPhone.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteMerchandise();
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

    private boolean verifyInputFields() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productString = mProductName.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String quantityString = mQuantity.getText().toString().trim();
        String supplierString = mSupplier.getText().toString().trim();
        String phoneString = mPhone.getText().toString().trim();

        // Check that all fields in the EditText view are completed and
        // the price of merchandise is not 0.
        // No need to check for negative values of price and quantity because
        // only positive inputs are possible as specified in activity_editor.xml inputType (is NOT signed)
        if (TextUtils.isEmpty(productString) ||
                TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) ||
                TextUtils.isEmpty(supplierString) ||
                TextUtils.isEmpty(phoneString)) {
            Toast.makeText(this, getString(R.string.all_field_required), Toast.LENGTH_SHORT).show();
            return false;
        } else if (Double.parseDouble(priceString) == 0) {
            Toast.makeText(this, getString(R.string.price_greater_then_zero), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get user input from editor and save pet into database.
     */
    private void saveMerchandise() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productString = mProductName.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String quantityString = mQuantity.getText().toString().trim();
        String supplierString = mSupplier.getText().toString().trim();
        String phoneString = mPhone.getText().toString().trim();

        // Check if this is supposed to be a new merchandise
        // and check if all the fields in the editor are blank
        if (mCurrentMerchandiseUri == null && TextUtils.isEmpty(productString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(phoneString)) {
            // Since no fields were modified, we can return early without creating a new merchandise.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and merchandise attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(MerchandiseEntry.COLUMN_PRODUCT_NAME, productString);
        values.put(MerchandiseEntry.COLUMN_PRICE, Double.parseDouble(priceString));
        values.put(MerchandiseEntry.COLUMN_QUANTITY, Integer.parseInt(quantityString));
        values.put(MerchandiseEntry.COLUMN_SUPPLIER_NAME, supplierString);
        values.put(MerchandiseEntry.COLUMN_SUPPLIER_PHONE, phoneString);

        // Determine if this is a new or existing product by checking if mCurrentMerchandiseUri is null or not
        if (mCurrentMerchandiseUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(MerchandiseEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.error_saving_product),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.product_saved),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product
            // with content URI: mCurrentMerchandiseUri and pass in the new ContentValues.
            // Pass in null for the selection and selection args because mCurrentMerchandiseUri
            // will already identify the correct row in the database that we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentMerchandiseUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.error_saving_product),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.product_saved),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteMerchandise() {
        // Only perform the delete if this is an existing merchandise.
        if (mCurrentMerchandiseUri != null) {
            // Call the ContentResolver to delete the merchandise at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentMerchandiseUri
            // content URI already identifies the merchandise that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentMerchandiseUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.error_deleting_product),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

            // Close the activity
            finish();
        }
    }
}
