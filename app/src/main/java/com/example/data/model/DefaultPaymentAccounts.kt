package com.example.data.model

object DefaultPaymentAccounts {
    val defaultList = listOf(
        // ----------------------------------------------------
        // বিক্রয় এর পেমেন্ট ক্ষেত্রে (জমা / ঢুকেছে (+))
        // প্রাপক নম্বর / কোড (Receiver)
        // ----------------------------------------------------
        // বিকাশ একাউন্ট
        // 1/ 2774☞B/S
        // 2/ 2689☞B/S
        // 3/ 5822☞B/S
        // 4/ 8960☞B/S
        // 5/ 3915☞B/A
        // 6/ 8787☞B/P
        PaymentAccountEntity(
            accountNumber = "2774",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "বিকাশ স্টোর / মার্চেন্ট",
            sortOrder = 1
        ),
        PaymentAccountEntity(
            accountNumber = "2689",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "বিকাশ স্টোর / মার্চেন্ট",
            sortOrder = 2
        ),
        PaymentAccountEntity(
            accountNumber = "5822",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "বিকাশ স্টোর / মার্চেন্ট",
            sortOrder = 3
        ),
        PaymentAccountEntity(
            accountNumber = "8960",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "বিকাশ স্টোর / মার্চেন্ট",
            sortOrder = 4
        ),
        PaymentAccountEntity(
            accountNumber = "3915",
            accountCode = "B/A",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "বিকাশ এজেন্ট",
            sortOrder = 5
        ),
        PaymentAccountEntity(
            accountNumber = "8787",
            accountCode = "B/P",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "বিকাশ পার্সোনাল",
            sortOrder = 6
        ),

        // নগদ একাউন্ট
        // 1/ 2774☞N/S
        // 2/ 2689☞N/S
        // 3/ 6965☞N/A
        // 4/ 8787☞N/P
        PaymentAccountEntity(
            accountNumber = "2774",
            accountCode = "N/S",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "নগদ স্টোর / মার্চেন্ট",
            sortOrder = 7
        ),
        PaymentAccountEntity(
            accountNumber = "2689",
            accountCode = "N/S",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "নগদ স্টোর / মার্চেন্ট",
            sortOrder = 8
        ),
        PaymentAccountEntity(
            accountNumber = "6965",
            accountCode = "N/A",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "নগদ এজেন্ট",
            sortOrder = 9
        ),
        PaymentAccountEntity(
            accountNumber = "8787",
            accountCode = "N/P",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "নগদ পার্সোনাল",
            sortOrder = 10
        ),

        // ক্যাশ টাকা
        PaymentAccountEntity(
            accountNumber = "Cash",
            accountCode = "ক্যাশ",
            method = PaymentMethod.CASH.name,
            scope = AccountScope.SALE_RECEIVER.name,
            note = "ক্যাশ টাকা (কাউন্টার)",
            sortOrder = 11
        ),

        // ----------------------------------------------------
        // ক্রয় এর পেমেন্ট ক্ষেত্রে (খরচ / গেছে (-))
        // প্রেরক নম্বর / কোড (Sender) - আমাদের একাউন্ট
        // ----------------------------------------------------
        // বিকাশ একাউন্ট
        // 1/ 2774☞B/S
        // 2/ 2689☞B/S
        // 3/ 5822☞B/S
        // 4/ 8960☞B/S
        // 3/ 915☞B/A
        // 4/ 787☞B/P
        PaymentAccountEntity(
            accountNumber = "2774",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "বিকাশ স্টোর",
            sortOrder = 12
        ),
        PaymentAccountEntity(
            accountNumber = "2689",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "বিকাশ স্টোর",
            sortOrder = 13
        ),
        PaymentAccountEntity(
            accountNumber = "5822",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "বিকাশ স্টোর",
            sortOrder = 14
        ),
        PaymentAccountEntity(
            accountNumber = "8960",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "বিকাশ স্টোর",
            sortOrder = 15
        ),
        PaymentAccountEntity(
            accountNumber = "915",
            accountCode = "B/A",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "বিকাশ এজেন্ট (3915)",
            sortOrder = 16
        ),
        PaymentAccountEntity(
            accountNumber = "787",
            accountCode = "B/P",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "বিকাশ পার্সোনাল (8787)",
            sortOrder = 17
        ),

        // নগদ একাউন্ট
        // 1/ 2774☞N/S
        // 2/ 2689☞N/S
        // 3/ 965☞N/A
        // 4/ 787☞N/P
        PaymentAccountEntity(
            accountNumber = "2774",
            accountCode = "N/S",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "নগদ স্টোর",
            sortOrder = 18
        ),
        PaymentAccountEntity(
            accountNumber = "2689",
            accountCode = "N/S",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "নগদ স্টোর",
            sortOrder = 19
        ),
        PaymentAccountEntity(
            accountNumber = "965",
            accountCode = "N/A",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "নগদ এজেন্ট (6965)",
            sortOrder = 20
        ),
        PaymentAccountEntity(
            accountNumber = "787",
            accountCode = "N/P",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "নগদ পার্সোনাল (8787)",
            sortOrder = 21
        ),

        // ক্যাশ টাকা(Case)
        PaymentAccountEntity(
            accountNumber = "Cash",
            accountCode = "ক্যাশ",
            method = PaymentMethod.CASH.name,
            scope = AccountScope.BUY_SENDER.name,
            note = "ক্যাশ টাকা",
            sortOrder = 22
        ),

        // ----------------------------------------------------
        // ক্রয় এর পেমেন্ট ক্ষেত্রে
        // প্রাপক নম্বর / কোড (Receiver) - মহাজন / সাপ্লায়ার
        // ----------------------------------------------------
        // বিকাশ একাউন্ট
        // 1/ 4010☞B/S
        // 2/ 2050☞B/S
        // 3/ 3915☞B/A
        // 4/ 8787☞B/P
        PaymentAccountEntity(
            accountNumber = "4010",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "মহাজন বিকাশ স্টোর",
            sortOrder = 23
        ),
        PaymentAccountEntity(
            accountNumber = "2050",
            accountCode = "B/S",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "মহাজন বিকাশ স্টোর",
            sortOrder = 24
        ),
        PaymentAccountEntity(
            accountNumber = "3915",
            accountCode = "B/A",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "মহাজন বিকাশ এজেন্ট",
            sortOrder = 25
        ),
        PaymentAccountEntity(
            accountNumber = "8787",
            accountCode = "B/P",
            method = PaymentMethod.BKASH.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "মহাজন বিকাশ পার্সোনাল",
            sortOrder = 26
        ),

        // নগদ একাউন্ট
        // 1/ 4010☞N/S
        // 2/ 2050☞N/S
        // 3/ 6965☞N/A
        // 4/ 8787☞N/P
        PaymentAccountEntity(
            accountNumber = "4010",
            accountCode = "N/S",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "মহাজন নগদ স্টোর",
            sortOrder = 27
        ),
        PaymentAccountEntity(
            accountNumber = "2050",
            accountCode = "N/S",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "মহাজন নগদ স্টোর",
            sortOrder = 28
        ),
        PaymentAccountEntity(
            accountNumber = "6965",
            accountCode = "N/A",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "মহাজন নগদ এজেন্ট",
            sortOrder = 29
        ),
        PaymentAccountEntity(
            accountNumber = "8787",
            accountCode = "N/P",
            method = PaymentMethod.NAGAD.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "মহাজন নগদ পার্সোনাল",
            sortOrder = 30
        ),

        // ক্যাশ টাকা(Case)
        PaymentAccountEntity(
            accountNumber = "Cash",
            accountCode = "ক্যাশ",
            method = PaymentMethod.CASH.name,
            scope = AccountScope.BUY_RECEIVER.name,
            note = "ক্যাশ টাকা",
            sortOrder = 31
        )
    )
}
