package com.example.makmurjayakosmetik

import android.provider.BaseColumns

object DBEntity {
    class TableAccount : BaseColumns {
        companion object {
            const val TABLE_NAME = "account"
            const val COLUMN_USERNAME = "username"
            const val COLUMN_PASSWORD = "password"
            const val COLUMN_NAME = "name"
        }
    }

    class TableStore : BaseColumns {
        companion object {
            const val TABLE_NAME = "store"
            const val COLUMN_NAME = "name"
            const val COLUMN_ID = "id"
            const val COLUMN_PLATFORM = "platform"
        }
    }

    class TableSupplier : BaseColumns {
        companion object {
            const val TABLE_NAME = "supplier"
            const val COLUMN_ID = "id"
            const val COLUMN_NAME = "name"
            const val COLUMN_ADDRESS = "address"
            const val COLUMN_CITY = "city"
            const val COLUMN_PHONE_NUM = "phone_number"
            const val COLUMN_EMAIL = "email"
        }
    }

    class TableProduct : BaseColumns {
        companion object {
            const val TABLE_NAME = "products"
            const val COLUMN_ID = "id"
            const val COLUMN_NAME = "name"
            const val COLUMN_CATEGORY_NAME = "category_name"
            const val COLUMN_CAPITAL_PRICE = "capital_price"
            const val COLUMN_WHOLESALE_PRICE = "wholesale_price"
            const val COLUMN_RETAIL_PRICE = "selling_price"
            const val COLUMN_TOTAL_STOCK = "total_stock"
            const val COLUMN_VARIANT_NAME = "variant_name"
            const val COLUMN_VARIANTS = "variants"
            const val COLUMN_SUPPLIER_ID = "supplier_id"
            const val COLUMN_IMAGES = "images"
        }
    }

    class TableCategory : BaseColumns {
        companion object {
            const val TABLE_NAME = "category"
            const val COLUMN_NAME = "name"
            const val COLUMN_DESC = "desc"
        }
    }

    class TableSales : BaseColumns {
        companion object {
            const val TABLE_NAME = "sales"
            const val COLUMN_ID = "id"
            const val COLUMN_STORE = "store"
            const val COLUMN_TYPE = "type"
            const val COLUMN_PAYMENT_STATUS = "payment_status"
            const val COLUMN_ITEM_STATUS = "item_status"
            const val COLUMN_DATETIME = "datetime"
            const val COLUMN_TOTAL_ITEM = "total_item"
            const val COLUMN_TOTAL_PRICE = "total_price"
        }
    }

    class TableSalesDetails : BaseColumns {
        companion object {
            const val TABLE_NAME = "sales_details"
            const val COLUMN_SALES_ID = "sales_id"
            const val COLUMN_PRODUCT_ID = "product_id"
            const val COLUMN_AMOUNT = "amount"
            const val COLUMN_VARIANTS = "variants"
            const val COLUMN_PRICE = "price"
        }
    }

    class TablePurchase : BaseColumns {
        companion object {
            const val TABLE_NAME = "purchase"
            const val COLUMN_ID = "id"
            const val COLUMN_SUPPLIER_ID = "supplier_id"
            const val COLUMN_DATETIME = "datetime"
            const val COLUMN_ITEM_CHECKED_STATUS = "item_checked_status"
            const val COLUMN_PAYMENT_METHOD = "payment_method"
            const val COLUMN_TOTAL_PAID = "total_paid"
            const val COLUMN_TOTAL_ITEM = "total_item"
            const val COLUMN_TOTAL_PURCHASE = "total_purchase"
        }
    }

    class TablePurchaseDetails : BaseColumns {
        companion object {
            const val TABLE_NAME = "purchase_details"
            const val COLUMN_PURCHASE_ID = "purchase_id"
            const val COLUMN_PRODUCT_ID = "product_id"
            const val COLUMN_AMOUNT = "amount"
            const val COLUMN_VARIANTS = "variants"
            const val COLUMN_PRICE = "price"
        }
    }

    class TablePayment : BaseColumns {
        companion object {
            const val TABLE_NAME = "payment"
            const val COLUMN_ID = "id"
            const val COLUMN_PURCHASE_ID = "purchase_id"
            const val COLUMN_DATETIME = "datetime"
            const val COLUMN_TOTAL_PAYMENT = "total_payment"
            const val COLUMN_MESSAGE = "message"
        }
    }

    class TableReturn : BaseColumns {
        companion object {
            const val TABLE_NAME = "restock"
            const val COLUMN_ID = "id"
            const val COLUMN_SUPPLIER_ID = "supplier_id"
            const val COLUMN_DATETIME = "datetime"
            const val COLUMN_ITEM_CHECKED_STATUS = "item_checked_status"
            const val COLUMN_PAYMENT_TYPE = "payment_type"
            const val COLUMN_TOTAL_PAID = "total_paid"
            const val COLUMN_TOTAL_ITEM = "total_item"
            const val COLUMN_TOTAL_PURCHASE = "total_purchase"
        }
    }

    class TableLog : BaseColumns {
        companion object {
            const val TABLE_NAME = "log"
            const val COLUMN_ID = "id"
            const val COLUMN_DATETIME = "datetime"
            const val COLUMN_TABLE_NAME = "table_name"
            const val COLUMN_PRIMARY_KEY = "primary_key"
            const val COLUMN_COMMAND = "command"
            const val COLUMN_COMMITER = "commiter"
        }
    }
}