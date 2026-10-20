package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CustomerEntity
import com.example.data.model.PaymentAccountEntity
import com.example.data.model.PaymentRequestEntity
import com.example.data.model.ProductItemEntity
import com.example.data.model.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        PaymentAccountEntity::class,
        CustomerEntity::class,
        PaymentRequestEntity::class,
        ProductItemEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun paymentAccountDao(): PaymentAccountDao
    abstract fun customerDao(): CustomerDao
    abstract fun paymentRequestDao(): PaymentRequestDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shawon_tech_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
