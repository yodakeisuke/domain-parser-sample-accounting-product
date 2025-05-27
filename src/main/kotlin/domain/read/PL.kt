package domain.read.pl

import domain.term.journal_data.JournalLine
import domain.term.accounting.AccountType
import domain.term.accounting.SignNormalization
import java.math.BigDecimal

data class PL(
    val revenueItems: List<AccountBalance>,
    val expenseItems: List<AccountBalance>,
    val totalRevenue: BigDecimal,
    val totalExpense: BigDecimal,
    val profit: BigDecimal
) {
    companion object {
        fun from(journalLines: List<JournalLine>): PL {
            val revenueItems = AccountBalance.from(journalLines, AccountType.REVENUE)
            val expenseItems = AccountBalance.from(journalLines, AccountType.EXPENSE)
            
            val totalRevenue = revenueItems.sumOf { it.balance }
            val totalExpense = expenseItems.sumOf { it.balance }

            return PL(
                revenueItems = revenueItems,
                expenseItems = expenseItems,
                totalRevenue = totalRevenue,
                totalExpense = totalExpense,
                profit = totalRevenue - totalExpense,
            )
        }
    }
}

data class AccountBalance(
    val accountCode: String,
    val accountName: String,
    val balance: BigDecimal
) {
    companion object {
        fun from(
            journalLines: List<JournalLine>,
            accountType: AccountType
        ): List<AccountBalance> {
            return journalLines
                .filter { it.account.type == accountType }
                .let { JournalLine.aggregateByAccount(it, SignNormalization::normalize) }
                .map { (account, balance) ->
                    AccountBalance(
                        accountCode = account.code.value,
                        accountName = account.name.value,
                        balance = balance
                    )
                }
                .sortedBy { it.accountCode }
        }
    }
}