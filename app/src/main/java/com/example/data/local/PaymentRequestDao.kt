package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PaymentRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRequestDao {
    @Query("SELECT * FROM payment_requests ORDER BY submissionDateMillis DESC")
    fun getAllRequests(): Flow<List<PaymentRequestEntity>>

    @Query("SELECT * FROM payment_requests WHERE status = 'PENDING' ORDER BY submissionDateMillis DESC")
    fun getPendingRequests(): Flow<List<PaymentRequestEntity>>

    @Query("SELECT * FROM payment_requests WHERE customerId = :customerId ORDER BY submissionDateMillis DESC")
    fun getRequestsByCustomerId(customerId: Long): Flow<List<PaymentRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: PaymentRequestEntity): Long

    @Update
    suspend fun updateRequest(request: PaymentRequestEntity)

    @Delete
    suspend fun deleteRequest(request: PaymentRequestEntity)

    @Query("DELETE FROM payment_requests WHERE id = :id")
    suspend fun deleteById(id: Long)
}
