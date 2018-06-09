package com.example.android.inventory.data;

import android.provider.BaseColumns;

//API Contract for the Pets app
public final class MerchandiseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private MerchandiseContract() {
    }

    /* Inner class that defines the table contents */
    public static final class MerchandiseEntry implements BaseColumns {
        public static final String TABLE_NAME = "merchandise";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_SUPPLIER_PHONE = "phone";
    }
}
