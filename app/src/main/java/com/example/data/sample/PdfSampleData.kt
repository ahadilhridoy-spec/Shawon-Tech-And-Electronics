package com.example.data.sample

import com.example.data.model.FlowDirection
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.util.Calendar

object PdfSampleData {

    fun getSampleTransactions(): List<TransactionEntity> {
        val list = mutableListOf<TransactionEntity>()

        fun getDateMillis(day: Int, month: Int = Calendar.SEPTEMBER, year: Int = 2026): Long {
            val cal = Calendar.getInstance()
            cal.set(year, month, day, 12, 0, 0)
            return cal.timeInMillis
        }

        // --- ১. ব্যক্তিভিত্তিক ও অন্যান্য খরচ (মোট ৮৬,০০০ টাকা) ---
        list.add(
            TransactionEntity(
                dateString = "১০ সেপ্টেম্বর",
                dateMillis = getDateMillis(10),
                type = TransactionType.PERSONAL_EXPENSE.name,
                method = PaymentMethod.CASH.name,
                flowDirection = FlowDirection.OUTGOING.name,
                amount = 37000.0,
                personName = "রানা ভাই",
                description = "20k + 4k + 8k + 5k",
                rawCode = "৩৭,০০০ (রানা ভাই)"
            )
        )
        list.add(
            TransactionEntity(
                dateString = "১০ সেপ্টেম্বর",
                dateMillis = getDateMillis(10),
                type = TransactionType.PERSONAL_EXPENSE.name,
                method = PaymentMethod.CASH.name,
                flowDirection = FlowDirection.OUTGOING.name,
                amount = 45000.0,
                personName = "মিরাজ ভাই",
                description = "30k + 15k",
                rawCode = "৪৫,০০০ (মিরাজ ভাই)"
            )
        )
        list.add(
            TransactionEntity(
                dateString = "১০ সেপ্টেম্বর",
                dateMillis = getDateMillis(10),
                type = TransactionType.PERSONAL_EXPENSE.name,
                method = PaymentMethod.CASH.name,
                flowDirection = FlowDirection.OUTGOING.name,
                amount = 4000.0,
                personName = "কেরিং খরচ",
                description = "Carry Cost",
                rawCode = "৪,০০০ (Carry Cost)"
            )
        )

        // --- ২. তারিখভিত্তিক লেনদেন (মোট ৭,৮৪,১৩৯ টাকা) ---

        // ১০ সেপ্টেম্বর (মোট ১,৩০,৫০০)
        val d10 = getDateMillis(10)
        list.add(TransactionEntity(dateString = "১০ সেপ্টেম্বর", dateMillis = d10, type = TransactionType.SELL.name, method = PaymentMethod.CASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 78000.0, rawCode = "78K", description = "পণ্য বিক্রয় ও ক্যাশ লেনদেন"))
        list.add(TransactionEntity(dateString = "১০ সেপ্টেম্বর", dateMillis = d10, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 30000.0, senderAccount = "915", receiverAccount = "689", rawCode = "30K (B 915/689)"))
        list.add(TransactionEntity(dateString = "১০ সেপ্টেম্বর", dateMillis = d10, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 13000.0, senderAccount = "4010", receiverAccount = "689", rawCode = "13K (B 4010/689)"))
        list.add(TransactionEntity(dateString = "১০ সেপ্টেম্বর", dateMillis = d10, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 9500.0, senderAccount = "4010", receiverAccount = "689", rawCode = "9.5K (N 4010/689)"))

        // ১১ সেপ্টেম্বর (মোট ৯৪,০০০)
        val d11 = getDateMillis(11)
        list.add(TransactionEntity(dateString = "১১ সেপ্টেম্বর", dateMillis = d11, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 7000.0, senderAccount = "2050", receiverAccount = "2774", rawCode = "7K (N 2050/2774)"))
        list.add(TransactionEntity(dateString = "১১ সেপ্টেম্বর", dateMillis = d11, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 50000.0, senderAccount = "2050", receiverAccount = "2689", rawCode = "50K (B 2050/2689)"))
        list.add(TransactionEntity(dateString = "১১ সেপ্টেম্বর", dateMillis = d11, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 24500.0, senderAccount = "2050", receiverAccount = "2689", rawCode = "24.5K (N 2050/2689)"))
        list.add(TransactionEntity(dateString = "১১ সেপ্টেম্বর", dateMillis = d11, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 12500.0, senderAccount = "2050", receiverAccount = "5822", rawCode = "12.5K (B 2050/5822)"))

        // ১২ সেপ্টেম্বর (মোট ৭৭,০৩৯)
        val d12 = getDateMillis(12)
        list.add(TransactionEntity(dateString = "১২ সেপ্টেম্বর", dateMillis = d12, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 16300.0, senderAccount = "3915", receiverAccount = "2774", rawCode = "16.3K (B 3915/2774)"))
        list.add(TransactionEntity(dateString = "১২ সেপ্টেম্বর", dateMillis = d12, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 9500.0, senderAccount = "2050", receiverAccount = "2774", rawCode = "9.5K (N 2050/2774)"))
        list.add(TransactionEntity(dateString = "১২ সেপ্টেম্বর", dateMillis = d12, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 6000.0, senderAccount = "2050", receiverAccount = "2689", rawCode = "6K (B 2050/2689)"))
        list.add(TransactionEntity(dateString = "১২ সেপ্টেম্বর", dateMillis = d12, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 37000.0, senderAccount = "2050", receiverAccount = "2689", rawCode = "37K (N 2050/2689)"))
        list.add(TransactionEntity(dateString = "১২ সেপ্টেম্বর", dateMillis = d12, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 8000.0, senderAccount = "2050", receiverAccount = "5822", rawCode = "8K (B 2050/5822)"))
        list.add(TransactionEntity(dateString = "১২ সেপ্টেম্বর", dateMillis = d12, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 239.0, senderAccount = "4010", receiverAccount = "2774", rawCode = "239 (B 4010/2774)"))

        // ১৩ সেপ্টেম্বর (মোট ৪৫,৫০০)
        val d13 = getDateMillis(13)
        list.add(TransactionEntity(dateString = "১৩ সেপ্টেম্বর", dateMillis = d13, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 12000.0, senderAccount = "2050", receiverAccount = "2774", rawCode = "12K (N 2050//2774)"))
        list.add(TransactionEntity(dateString = "১৩ সেপ্টেম্বর", dateMillis = d13, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 16000.0, senderAccount = "2050", receiverAccount = "2689", rawCode = "16K (B 2050//2689)"))
        list.add(TransactionEntity(dateString = "১৩ সেপ্টেম্বর", dateMillis = d13, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 17500.0, senderAccount = "2050", receiverAccount = "5822", rawCode = "17.5K (B 2050//5822)"))

        // ১৪ সেপ্টেম্বর (মোট ১,২৬,৫০০)
        val d14 = getDateMillis(14)
        list.add(TransactionEntity(dateString = "১৪ সেপ্টেম্বর", dateMillis = d14, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 50000.0, senderAccount = "965", receiverAccount = "408", rawCode = "50K (B 965/408)"))
        list.add(TransactionEntity(dateString = "১৪ সেপ্টেম্বর", dateMillis = d14, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 26500.0, senderAccount = "965", receiverAccount = "422", rawCode = "26.5K (B 965/422)"))
        list.add(TransactionEntity(dateString = "১৪ সেপ্টেম্বর", dateMillis = d14, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 10000.0, senderAccount = "965", receiverAccount = "760", rawCode = "10K (B 965/760)"))
        list.add(TransactionEntity(dateString = "১৪ সেপ্টেম্বর", dateMillis = d14, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 20000.0, senderAccount = "965", receiverAccount = "689", rawCode = "20K (B 965/689)"))
        list.add(TransactionEntity(dateString = "১৪ সেপ্টেম্বর", dateMillis = d14, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 20000.0, senderAccount = "965", receiverAccount = "689", rawCode = "20K (B 965/689)"))

        // ১৫ সেপ্টেম্বর (মোট ৩৫,০০০)
        val d15 = getDateMillis(15)
        list.add(TransactionEntity(dateString = "১৫ সেপ্টেম্বর", dateMillis = d15, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 35000.0, senderAccount = "2050", receiverAccount = "5822", rawCode = "35K (B 2050//5822)"))

        // ১৬ সেপ্টেম্বর (মোট ১,২৭,৫০০)
        val d16 = getDateMillis(16)
        list.add(TransactionEntity(dateString = "১৬ সেপ্টেম্বর", dateMillis = d16, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 8500.0, senderAccount = "2050", receiverAccount = "5822", rawCode = "8.5K (B 2050/5822)"))
        list.add(TransactionEntity(dateString = "১৬ সেপ্টেম্বর", dateMillis = d16, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 8500.0, senderAccount = "2050", receiverAccount = "2689", rawCode = "8.5K (N 2050/2689)"))
        list.add(TransactionEntity(dateString = "১৬ সেপ্টেম্বর", dateMillis = d16, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 30000.0, senderAccount = "2050", receiverAccount = "3915", rawCode = "30K (B 2050/3915)"))
        list.add(TransactionEntity(dateString = "১৬ সেপ্টেম্বর", dateMillis = d16, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 41500.0, senderAccount = "2050", receiverAccount = "5822", rawCode = "41.5K (B 2050/5822)"))
        list.add(TransactionEntity(dateString = "১৬ সেপ্টেম্বর", dateMillis = d16, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 16500.0, senderAccount = "2050", receiverAccount = "2774", rawCode = "16.5K (N 2050/2774)"))
        list.add(TransactionEntity(dateString = "১৬ সেপ্টেম্বর", dateMillis = d16, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 22500.0, senderAccount = "2050", receiverAccount = "2689", rawCode = "22.5K (B 2050/2689)"))

        // ১৭ সেপ্টেম্বর (মোট ৫৮,০০০)
        val d17 = getDateMillis(17)
        list.add(TransactionEntity(dateString = "১৭ সেপ্টেম্বর", dateMillis = d17, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 15500.0, senderAccount = "2050", receiverAccount = "5822", rawCode = "15.5K (B 2050/5822)"))
        list.add(TransactionEntity(dateString = "১৭ সেপ্টেম্বর", dateMillis = d17, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 18500.0, senderAccount = "2050", receiverAccount = "1988", rawCode = "18.5K (B 2050/1988)"))
        list.add(TransactionEntity(dateString = "১৭ সেপ্টেম্বর", dateMillis = d17, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 21500.0, senderAccount = "2050", receiverAccount = "7143", rawCode = "21.5K (B 2050/7143)"))
        list.add(TransactionEntity(dateString = "১৭ সেপ্টেম্বর", dateMillis = d17, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 2500.0, senderAccount = "3915", receiverAccount = "7723", rawCode = "2.5K (B 3915/7723)"))

        // ১৮ সেপ্টেম্বর (মোট ৭৮,৫০০)
        val d18 = getDateMillis(18)
        list.add(TransactionEntity(dateString = "১৮ সেপ্টেম্বর", dateMillis = d18, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 37500.0, senderAccount = "4010", receiverAccount = "5822", rawCode = "37.5K (B 4010/5822)"))
        list.add(TransactionEntity(dateString = "১৮ সেপ্টেম্বর", dateMillis = d18, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 7000.0, senderAccount = "2774", receiverAccount = "3915", rawCode = "7K (B 2774/3915)"))
        list.add(TransactionEntity(dateString = "১৮ সেপ্টেম্বর", dateMillis = d18, type = TransactionType.SELL.name, method = PaymentMethod.BKASH.name, flowDirection = FlowDirection.INCOMING.name, amount = 34000.0, senderAccount = "2774", receiverAccount = "4010", rawCode = "34K (B 2774/4010)"))

        // ১৯ সেপ্টেম্বর (মোট ৩১,৬০০)
        val d19 = getDateMillis(19)
        list.add(TransactionEntity(dateString = "১৯ সেপ্টেম্বর", dateMillis = d19, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 6600.0, senderAccount = "2774", receiverAccount = "2050", rawCode = "6.6K (N 2774/2050)"))
        list.add(TransactionEntity(dateString = "১৯ সেপ্টেম্বর", dateMillis = d19, type = TransactionType.SELL.name, method = PaymentMethod.NAGAD.name, flowDirection = FlowDirection.INCOMING.name, amount = 25000.0, senderAccount = "4010", receiverAccount = "2689", rawCode = "25K (N 4010/2689)"))

        return list
    }
}
