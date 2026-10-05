package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DeliveryDao
import com.example.data.model.*

@Database(
    entities = [
        UserProfileEntity::class,
        CourierPresenceEntity::class,
        ServiceAreaEntity::class,
        OrderEntity::class,
        OrderEventEntity::class,
        CashLedgerEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deliveryDao(): DeliveryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chefchaouen_delivery.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
