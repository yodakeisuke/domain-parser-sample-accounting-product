package domain.term.journal_data

import domain.term.accounting.Account
import domain.term.accounting.AccountingAmount
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