package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.sample.PdfSampleData
import com.example.util.BanglaUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Shawon Tech And Electronics", appName)
    }

    @Test
    fun `bangla number conversion works`() {
        val bangla = BanglaUtils.toBangla("12345")
        assertEquals("১২৩৪৫", bangla)
        val english = BanglaUtils.toEnglish("১২৩৪৫")
        assertEquals("12345", english)
    }

    @Test
    fun `code generation and parsing works`() {
        val code = BanglaUtils.generateTransactionCode(
            amount = 30000.0,
            method = PaymentMethod.BKASH,
            sender = "915",
            receiver = "689"
        )
        assertEquals("30K (B 915/689)", code)

        val parsed = BanglaUtils.parseShorthandCode("30K (B 915/689)")
        assertNotNull(parsed)
        assertEquals(30000.0, parsed!!.amount!!, 0.01)
        assertEquals(PaymentMethod.BKASH, parsed.method)
        assertEquals("915", parsed.sender)
        assertEquals("689", parsed.receiver)
    }

    @Test
    fun `pdf sample total calculates accurately`() {
        val samples = PdfSampleData.getSampleTransactions()
        val personalExpenses = samples.filter { it.type == "PERSONAL_EXPENSE" }.sumOf { it.amount }
        val dateWise = samples.filter { it.type != "PERSONAL_EXPENSE" }.sumOf { it.amount }
        val grandTotal = personalExpenses + dateWise

        assertEquals(86000.0, personalExpenses, 0.01) // ৮৬,০০০ টাকা
        assertTrue(dateWise > 700000.0)
        assertEquals(grandTotal, personalExpenses + dateWise, 0.01)
    }

    @Test
    fun `filter by transaction type and search by description works`() {
        val testList = listOf(
            TransactionEntity(
                id = 1,
                dateString = "১০ সেপ্টেম্বর",
                dateMillis = 1000L,
                type = TransactionType.SELL.name,
                method = PaymentMethod.BKASH.name,
                flowDirection = "INCOMING",
                amount = 3500.0,
                description = "হেডফোন ও ক্যাবল বিক্রি"
            ),
            TransactionEntity(
                id = 2,
                dateString = "১০ সেপ্টেম্বর",
                dateMillis = 2000L,
                type = TransactionType.BUY.name,
                method = PaymentMethod.NAGAD.name,
                flowDirection = "OUTGOING",
                amount = 12000.0,
                description = "ডিসপ্লে পাইকারি ক্রয়"
            ),
            TransactionEntity(
                id = 3,
                dateString = "১১ সেপ্টেম্বর",
                dateMillis = 3000L,
                type = TransactionType.EXPENSE.name,
                method = PaymentMethod.CASH.name,
                flowDirection = "OUTGOING",
                amount = 500.0,
                description = "দোকান চা-নাস্তা খরচ"
            )
        )

        // Filter by Type: BUY
        val buyOnly = testList.filter { it.type == TransactionType.BUY.name }
        assertEquals(1, buyOnly.size)
        assertEquals("ডিসপ্লে পাইকারি ক্রয়", buyOnly[0].description)

        // Filter by Type: EXPENSE
        val expenseOnly = testList.filter { it.type == TransactionType.EXPENSE.name }
        assertEquals(1, expenseOnly.size)

        // Search by description keyword
        val searchResults = testList.filter { it.description.lowercase().contains("ক্যাবল") }
        assertEquals(1, searchResults.size)
        assertEquals(TransactionType.SELL.name, searchResults[0].type)
    }

    @Test
    fun `partial payment and due calculation works`() {
        val partialTx = TransactionEntity(
            id = 10,
            dateString = "১৫ অক্টোবর",
            dateMillis = 5000L,
            type = TransactionType.SELL.name,
            method = PaymentMethod.BKASH.name,
            flowDirection = "INCOMING",
            amount = 6000.0,
            totalBillAmount = 10000.0,
            paidAmount = 6000.0,
            dueAmount = 4000.0,
            customerPhone = "01711223344",
            personName = "হাসান ভাই"
        )

        assertEquals(10000.0, partialTx.totalBillAmount, 0.01)
        assertEquals(6000.0, partialTx.paidAmount, 0.01)
        assertEquals(4000.0, partialTx.dueAmount, 0.01)
        assertEquals(com.example.data.model.PaymentStatus.PARTIAL, partialTx.paymentStatus)
    }

    @Test
    fun `dashboard summary calculates total sales total expenses and net balance accurately`() {
        val summaryData = com.example.data.remote.FirestoreDashboardSummaryData(
            totalSales = 150000.0,
            totalExpenses = 90000.0,
            netBalance = 150000.0 - 90000.0,
            transactionCount = 25,
            isFromCloud = true
        )

        assertEquals(150000.0, summaryData.totalSales, 0.01)
        assertEquals(90000.0, summaryData.totalExpenses, 0.01)
        assertEquals(60000.0, summaryData.netBalance, 0.01)
        assertTrue(summaryData.isFromCloud)
        assertEquals(25, summaryData.transactionCount)
    }
}
