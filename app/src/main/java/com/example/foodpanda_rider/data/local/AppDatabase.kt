package com.example.foodpanda_rider.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderJson: String,
    val isRiderAccept: Boolean,
    val isDelivered: Boolean,
    val riderUid: String?,
    val time: Long,
    val deliveredTime: Long
)

@Entity(tableName = "rider_accounts")
data class RiderAccountEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val phoneNumber: String,
    val imageUrl: String,
    val emailVerified: Boolean,
    val isApproved: Boolean
)

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE isRiderAccept = 0 AND isDelivered = 0 ORDER BY time DESC")
    fun observeAvailableOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE isRiderAccept = 1 AND isDelivered = 0 ORDER BY time DESC")
    fun observeCurrentOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE isDelivered = 1 ORDER BY deliveredTime DESC, time DESC")
    fun observeDeliveredOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE isDelivered = 1 ORDER BY deliveredTime DESC, time DESC")
    suspend fun getDeliveredOrdersOnce(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun observeOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrders(orders: List<OrderEntity>)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}

@Dao
interface RiderAccountDao {
    @Query("SELECT * FROM rider_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getByEmail(email: String): RiderAccountEntity?

    @Query("SELECT * FROM rider_accounts WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): RiderAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: RiderAccountEntity)
}

@Database(
    entities = [OrderEntity::class, RiderAccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun riderAccountDao(): RiderAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodpanda_rider.db"
                ).fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
