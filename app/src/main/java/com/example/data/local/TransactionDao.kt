package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateString = :date ORDER BY id DESC")
    fun getTransactionsByDate(date: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE method = :method ORDER BY dateMillis DESC, id DESC")
    fun getTransactionsByMethod(method: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'PERSONAL_EXPENSE' ORDER BY dateMillis DESC, id DESC")
    fun getPersonalExpenses(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE senderAccount = :account OR receiverAccount = :account ORDER BY dateMillis DESC, id DESC")
    fun getTransactionsForAccount(account: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
