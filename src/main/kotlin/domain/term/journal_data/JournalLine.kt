package domain.term.journal_data

import domain.term.accounting.Account
import domain.term.accounting.AccountingAmount
import domain.term.accounting.AccountType
import domain.term.accounting.DebitCredit
import domain.term.accounting.denormalizeSign
import java.math.BigDecimal

data class JournalLine(
    val account: Account,
    val amount: AccountingAmount,
    val description: String,
) {
    companion object {
        fun sumDebits(lines: List<JournalLine>): BigDecimal {
            return filterUnsignedByDebitCredit(lines, DebitCredit.DEBIT).sumOf { it }
        }
        fun sumCredits(lines: List<JournalLine>): BigDecimal {
            return filterUnsignedByDebitCredit(lines, DebitCredit.CREDIT).sumOf { it }
        }
        
        // 勘定科目別に集計（符号付き）
        fun aggregateByAccount(
            lines: List<JournalLine>,
            normalizeSign: (AccountType, DebitCredit) -> Int
        ): Map<Account, BigDecimal> {
            return lines
                .groupBy { it.account }
                .mapValues { (account, accountLines) ->
                    accountLines.sumOf { line ->
                        line.amount.toSigned(account.type, normalizeSign).value
                    }
                }
        }
    }
}

// helper
internal fun filterUnsignedByDebitCredit(
    lines: List<JournalLine>,
    targetType: DebitCredit
): List<BigDecimal> {
    return lines.mapNotNull { line ->
        when (val amount = line.amount) {
            is AccountingAmount.Unsigned ->
                amount.takeIf { it.debitCredit == targetType }?.amount?.value
            is AccountingAmount.Signed -> amount.toUnsigned(line.account.type, ::denormalizeSign)
                .takeIf { it.debitCredit == targetType }?.amount?.value
        }
    }
}