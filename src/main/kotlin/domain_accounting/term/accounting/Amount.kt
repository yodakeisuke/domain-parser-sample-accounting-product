package domain_accounting.term.accounting

import common.primitive.PositiveBigDecimal
import java.math.BigDecimal

sealed interface AccountingAmount {
    // states
    data class Unsigned(
        val amount: PositiveBigDecimal,
        val debitCredit: DebitCredit,
    ) :AccountingAmount
    @JvmInline
    value class Signed(
        val value: BigDecimal
    ) : AccountingAmount

    // transition actions
    fun toSigned(
        accountType: AccountType,
        normalizeSign: (AccountType, DebitCredit) -> Int,
    ): Signed = when (this) {
        is Unsigned -> {
            val sign = normalizeSign(accountType, debitCredit)
            Signed(amount.value.multiply(BigDecimal.valueOf(sign.toLong())))
        }
        is Signed -> this
    }

    fun toUnsigned(
        accountType: AccountType,
        denormalizeSign: (AccountType, Boolean) -> DebitCredit,
    ): Unsigned {
        return when (this) {
            is Unsigned -> this
            is Signed -> Unsigned(
                PositiveBigDecimal.fromTrusted(value.abs()),
                denormalizeSign(accountType, value >= BigDecimal.ZERO)
            )
        }
    }
}