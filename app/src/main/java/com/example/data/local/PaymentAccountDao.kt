package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PaymentAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentAccountDao {

    @Query("SELECT * FROM payment_accounts ORDER BY sortOrder ASC, id ASC")
    fun getAllAccounts(): Flow<List<PaymentAccountEntity>>

    @Query("SELECT * FROM payment_accounts WHERE scope = :scope OR scope = 'ALL' ORDER BY sortOrder ASC, id ASC")
    fun getAccountsByScope(scope: String): Flow<List<PaymentAccountEntity>>

    @Query("SELECT * FROM payment_accounts WHERE method = :method ORDER BY sortOrder ASC, id ASC")
    fun getAccountsByMethod(method: String): Flow<List<PaymentAccountEntity>>

    @Query("SELECT * FROM payment_accounts WHERE (scope = :scope OR scope = 'ALL') AND method = :method ORDER BY sortOrder ASC, id ASC")
    fun getAccountsByScopeAndMethod(scope: String, method: String): Flow<List<PaymentAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: PaymentAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<PaymentAccountEntity>)

    @Update
    suspend fun updateAccount(account: PaymentAccountEntity)

    @Delete
    suspend fun deleteAccount(account: PaymentAccountEntity)

    @Query("DELETE FROM payment_accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM payment_accounts")
    suspend fun getAccountCount(): Int

    @Query("DELETE FROM payment_accounts")
    suspend fun clearAll()
}
