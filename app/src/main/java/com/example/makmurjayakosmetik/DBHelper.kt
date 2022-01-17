package com.example.makmurjayakosmetik

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.makmurjayakosmetik.classes.*
import java.lang.Exception
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "MakmurJayaKosmetik.db"
        const val DATABASE_VERSION = 1
        val dateFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale("in", "ID"))
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        val createTableAccount = "create table if not exists ${DBEntity.TableAccount.TABLE_NAME} " +
                "(" +
                "${DBEntity.TableAccount.COLUMN_USERNAME} text not null," +
                "${DBEntity.TableAccount.COLUMN_PASSWORD} text," +
                "${DBEntity.TableAccount.COLUMN_NAME} text not null," +
                "primary key (${DBEntity.TableAccount.COLUMN_USERNAME})" +
                ")"

        val createTableStore = "create table if not exists ${DBEntity.TableStore.TABLE_NAME} " +
                "(" +
                "${DBEntity.TableStore.COLUMN_NAME} text not null," +
                "${DBEntity.TableStore.COLUMN_ID} text not null," +
                "${DBEntity.TableStore.COLUMN_PLATFORM} text not null" +
                ")"

        val createTableSupplier = "create table if not exists ${DBEntity.TableSupplier.TABLE_NAME} " +
                "(" +
                "${DBEntity.TableSupplier.COLUMN_ID} text not null," +
                "${DBEntity.TableSupplier.COLUMN_NAME} text not null," +
                "${DBEntity.TableSupplier.COLUMN_ADDRESS} text not null," +
                "${DBEntity.TableSupplier.COLUMN_CITY} text not null," +
                "${DBEntity.TableSupplier.COLUMN_PHONE_NUM} text not null," +
                "${DBEntity.TableSupplier.COLUMN_EMAIL} text not null," +
                "primary key (${DBEntity.TableSupplier.COLUMN_ID})" +
                ")"

        val createTableCategory = "create table if not exists ${DBEntity.TableCategory.TABLE_NAME} " +
                "(" +
                "${DBEntity.TableCategory.COLUMN_NAME} text not null," +
                "${DBEntity.TableCategory.COLUMN_DESC} text not null," +
                "primary key (${DBEntity.TableCategory.COLUMN_NAME})" +
                ")"

        val createTableProduct = "create table if not exists ${DBEntity.TableProduct.TABLE_NAME} " +
                "(" +
                "${DBEntity.TableProduct.COLUMN_ID} text not null," +
                "${DBEntity.TableProduct.COLUMN_NAME} text not null," +
                "${DBEntity.TableProduct.COLUMN_CATEGORY_NAME} text not null," +
                "${DBEntity.TableProduct.COLUMN_CAPITAL_PRICE} integer not null," +
                "${DBEntity.TableProduct.COLUMN_WHOLESALE_PRICE} integer not null," +
                "${DBEntity.TableProduct.COLUMN_RETAIL_PRICE} integer not null," +
                "${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} integer not null," +
                "${DBEntity.TableProduct.COLUMN_VARIANT_NAME} text not null," +
                "${DBEntity.TableProduct.COLUMN_VARIANTS} text not null," +
                "${DBEntity.TableProduct.COLUMN_SUPPLIER_ID} text not null," +
                "${DBEntity.TableProduct.COLUMN_IMAGES} text not null," +
                "primary key (${DBEntity.TableProduct.COLUMN_ID})," +
                "foreign key (${DBEntity.TableProduct.COLUMN_CATEGORY_NAME}) references ${DBEntity.TableCategory.TABLE_NAME}(${DBEntity.TableCategory.COLUMN_NAME})" +
                ")"

        val createTableSales = "create table if not exists ${DBEntity.TableSales.TABLE_NAME} " +
                "(" +
                "${DBEntity.TableSales.COLUMN_ID} text not null," +
                "${DBEntity.TableSales.COLUMN_STORE} text not null," +
                "${DBEntity.TableSales.COLUMN_TYPE} text not null," +
                "${DBEntity.TableSales.COLUMN_PAYMENT_STATUS} text not null," +
                "${DBEntity.TableSales.COLUMN_ITEM_STATUS} text not null," +
                "${DBEntity.TableSales.COLUMN_DATETIME} text not null," +
                "${DBEntity.TableSales.COLUMN_TOTAL_ITEM} integer not null," +
                "${DBEntity.TableSales.COLUMN_TOTAL_PRICE} integer not null," +
                "primary key (${DBEntity.TableSales.COLUMN_ID})" +
                ")"

        val createTableSalesDetails = "create table if not exists ${DBEntity.TableSalesDetails.TABLE_NAME} " +
                "(" +
                "${DBEntity.TableSalesDetails.COLUMN_SALES_ID} text not null," +
                "${DBEntity.TableSalesDetails.COLUMN_PRODUCT_ID} text not null," +
                "${DBEntity.TableSalesDetails.COLUMN_AMOUNT} integer not null," +
                "${DBEntity.TableSalesDetails.COLUMN_VARIANTS} text not null," +
                "${DBEntity.TableSalesDetails.COLUMN_PRICE} integer not null," +
                "foreign key (${DBEntity.TableSalesDetails.COLUMN_SALES_ID}) references ${DBEntity.TableSales.TABLE_NAME}(${DBEntity.TableSales.COLUMN_ID})," +
                "foreign key (${DBEntity.TableSalesDetails.COLUMN_PRODUCT_ID}) references ${DBEntity.TableProduct.TABLE_NAME}(${DBEntity.TableProduct.COLUMN_ID})" +
                ")"

        val createTablePurchase = "create table if not exists ${DBEntity.TablePurchase.TABLE_NAME} " +
                "(" +
                "${DBEntity.TablePurchase.COLUMN_ID} text not null," +
                "${DBEntity.TablePurchase.COLUMN_SUPPLIER_ID} text not null," +
                "${DBEntity.TablePurchase.COLUMN_DATETIME} text not null," +
                "${DBEntity.TablePurchase.COLUMN_ITEM_CHECKED_STATUS} text not null," +
                "${DBEntity.TablePurchase.COLUMN_PAYMENT_METHOD} text not null," +
                "${DBEntity.TablePurchase.COLUMN_TOTAL_PAID} integer not null," +
                "${DBEntity.TablePurchase.COLUMN_TOTAL_ITEM} integer not null," +
                "${DBEntity.TablePurchase.COLUMN_TOTAL_PURCHASE} integer not null," +
                "primary key (${DBEntity.TablePurchase.COLUMN_ID})," +
                "foreign key (${DBEntity.TablePurchase.COLUMN_SUPPLIER_ID}) references ${DBEntity.TableSupplier.TABLE_NAME}(${DBEntity.TableSupplier.COLUMN_ID})" +
                ")"

        val createTablePurchaseDetails = "create table if not exists ${DBEntity.TablePurchaseDetails.TABLE_NAME} " +
                "(" +
                "${DBEntity.TablePurchaseDetails.COLUMN_PURCHASE_ID} text not null," +
                "${DBEntity.TablePurchaseDetails.COLUMN_PRODUCT_ID} text not null," +
                "${DBEntity.TablePurchaseDetails.COLUMN_AMOUNT} integer not null," +
                "${DBEntity.TablePurchaseDetails.COLUMN_VARIANTS} text not null," +
                "${DBEntity.TablePurchaseDetails.COLUMN_PRICE} integer not null," +
                "foreign key (${DBEntity.TablePurchaseDetails.COLUMN_PURCHASE_ID}) references ${DBEntity.TablePurchase.TABLE_NAME}(${DBEntity.TablePurchase.COLUMN_ID})," +
                "foreign key (${DBEntity.TablePurchaseDetails.COLUMN_PRODUCT_ID}) references ${DBEntity.TableProduct.TABLE_NAME}(${DBEntity.TableProduct.COLUMN_ID})" +
                ")"

        val createTablePayment = "create table if not exists ${DBEntity.TablePayment.TABLE_NAME} " +
                "(" +
                "${DBEntity.TablePayment.COLUMN_ID} text not null," +
                "${DBEntity.TablePayment.COLUMN_PURCHASE_ID} text not null," +
                "${DBEntity.TablePayment.COLUMN_DATETIME} text not null," +
                "${DBEntity.TablePayment.COLUMN_TOTAL_PAYMENT} text not null," +
                "${DBEntity.TablePayment.COLUMN_MESSAGE} text not null," +
                "foreign key (${DBEntity.TablePayment.COLUMN_PURCHASE_ID}) references ${DBEntity.TablePurchase.TABLE_NAME}(${DBEntity.TablePurchase.COLUMN_ID})" +
                ")"

        val createTableLog = "create table if not exists ${DBEntity.TableLog.TABLE_NAME} " +
                "(" +
                "${DBEntity.TableLog.COLUMN_ID} text not null," +
                "${DBEntity.TableLog.COLUMN_DATETIME} text not null," +
                "${DBEntity.TableLog.COLUMN_TABLE_NAME} text not null," +
                "${DBEntity.TableLog.COLUMN_COMMAND} text not null," +
                "${DBEntity.TableLog.COLUMN_COMMITER} text not null," +
                "primary key (${DBEntity.TableLog.COLUMN_ID})" +
                ")"

        val createTriggerInsertPayment = "create trigger if not exists trigger_insert_payment after insert on ${DBEntity.TablePayment.TABLE_NAME} begin " +
                "update ${DBEntity.TablePurchase.TABLE_NAME} set ${DBEntity.TablePurchase.COLUMN_TOTAL_PAID} = ${DBEntity.TablePurchase.COLUMN_TOTAL_PAID} + NEW.${DBEntity.TablePayment.COLUMN_TOTAL_PAYMENT} " +
                "where ${DBEntity.TablePurchase.TABLE_NAME}.${DBEntity.TablePurchase.COLUMN_ID} = NEW.${DBEntity.TablePayment.COLUMN_PURCHASE_ID};" +
                "end;"

        val createTriggerDeletePayment = "create trigger if not exists trigger_delete_payment after delete on ${DBEntity.TablePayment.TABLE_NAME} begin " +
                "update ${DBEntity.TablePurchase.TABLE_NAME} set ${DBEntity.TablePurchase.COLUMN_TOTAL_PAID} = ${DBEntity.TablePurchase.COLUMN_TOTAL_PAID} - OLD.${DBEntity.TablePayment.COLUMN_TOTAL_PAYMENT} " +
                "where ${DBEntity.TablePurchase.TABLE_NAME}.${DBEntity.TablePurchase.COLUMN_ID} = OLD.${DBEntity.TablePayment.COLUMN_PURCHASE_ID};" +
                "end;"

        val createTriggerInsertPurchaseDetails = "create trigger if not exists trigger_insert_purchase_details after insert on ${DBEntity.TablePurchaseDetails.TABLE_NAME} begin " +
                "update ${DBEntity.TableProduct.TABLE_NAME} set ${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} = ${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} + NEW.${DBEntity.TablePurchaseDetails.COLUMN_AMOUNT} " +
                "where ${DBEntity.TableProduct.TABLE_NAME}.${DBEntity.TableProduct.COLUMN_ID} = NEW.${DBEntity.TablePurchaseDetails.COLUMN_PRODUCT_ID};" +
                "end;"

        val createTriggerDeletePurchaseDetails = "create trigger if not exists trigger_delete_purchase_details after delete on ${DBEntity.TablePurchaseDetails.TABLE_NAME} begin " +
                "update ${DBEntity.TableProduct.TABLE_NAME} set ${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} = ${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} - OLD.${DBEntity.TablePurchaseDetails.COLUMN_AMOUNT} " +
                "where ${DBEntity.TableProduct.TABLE_NAME}.${DBEntity.TableProduct.COLUMN_ID} = OLD.${DBEntity.TablePurchaseDetails.COLUMN_PRODUCT_ID};" +
                "end;"

        val createTriggerInsertSalesDetails = "create trigger if not exists trigger_insert_sales_details after insert on ${DBEntity.TableSalesDetails.TABLE_NAME} begin " +
                "update ${DBEntity.TableProduct.TABLE_NAME} set ${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} = ${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} - NEW.${DBEntity.TableSalesDetails.COLUMN_AMOUNT} " +
                "where ${DBEntity.TableProduct.TABLE_NAME}.${DBEntity.TableProduct.COLUMN_ID} = NEW.${DBEntity.TableSalesDetails.COLUMN_PRODUCT_ID};" +
                "end;"

        val createTriggerDeleteSalesDetails = "create trigger if not exists trigger_delete_sales_details after delete on ${DBEntity.TableSalesDetails.TABLE_NAME} begin " +
                "update ${DBEntity.TableProduct.TABLE_NAME} set ${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} = ${DBEntity.TableProduct.COLUMN_TOTAL_STOCK} + OLD.${DBEntity.TableSalesDetails.COLUMN_AMOUNT} " +
                "where ${DBEntity.TableProduct.TABLE_NAME}.${DBEntity.TableProduct.COLUMN_ID} = OLD.${DBEntity.TableSalesDetails.COLUMN_PRODUCT_ID};" +
                "end;"

        p0?.execSQL(createTableAccount)
        p0?.execSQL(createTableStore)
        p0?.execSQL(createTableSupplier)
        p0?.execSQL(createTableCategory)
        p0?.execSQL(createTableProduct)
        p0?.execSQL(createTableSales)
        p0?.execSQL(createTableSalesDetails)
        p0?.execSQL(createTablePurchase)
        p0?.execSQL(createTablePurchaseDetails)
        p0?.execSQL(createTablePayment)
        p0?.execSQL(createTableLog)
        p0?.execSQL(createTriggerInsertPayment)
        p0?.execSQL(createTriggerDeletePayment)
        p0?.execSQL(createTriggerInsertPurchaseDetails)
        p0?.execSQL(createTriggerDeletePurchaseDetails)
        p0?.execSQL(createTriggerInsertSalesDetails)
        p0?.execSQL(createTriggerDeleteSalesDetails)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("drop table if exists ${DBEntity.TableAccount.TABLE_NAME}")
        p0?.execSQL("drop table if exists ${DBEntity.TableStore.TABLE_NAME}")
        p0?.execSQL("drop table if exists ${DBEntity.TableSupplier.TABLE_NAME}")
        p0?.execSQL("drop table if exists ${DBEntity.TableProduct.TABLE_NAME}")
        p0?.execSQL("drop table if exists ${DBEntity.TableCategory.TABLE_NAME}")
        p0?.execSQL("drop table if exists ${DBEntity.TableSales.TABLE_NAME}")
        p0?.execSQL("drop table if exists ${DBEntity.TableSalesDetails.TABLE_NAME}")
        p0?.execSQL("drop table if exists ${DBEntity.TablePurchase.TABLE_NAME}")
        p0?.execSQL("drop table if exists ${DBEntity.TablePurchaseDetails.TABLE_NAME}")
    }

    fun checkAdminAccount() {
        val cursor = readableDatabase.query(
            DBEntity.TableAccount.TABLE_NAME,
            null,
            "${DBEntity.TableAccount.COLUMN_USERNAME} = ?",
            arrayOf("admin"),
            null,
            null,
            null
        )

        if (cursor.count <= 0) {
            val md5 = MessageDigest.getInstance("MD5")
            val password = BigInteger(1, md5.digest("admin".toByteArray())).toString().padStart(32, '0')

            var contentValues = ContentValues().apply {
                put(DBEntity.TableAccount.COLUMN_USERNAME, "admin")
                put(DBEntity.TableAccount.COLUMN_PASSWORD, password)
                put(DBEntity.TableAccount.COLUMN_NAME, "Admin")
            }
            writableDatabase.insert(DBEntity.TableAccount.TABLE_NAME, null, contentValues)

            contentValues = ContentValues().apply {
                put(DBEntity.TableAccount.COLUMN_USERNAME, "karyawan")
                put(DBEntity.TableAccount.COLUMN_PASSWORD, "")
                put(DBEntity.TableAccount.COLUMN_NAME, "Karyawan")
            }
            writableDatabase.insert(DBEntity.TableAccount.TABLE_NAME, null, contentValues)
        }
        cursor.close()
    }

    fun getAllAccount() : ArrayList<Account> {
        val accounts = arrayListOf<Account>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableAccount.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBEntity.TableAccount.COLUMN_NAME
            )

            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val account = Account(getString(0), getString(2))
                        if (getString(1).isNotEmpty()) account.hasPassword = true
                        accounts.add(account)
                    } while (cursor.moveToNext())
                }

            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }
        return accounts
    }

    fun getAccountByUsername(username: String) : Account? {
        var account: Account? = null
        val cursor = readableDatabase.query(
            DBEntity.TableAccount.TABLE_NAME,
            null,
            "${DBEntity.TableAccount.COLUMN_USERNAME} = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            account = Account(cursor.getString(0), cursor.getString(2))
            if (cursor.getString(1).isNotEmpty()) account.hasPassword = true
        }

        cursor.close()
        return account
    }

    fun checkLoginCredentials(account : Account) : Boolean {
        val cursor = readableDatabase.query(
            DBEntity.TableAccount.TABLE_NAME,
            arrayOf(DBEntity.TableAccount.COLUMN_PASSWORD),
            "${DBEntity.TableAccount.COLUMN_USERNAME} = ?",
            arrayOf(account.username),
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            return account.password == cursor.getString(0).also {
                cursor.close()
            }
        }
        cursor.close()
        return false
    }

    fun changeAccountPassword(account: Account, newPassword: String) {
        writableDatabase.execSQL("update ${DBEntity.TableAccount.TABLE_NAME} set ${DBEntity.TableAccount.COLUMN_PASSWORD} = '$newPassword' where ${DBEntity.TableAccount.COLUMN_USERNAME} = '${account.username}'")
    }

    fun insertAccount(account: Account) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TableAccount.COLUMN_USERNAME, account.username)
            put(DBEntity.TableAccount.COLUMN_PASSWORD, account.password)
            put(DBEntity.TableAccount.COLUMN_NAME, account.name)
        }

        writableDatabase.insert(DBEntity.TableAccount.TABLE_NAME, null, contentValues)
    }

    fun changePassword(username: String, newPassword: String) {
        writableDatabase.execSQL("update ${DBEntity.TableAccount.TABLE_NAME} set ${DBEntity.TableAccount.COLUMN_PASSWORD} = ? where ${DBEntity.TableAccount.COLUMN_USERNAME} = ?", arrayOf(newPassword, username))
    }

    fun updateAccount(username: String, newAccount: Account) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TableAccount.COLUMN_USERNAME, newAccount.username)
            put(DBEntity.TableAccount.COLUMN_PASSWORD, newAccount.password)
            put(DBEntity.TableAccount.COLUMN_NAME, newAccount.name)
        }

        writableDatabase.update(DBEntity.TableAccount.TABLE_NAME, contentValues, "${DBEntity.TableAccount.COLUMN_USERNAME} = ?", arrayOf(username))
    }

    fun deleteAccount(username: String) {
        writableDatabase.delete(DBEntity.TableAccount.TABLE_NAME, "${DBEntity.TableAccount.COLUMN_USERNAME} = ?", arrayOf(username))
    }

    fun getAllStore() : ArrayList<Store> {
        val stores = arrayListOf<Store>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableStore.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBEntity.TableStore.COLUMN_NAME
            )

            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val store = Store(getString(0), getString(1), getString(2))
                        stores.add(store)
                    } while (cursor.moveToNext())
                }

            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }
        return stores
    }

    fun getStoreByNameAndPlatform(name: String, platform: String) : Store? {
        var store: Store? = null
        val cursor = readableDatabase.query(
            DBEntity.TableStore.TABLE_NAME,
            null,
            "${DBEntity.TableStore.COLUMN_NAME} = ? and ${DBEntity.TableStore.COLUMN_PLATFORM} = ?",
            arrayOf(name, platform),
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            store = Store(cursor.getString(0), cursor.getString(1), cursor.getString(2))
        }

        cursor.close()
        return store
    }

    fun checkStoreIsExisted(store: Store) : Boolean {
        val cursor = readableDatabase.query(
            DBEntity.TableStore.TABLE_NAME,
            null,
            "${DBEntity.TableStore.COLUMN_NAME} = ? and ${DBEntity.TableStore.COLUMN_ID} = ? and ${DBEntity.TableStore.COLUMN_PLATFORM} = ?",
            arrayOf(store.name, store.id, store.platform),
            null,
            null,
            null
        )
        return (cursor.count > 0).also {
            cursor.close()
        }
    }

    fun insertStore(store: Store) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TableStore.COLUMN_NAME, store.name)
            put(DBEntity.TableStore.COLUMN_ID, store.id)
            put(DBEntity.TableStore.COLUMN_PLATFORM, store.platform)
        }
        writableDatabase.insert(DBEntity.TableStore.TABLE_NAME, null, contentValues)
    }

    fun updateStore(oldStore: Store, newStore: Store) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TableStore.COLUMN_NAME, newStore.name)
            put(DBEntity.TableStore.COLUMN_ID, newStore.id)
            put(DBEntity.TableStore.COLUMN_PLATFORM, newStore.platform)
        }
        writableDatabase.update(DBEntity.TableStore.TABLE_NAME, contentValues,
            "${DBEntity.TableStore.COLUMN_NAME} = ? and ${DBEntity.TableStore.COLUMN_ID} = ? and ${DBEntity.TableStore.COLUMN_PLATFORM} = ?", arrayOf(oldStore.name, oldStore.id, oldStore.platform))
    }

    fun deleteStore(store: Store) {
        writableDatabase.delete(DBEntity.TableStore.TABLE_NAME, "${DBEntity.TableStore.COLUMN_NAME} = ? and ${DBEntity.TableStore.COLUMN_ID} = ? and ${DBEntity.TableStore.COLUMN_PLATFORM} = ?", arrayOf(store.name, store.id, store.platform))
    }

    fun getAllSuppliers() : ArrayList<Supplier> {
        val suppliers = arrayListOf<Supplier>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableSupplier.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBEntity.TableSupplier.COLUMN_ID
            )

            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val supplier = Supplier(getString(0), getString(1), getString(2), getString(3), getString(4), getString(5))
                        suppliers.add(supplier)
                    } while (cursor.moveToNext())
                }

            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }
        return suppliers
    }

    fun getSupplierById(id: String) : Supplier? {
        var supplier: Supplier? = null
        val cursor = readableDatabase.query(
            DBEntity.TableSupplier.TABLE_NAME,
            null,
            "${DBEntity.TableSupplier.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            supplier = Supplier(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5))
        }

        cursor.close()
        return supplier
    }

    fun checkSupplierIsExisted(id: String) : Boolean {
        val cursor = readableDatabase.query(
            DBEntity.TableSupplier.TABLE_NAME,
            null,
            "${DBEntity.TableSupplier.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        return (cursor.count > 0).also {
            cursor.close()
        }
    }

    fun generateSupplierId() : String {
        var id = dateFormatter.format(Date()).substring(0, 4) + "01"

        val cursor = readableDatabase.query(
            DBEntity.TableSales.TABLE_NAME,
            arrayOf(DBEntity.TableSales.COLUMN_ID),
            "${DBEntity.TableSales.COLUMN_DATETIME} = ?",
            arrayOf(dateFormatter.format(Date()).substring(0, 4) + "%"),
            null,
            null,
            "${DBEntity.TableSales.COLUMN_ID} desc",
            "1"
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            var lastId = cursor.getInt(0)
            id = id.substring(0, 4) + (++lastId).toString().padStart(2, '0')
        }

        cursor.close()
        return id
    }

    fun insertSupplier(supplier: Supplier) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TableSupplier.COLUMN_ID, supplier.id)
            put(DBEntity.TableSupplier.COLUMN_NAME, supplier.name)
            put(DBEntity.TableSupplier.COLUMN_ADDRESS, supplier.address)
            put(DBEntity.TableSupplier.COLUMN_CITY, supplier.city)
            put(DBEntity.TableSupplier.COLUMN_PHONE_NUM, supplier.phone_num)
            put(DBEntity.TableSupplier.COLUMN_EMAIL, supplier.email)
        }

        writableDatabase.insert(DBEntity.TableSupplier.TABLE_NAME, null, contentValues)
    }

    fun updateSupplier(id: String, supplier: Supplier) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TableSupplier.COLUMN_ID, supplier.id)
            put(DBEntity.TableSupplier.COLUMN_NAME, supplier.name)
            put(DBEntity.TableSupplier.COLUMN_ADDRESS, supplier.address)
            put(DBEntity.TableSupplier.COLUMN_CITY, supplier.city)
            put(DBEntity.TableSupplier.COLUMN_PHONE_NUM, supplier.phone_num)
            put(DBEntity.TableSupplier.COLUMN_EMAIL, supplier.email)
        }

        writableDatabase.update(DBEntity.TableSupplier.TABLE_NAME, contentValues, "${DBEntity.TableSupplier.COLUMN_ID} = ?", arrayOf(id))
    }

    fun deleteSupplier(id: String) {
        writableDatabase.delete(DBEntity.TableSupplier.TABLE_NAME, "${DBEntity.TableSupplier.COLUMN_ID} = ?", arrayOf(id))
    }

    fun getAllCategories() : ArrayList<Category> {
        val categories = arrayListOf<Category>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableCategory.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBEntity.TableCategory.COLUMN_NAME
            )

            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val category = Category(getString(0), getString(1))
                        categories.add(category)
                    } while (cursor.moveToNext())
                }

            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }
        return categories
    }

    fun getAllCategoriesWithAmount() : ArrayList<Category> {
        val categories = arrayListOf<Category>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableCategory.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBEntity.TableCategory.COLUMN_NAME
            )

            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val category = Category(getString(0), getString(1))
                        val cursorTotalProduct = readableDatabase.rawQuery("select count(*) from ${DBEntity.TableProduct.TABLE_NAME} where ${DBEntity.TableProduct.COLUMN_CATEGORY_NAME} = ?", arrayOf(category.name))
                        cursorTotalProduct.moveToFirst()
                        cursorTotalProduct.close()
                        categories.add(category)
                    } while (cursor.moveToNext())
                }

            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }
        return categories
    }

    fun getCategoryByName(name: String) : Category? {
        var category: Category? = null
        val cursor = readableDatabase.query(
            DBEntity.TableCategory.TABLE_NAME,
            null,
            "${DBEntity.TableCategory.COLUMN_NAME} = ?",
            arrayOf(name),
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            category = Category(cursor.getString(0), cursor.getString(1))
        }

        cursor.close()
        return category
    }

    fun checkCategoryIsExisted(name: String) : Boolean {
        val cursor = readableDatabase.query(
            DBEntity.TableCategory.TABLE_NAME,
            null,
            "${DBEntity.TableCategory.COLUMN_NAME} = ?",
            arrayOf(name),
            null,
            null,
            null
        )
        return (cursor.count > 0).also {
            cursor.close()
        }
    }

    fun insertCategory(category: Category) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TableCategory.COLUMN_NAME, category.name)
            put(DBEntity.TableCategory.COLUMN_DESC, category.desc)
        }

        writableDatabase.insert(DBEntity.TableCategory.TABLE_NAME, null, contentValues)
    }

    fun updateCategory(name: String, category: Category) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TableCategory.COLUMN_NAME, category.name)
            put(DBEntity.TableCategory.COLUMN_DESC, category.desc)
        }

        writableDatabase.update(DBEntity.TableCategory.TABLE_NAME, contentValues, "${DBEntity.TableCategory.COLUMN_NAME} = ?", arrayOf(name))
    }

    fun deleteCategory(name: String) {
        writableDatabase.delete(DBEntity.TableCategory.TABLE_NAME, "${DBEntity.TableCategory.COLUMN_NAME} = ?", arrayOf(name))
    }

    fun getAllProducts() : ArrayList<Product> {
        val products = arrayListOf<Product>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableProduct.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBEntity.TableProduct.COLUMN_NAME
            )
            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val listVariant = arrayListOf<Pair<String, Int>>()
                        val stringVariant = getString(8)
                        if (stringVariant.isNotEmpty())
                            stringVariant.split(';').forEach {
                                with (it.split(':')){
                                    listVariant.add(Pair(this[0], this[1].toInt()))
                                }
                            }

                        val listImage = arrayListOf<String>()
                        getString(10).split(';').forEach {
                            listImage.add(it)
                        }

                        val product = Product(
                            getString(0),
                            getString(1),
                            getCategoryByName(getString(2)) ?: Category("", ""),
                            getInt(3),
                            getInt(4),
                            getInt(5),
                            getInt(6),
                            getString(7),
                            listVariant,
                            getSupplierById(getString(9)) ?: Supplier("", "", "", "", "", "")
                        )
                        product.images = listImage
                        products.add(product)
                    } while (moveToNext())
                }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }

        return products
    }

    fun getProductById(id: String) : Product? {
        var product : Product? = null
        val cursor = readableDatabase.query(
            DBEntity.TableProduct.TABLE_NAME,
            null,
            "${DBEntity.TableProduct.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            val listVariant = arrayListOf<Pair<String, Int>>()
            val stringVariant = cursor.getString(8)
            if (stringVariant.isNotEmpty())
                stringVariant.split(';').forEach {
                    with (it.split(':')){
                        listVariant.add(Pair(this[0], this[1].toInt()))
                    }
                }

            val listImage = arrayListOf<String>()
            cursor.getString(10).split(';').forEach {
                listImage.add(it)
            }
            product = Product(
                cursor.getString(0),
                cursor.getString(1),
                getCategoryByName(cursor.getString(2)) ?: Category("", ""),
                cursor.getInt(3),
                cursor.getInt(4),
                cursor.getInt(5),
                cursor.getInt(6),
                cursor.getString(7),
                listVariant,
                getSupplierById(cursor.getString(9)) ?: Supplier("", "", "", "", "", "")
            )
            product.images = listImage
        }
        cursor.close()
        return product
    }

    fun checkProductIsExisted(id: String) : Boolean {
        val cursor = readableDatabase.query(
            DBEntity.TableProduct.TABLE_NAME,
            null,
            "${DBEntity.TableProduct.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        return (cursor.count > 0).also {
            cursor.close()
        }
    }

    fun insertProduct(product: Product) {
        var stringVariants = ""
        for (i in product.variants)
            stringVariants += "${i.first}:${i.second};"
        if (stringVariants.isNotEmpty()) stringVariants = stringVariants.substring(0, stringVariants.length - 1)

        var stringImages = ""
        for (i in product.images)
            stringImages += "$i;"
        if (stringImages.length > 1) stringImages = stringImages.substring(0, stringImages.length - 1)

        val contentValues = ContentValues().apply {
            put(DBEntity.TableProduct.COLUMN_ID, product.id)
            put(DBEntity.TableProduct.COLUMN_NAME, product.name)
            put(DBEntity.TableProduct.COLUMN_CATEGORY_NAME, product.category.name)
            put(DBEntity.TableProduct.COLUMN_CAPITAL_PRICE, product.capital_price)
            put(DBEntity.TableProduct.COLUMN_WHOLESALE_PRICE, product.wholesale_price)
            put(DBEntity.TableProduct.COLUMN_RETAIL_PRICE, product.retail_price)
            put(DBEntity.TableProduct.COLUMN_TOTAL_STOCK, product.total_stock)
            put(DBEntity.TableProduct.COLUMN_VARIANT_NAME, product.variant_name)
            put(DBEntity.TableProduct.COLUMN_VARIANTS, stringVariants)
            put(DBEntity.TableProduct.COLUMN_SUPPLIER_ID, product.supplier.id)
            put(DBEntity.TableProduct.COLUMN_IMAGES, stringImages)
        }
        writableDatabase.insert(DBEntity.TableProduct.TABLE_NAME, null, contentValues)
    }

    fun updateProduct(id: String, product: Product) {
        var stringVariants = ""
        for (i in product.variants)
            stringVariants += "${i.first}:${i.second};"
        if (stringVariants.isNotEmpty()) stringVariants = stringVariants.substring(0, stringVariants.length - 1)

        var stringImages = ""
        for (i in product.images)
            stringImages += "$i;"
        if (stringImages.length > 1) stringImages = stringImages.substring(0, stringImages.length - 1)

        val contentValues = ContentValues().apply {
            put(DBEntity.TableProduct.COLUMN_ID, product.id)
            put(DBEntity.TableProduct.COLUMN_NAME, product.name)
            put(DBEntity.TableProduct.COLUMN_CATEGORY_NAME, product.category.name)
            put(DBEntity.TableProduct.COLUMN_CAPITAL_PRICE, product.capital_price)
            put(DBEntity.TableProduct.COLUMN_WHOLESALE_PRICE, product.wholesale_price)
            put(DBEntity.TableProduct.COLUMN_RETAIL_PRICE, product.retail_price)
            put(DBEntity.TableProduct.COLUMN_TOTAL_STOCK, product.total_stock)
            put(DBEntity.TableProduct.COLUMN_VARIANT_NAME, product.variant_name)
            put(DBEntity.TableProduct.COLUMN_VARIANTS, stringVariants)
            put(DBEntity.TableProduct.COLUMN_SUPPLIER_ID, product.supplier.id)
            put(DBEntity.TableProduct.COLUMN_IMAGES, stringImages)
        }
        writableDatabase.update(DBEntity.TableProduct.TABLE_NAME, contentValues, "${DBEntity.TableProduct.COLUMN_ID} = ?", arrayOf(id))
    }

    fun deleteProduct(id: String) {
        writableDatabase.delete(DBEntity.TableProduct.TABLE_NAME, "${DBEntity.TableProduct.COLUMN_ID} = ?", arrayOf(id))
    }

    fun getAllSales() : ArrayList<Sales> {
        val sales = arrayListOf<Sales>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableSales.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "${DBEntity.TableSales.COLUMN_DATETIME} DESC"
            )
            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val storeAttr = getString(1).split(';')
                        val sale = Sales(
                            getString(0),
                            getStoreByNameAndPlatform(storeAttr[0], storeAttr[1]) ?: Store("", "", ""),
                            getString(2),
                            getString(3),
                            getString(4),
                            dateFormatter.parse(getString(5)) ?: Calendar.getInstance().time,
                            getInt(6),
                            getInt(7)
                        )
                        sales.add(sale)
                    } while (moveToNext())
                }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }

        return sales
    }

    fun getSalesByDay(date: Date) : ArrayList<Sales> {
        val saless = arrayListOf<Sales>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableSales.TABLE_NAME,
                null,
                "${DBEntity.TableSales.COLUMN_DATETIME} like ?",
                arrayOf(dateFormatter.format(date).substring(0, 5) + "%"),
                null,
                null,
                "${DBEntity.TableSales.COLUMN_DATETIME} DESC"
            )
            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val storeAttr = getString(1).split(';')
                        val sales = Sales(
                            getString(0),
                            getStoreByNameAndPlatform(storeAttr[0], storeAttr[1]) ?: Store("", "", ""),
                            getString(2),
                            getString(3),
                            getString(4),
                            dateFormatter.parse(getString(5)) ?: Calendar.getInstance().time,
                            getInt(6),
                            getInt(7)
                        )
                        saless.add(sales)
                    } while (moveToNext())
                }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }

        return saless
    }

    fun getSalesById(id: String) : Sales? {
        var sales : Sales? = null
        val cursor = readableDatabase.query(
            DBEntity.TableSales.TABLE_NAME,
            null,
            "${DBEntity.TableSales.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            val storeAttr = cursor.getString(1).split(';')
            sales = Sales(
                cursor.getString(0),
                getStoreByNameAndPlatform(storeAttr[0], storeAttr[1]) ?: Store("", "",""),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                dateFormatter.parse(cursor.getString(5)) ?: Date(),
                cursor.getInt(6),
                cursor.getInt(7)
            )
            sales.listProduct = getSalesDetailsById(id)
        }
        cursor.close()
        return sales
    }

    fun generateSalesId() : String {
        var id = dateFormatter.format(Date()).substring(0, 6) + "0001"

        val cursor = readableDatabase.query(
            DBEntity.TableSales.TABLE_NAME,
            arrayOf(DBEntity.TableSales.COLUMN_ID),
            "${DBEntity.TableSales.COLUMN_DATETIME} = ?",
            arrayOf(dateFormatter.format(Date()).substring(0, 6) + "%"),
            null,
            null,
            "${DBEntity.TableSales.COLUMN_ID} desc",
            "1"
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            var lastId = cursor.getInt(0)
            id = id.substring(0, 6) + (++lastId).toString().padStart(4, '0')
        }

        cursor.close()
        return id
    }

    fun getSalesDetailsById(id: String) : ArrayList<Product> {
        val products: ArrayList<Product> = arrayListOf()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TableSalesDetails.TABLE_NAME,
                null,
                "${DBEntity.TableSalesDetails.COLUMN_SALES_ID} = ?",
                arrayOf(id),
                null,
                null,
                null
            )

            if (cursor.count > 0) {
                with (cursor) {
                    moveToFirst()
                    do {
                        val product = getProductById(getString(1))
                        product?.totalItems = getInt(2)
                        product?.customPrice = getInt(4)
                        if (product?.variants?.size ?: 0 > 0) {
                            product?.variants?.forEach {
                                product.variantsChoosed[it.first] = 0
                            }
                        }
                        val variantsChoosedString = getString(3)
                        if (variantsChoosedString.isNotEmpty())
                            variantsChoosedString.split(';').forEach {
                                with (it.split(':')){
                                    product!!.variantsChoosed[this[0]] = this[1].toInt()
                                }
                            }
                        if (product != null) products.add(product)
                    } while (moveToNext())
                }
            }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }
        return products
    }

    fun insertSales(sales: Sales) {
        val store = sales.store.name + ";" + sales.store.platform

        val contentValues = ContentValues().apply {
            put(DBEntity.TableSales.COLUMN_ID, sales.id)
            put(DBEntity.TableSales.COLUMN_STORE, store)
            put(DBEntity.TableSales.COLUMN_TYPE, sales.type)
            put(DBEntity.TableSales.COLUMN_PAYMENT_STATUS, sales.payment_status)
            put(DBEntity.TableSales.COLUMN_ITEM_STATUS, sales.item_status)
            put(DBEntity.TableSales.COLUMN_DATETIME, dateFormatter.format(sales.datetime))
            put(DBEntity.TableSales.COLUMN_TOTAL_ITEM, sales.total_item)
            put(DBEntity.TableSales.COLUMN_TOTAL_PRICE, sales.total_purchase)
        }

        writableDatabase.insert(DBEntity.TableSales.TABLE_NAME, null, contentValues)
        sales.listProduct.forEach {
            insertSalesDetails(sales.id, it)
        }
    }

    fun updateSalesPaymentStatus(id: String, newStatus: String) {
        writableDatabase.execSQL("update ${DBEntity.TableSales.TABLE_NAME} set ${DBEntity.TableSales.COLUMN_PAYMENT_STATUS} = '$newStatus' where ${DBEntity.TableSales.COLUMN_ID} = ?", arrayOf(id))
    }

    fun updateSalesItemStatus(id: String, newStatus:String) {
        writableDatabase.execSQL("update ${DBEntity.TableSales.TABLE_NAME} set ${DBEntity.TableSales.COLUMN_ITEM_STATUS} = '$newStatus' where ${DBEntity.TableSales.COLUMN_ID} = ?", arrayOf(id))
    }

    fun updateSales(id: String, sales: Sales) {
        val store = sales.store.name + ";" + sales.store.platform

        val contentValues = ContentValues().apply {
            put(DBEntity.TableSales.COLUMN_ID, sales.id)
            put(DBEntity.TableSales.COLUMN_STORE, store)
            put(DBEntity.TableSales.COLUMN_TYPE, sales.type)
            put(DBEntity.TableSales.COLUMN_PAYMENT_STATUS, sales.payment_status)
            put(DBEntity.TableSales.COLUMN_ITEM_STATUS, sales.item_status)
            put(DBEntity.TableSales.COLUMN_DATETIME, dateFormatter.format(sales.datetime))
            put(DBEntity.TableSales.COLUMN_TOTAL_ITEM, sales.total_item)
            put(DBEntity.TableSales.COLUMN_TOTAL_PRICE, sales.total_purchase)
        }

        writableDatabase.update(DBEntity.TableSales.TABLE_NAME, contentValues, "${DBEntity.TableSales.COLUMN_ID} = ?", arrayOf(id))
        deleteSalesDetails(sales.id)
        sales.listProduct.forEach {
            insertSalesDetails(sales.id, it)
        }
    }

    fun cancelSales(id: String) {
        deleteSalesDetails(id)
        writableDatabase.execSQL("update ${DBEntity.TableSales.TABLE_NAME} set ${DBEntity.TableSales.COLUMN_PAYMENT_STATUS} = 'canceled', ${DBEntity.TableSales.COLUMN_ITEM_STATUS} = 'canceled' where ${DBEntity.TableSales.COLUMN_ID} = ?", arrayOf(id))
    }

    fun insertSalesDetails(trans_id: String, product: Product) {
        var stringVariants = ""
        var stringVariantsRemain = ""
        for (i in product.variants) {
            stringVariantsRemain += "${i.first}:${i.second - (product.variantsChoosed[i.first] ?: 0)};"
            stringVariants += "${i.first}:${product.variantsChoosed[i.first] ?: 0};"
        }
        if (stringVariants.isNotEmpty()) stringVariants = stringVariants.substring(0, stringVariants.length - 1)
        if (stringVariantsRemain.isNotEmpty()) stringVariantsRemain = stringVariantsRemain.substring(0, stringVariantsRemain.length - 1)

        writableDatabase.execSQL("update ${DBEntity.TableProduct.TABLE_NAME} set ${DBEntity.TableProduct.COLUMN_VARIANTS} = ? where ${DBEntity.TableProduct.COLUMN_ID} = ?", arrayOf(stringVariantsRemain, product.id))
        val contentValues = ContentValues().apply {
            put(DBEntity.TableSalesDetails.COLUMN_SALES_ID, trans_id)
            put(DBEntity.TableSalesDetails.COLUMN_PRODUCT_ID, product.id)
            put(DBEntity.TableSalesDetails.COLUMN_AMOUNT, product.totalItems)
            put(DBEntity.TableSalesDetails.COLUMN_VARIANTS, stringVariants)
            put(DBEntity.TableSalesDetails.COLUMN_PRICE, product.customPrice)
        }
        writableDatabase.insert(DBEntity.TableSalesDetails.TABLE_NAME, null, contentValues)
    }

    fun deleteSalesDetails(trans_id: String) {
        val productList = getSalesDetailsById(trans_id)
        productList.forEach {
            var stringVariantsRemain = ""
            for (i in it.variants)
                stringVariantsRemain += "${i.first}:${i.second + (it.variantsChoosed[i.first] ?: 0)};"
            if (stringVariantsRemain.isNotEmpty()) stringVariantsRemain = stringVariantsRemain.substring(0, stringVariantsRemain.length - 1)
            writableDatabase.execSQL("update ${DBEntity.TableProduct.TABLE_NAME} set ${DBEntity.TableProduct.COLUMN_VARIANTS} = ? where ${DBEntity.TableProduct.COLUMN_ID} = ?", arrayOf(stringVariantsRemain, it.id))
        }

        try {
            writableDatabase.beginTransaction()
            writableDatabase.delete(DBEntity.TableSalesDetails.TABLE_NAME, "${DBEntity.TableSalesDetails.COLUMN_SALES_ID} = ?", arrayOf(trans_id))
            writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun getAllPurchase() : ArrayList<Purchase> {
        val purchases = arrayListOf<Purchase>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TablePurchase.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "${DBEntity.TablePurchase.COLUMN_DATETIME} DESC"
            )
            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val purchase = Purchase(
                            getString(0),
                            getSupplierById(getString(1)) ?: Supplier("", "", "", "", "", ""),
                            dateFormatter.parse(getString(2)) ?: Date(),
                            getString(3),
                            getString(4),
                            getInt(5),
                            getInt(6),
                            getInt(7)
                        )
                        purchases.add(purchase)
                    } while (moveToNext())
                }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }

        return purchases
    }

    fun getPurchaseSinceUntil() : ArrayList<Purchase> {
        return arrayListOf()
    }

    fun getPurchasesInDebt() : ArrayList<Purchase> {
        val purchases = arrayListOf<Purchase>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TablePurchase.TABLE_NAME,
                null,
                "${DBEntity.TablePurchase.COLUMN_TOTAL_PURCHASE} - ${DBEntity.TablePurchase.COLUMN_TOTAL_PAID} > 0",
                null,
                null,
                null,
                "${DBEntity.TablePurchase.COLUMN_DATETIME} DESC"
            )
            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val purchase = Purchase(
                            getString(0),
                            getSupplierById(getString(1)) ?: Supplier("", "", "", "", "", ""),
                            dateFormatter.parse(getString(2)) ?: Date(),
                            getString(3),
                            getString(4),
                            getInt(5),
                            getInt(6),
                            getInt(7)
                        )
                        purchases.add(purchase)
                    } while (moveToNext())
                }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }

        return purchases
    }

    fun getPurchaseById(id: String) : Purchase? {
        var purchase : Purchase? = null
        val cursor = readableDatabase.query(
            DBEntity.TablePurchase.TABLE_NAME,
            null,
            "${DBEntity.TablePurchase.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            purchase = Purchase(
                cursor.getString(0),
                getSupplierById(cursor.getString(1)) ?: Supplier("", "","", "", "", ""),
                dateFormatter.parse(cursor.getString(2)) ?: Date(),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getInt(5),
                cursor.getInt(6),
                cursor.getInt(7)
            )
            purchase.listProduct = getPurchaseDetailsById(id)
        }
        cursor.close()
        return purchase
    }

    fun getPurchaseDetailsById(id: String) : ArrayList<Product> {
        val products: ArrayList<Product> = arrayListOf()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TablePurchaseDetails.TABLE_NAME,
                null,
                "${DBEntity.TablePurchaseDetails.COLUMN_PURCHASE_ID} = ?",
                arrayOf(id),
                null,
                null,
                null
            )

            if (cursor.count > 0) {
                with (cursor) {
                    moveToFirst()
                    do {
                        val product = getProductById(getString(1))
                        product?.totalItems = getInt(2)
                        product?.customPrice = getInt(3)
                        if (product != null) products.add(product)
                    } while (moveToNext())
                }
            }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }
        return products
    }

    fun insertPurchase(purchase: Purchase) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TablePurchase.COLUMN_ID, purchase.id)
            put(DBEntity.TablePurchase.COLUMN_SUPPLIER_ID, purchase.supplier.id)
            put(DBEntity.TablePurchase.COLUMN_DATETIME, dateFormatter.format(purchase.datetime))
            put(DBEntity.TablePurchase.COLUMN_ITEM_CHECKED_STATUS, purchase.item_checked)
            put(DBEntity.TablePurchase.COLUMN_PAYMENT_METHOD, purchase.payment_method)
            put(DBEntity.TablePurchase.COLUMN_TOTAL_PAID, purchase.total_paid)
            put(DBEntity.TablePurchase.COLUMN_TOTAL_ITEM, purchase.total_item)
            put(DBEntity.TablePurchase.COLUMN_TOTAL_PURCHASE, purchase.total_purchase)
        }

        writableDatabase.insert(DBEntity.TablePurchase.TABLE_NAME, null, contentValues)
        purchase.listProduct.forEach {
            insertPurchaseDetails(purchase.id, it)
        }
    }

    fun updatePurchaseItemCheckedStatus(id: String, newStatus: String) {
        writableDatabase.execSQL("update ${DBEntity.TablePurchase.TABLE_NAME} set ${DBEntity.TablePurchase.COLUMN_ITEM_CHECKED_STATUS} = '$newStatus' where ${DBEntity.TablePurchase.COLUMN_ID} = ?", arrayOf(id))
    }

    fun updatePurchase(id: String, purchase: Purchase) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TablePurchase.COLUMN_ID, purchase.id)
            put(DBEntity.TablePurchase.COLUMN_SUPPLIER_ID, purchase.supplier.id)
            put(DBEntity.TablePurchase.COLUMN_DATETIME, dateFormatter.format(purchase.datetime))
            put(DBEntity.TablePurchase.COLUMN_ITEM_CHECKED_STATUS, purchase.item_checked)
            put(DBEntity.TablePurchase.COLUMN_PAYMENT_METHOD, purchase.payment_method)
            put(DBEntity.TablePurchase.COLUMN_TOTAL_PAID, purchase.total_paid)
            put(DBEntity.TablePurchase.COLUMN_TOTAL_ITEM, purchase.total_item)
            put(DBEntity.TablePurchase.COLUMN_TOTAL_PURCHASE, purchase.total_purchase)
        }

        writableDatabase.update(DBEntity.TablePurchase.TABLE_NAME, contentValues, "${DBEntity.TablePurchase.COLUMN_ID} = ?", arrayOf(id))
        deletePurchaseDetails(purchase.id)
        purchase.listProduct.forEach {
            insertPurchaseDetails(purchase.id, it)
        }
    }

    fun cancelPurchase(id: String) {
        deletePurchaseDetails(id)
        writableDatabase.execSQL("update ${DBEntity.TablePurchase.TABLE_NAME} " +
                "set ${DBEntity.TablePurchase.COLUMN_ITEM_CHECKED_STATUS} = 'canceled', " +
                "${DBEntity.TablePurchase.COLUMN_TOTAL_PAID} = 0 where ${DBEntity.TablePurchase.COLUMN_ID} = ?", arrayOf(id))
    }

    private fun insertPurchaseDetails(purchase_id: String, product: Product) {
        var stringVariants = ""
        for (i in product.variantsChoosed)
            stringVariants += "${i.key}:${i.value};"
        if (stringVariants.isNotEmpty()) stringVariants = stringVariants.substring(0, stringVariants.length - 1)

        val contentValues = ContentValues().apply {
            put(DBEntity.TablePurchaseDetails.COLUMN_PURCHASE_ID, purchase_id)
            put(DBEntity.TablePurchaseDetails.COLUMN_PRODUCT_ID, product.id)
            put(DBEntity.TablePurchaseDetails.COLUMN_AMOUNT, product.totalItems)
            put(DBEntity.TablePurchaseDetails.COLUMN_VARIANTS, stringVariants)
            put(DBEntity.TablePurchaseDetails.COLUMN_PRICE, product.customPrice)
        }
        writableDatabase.insert(DBEntity.TablePurchaseDetails.TABLE_NAME, null, contentValues)
    }

    private fun deletePurchaseDetails(purchase_id: String) {
        try {
            writableDatabase.beginTransaction()
            writableDatabase.delete(DBEntity.TablePurchaseDetails.TABLE_NAME, "${DBEntity.TablePurchaseDetails.COLUMN_PURCHASE_ID} = ?", arrayOf(purchase_id))
            writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun getAllPayment() : ArrayList<Payment> {
        val payments = arrayListOf<Payment>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TablePayment.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "${DBEntity.TablePayment.COLUMN_DATETIME} DESC"
            )
            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val payment = Payment(
                            getString(0),
                            getString(1),
                            dateFormatter.parse(getString(2)) ?: Date(),
                            getInt(3),
                            getString(4)
                        )
                        payments.add(payment)
                    } while (moveToNext())
                }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }

        return payments
    }

    fun getPaymentsByPurchaseId(purchaseId: String) : ArrayList<Payment> {
        val payments = arrayListOf<Payment>()
        try {
            readableDatabase.beginTransaction()
            val cursor = readableDatabase.query(
                DBEntity.TablePayment.TABLE_NAME,
                null,
                "${DBEntity.TablePayment.COLUMN_PURCHASE_ID} = ?",
                arrayOf(purchaseId),
                null,
                null,
                "${DBEntity.TablePayment.COLUMN_ID} DESC"
            )
            if (cursor.count > 0)
                with (cursor) {
                    moveToFirst()
                    do {
                        val payment = Payment(
                            getString(0),
                            getString(1),
                            dateFormatter.parse(getString(2)) ?: Date(),
                            getInt(3),
                            getString(4)
                        )
                        payments.add(payment)
                    } while (moveToNext())
                }
            cursor.close()
            readableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            readableDatabase.endTransaction()
        }

        return payments
    }

    fun generatePaymentIdForPurchase(purchaseId: String): String {
        var id = "$purchaseId.001"

        val cursor = readableDatabase.query(
            DBEntity.TablePayment.TABLE_NAME,
            null,
            "${DBEntity.TablePayment.COLUMN_PURCHASE_ID} = ?",
            arrayOf(purchaseId),
            null,
            null,
            "${DBEntity.TablePayment.COLUMN_ID} DESC",
            "1"
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            var lastId = cursor.getString(0).split('.')[1].toInt()
            id = "$purchaseId.${(++lastId).toString().padStart(4, '0')}"
        }

        cursor.close()
        return id
    }

    fun insertPayment(payment: Payment) {
        val contentValues = ContentValues().apply {
            put(DBEntity.TablePayment.COLUMN_ID, payment.id)
            put(DBEntity.TablePayment.COLUMN_PURCHASE_ID, payment.purchase_id)
            put(DBEntity.TablePayment.COLUMN_DATETIME, dateFormatter.format(payment.datetime))
            put(DBEntity.TablePayment.COLUMN_TOTAL_PAYMENT, payment.total_paid)
            put(DBEntity.TablePayment.COLUMN_MESSAGE, payment.message)
        }

        writableDatabase.insert(DBEntity.TablePayment.TABLE_NAME, null, contentValues)
    }

    fun deletePayment(paymentId: String) {
        writableDatabase.delete(DBEntity.TablePayment.TABLE_NAME, "${DBEntity.TablePayment.COLUMN_ID} = ?", arrayOf(paymentId))
    }

    fun deletePaymentByPurchaseId(purchaseId: String) {
        try {
            writableDatabase.beginTransaction()
            writableDatabase.delete(DBEntity.TablePayment.TABLE_NAME, "${DBEntity.TablePayment.COLUMN_PURCHASE_ID} = ?", arrayOf(purchaseId))
            writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            writableDatabase.endTransaction()
        }
    }
}