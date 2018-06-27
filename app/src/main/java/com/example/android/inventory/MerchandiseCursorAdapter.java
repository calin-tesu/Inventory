package com.example.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import static com.example.android.inventory.data.MerchandiseContract.MerchandiseEntry;

public class MerchandiseCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link MerchandiseCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public MerchandiseCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView productTextView = view.findViewById(R.id.product_name);
        TextView priceTextView = view.findViewById(R.id.price_value);
        TextView quantityTextView = view.findViewById(R.id.quantity_value);
        Button sellButton = view.findViewById(R.id.sell_button);

        // Find the columns of merchandise attributes that we're interested in
        int productColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_QUANTITY);

        // Read the merchandise attributes from the Cursor for the current merchandise
        String productName = cursor.getString(productColumnIndex);
        String priceValue = cursor.getString(priceColumnIndex);
        String quantityValue = cursor.getString(quantityColumnIndex);

        // Logic for hiding the Sale button when the quantity of the current
        // merchandise is 0 (we are out of stock), so no more sales are possible
        if (Integer.parseInt(quantityValue) == 0) {
            sellButton.setVisibility(View.GONE);
        }

        // Update the TextViews with the attributes for the current merchandise
        productTextView.setText(productName);
        priceTextView.setText(priceValue);
        quantityTextView.setText(quantityValue);
    }
}
