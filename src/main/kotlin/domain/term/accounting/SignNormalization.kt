package domain.term.accounting

object SignNormalization {
    fun normalize(
        accountType: AccountType,
        debitCredit: DebitCredit
    ): Int = when (accountType) {
        AccountType.ASSET, AccountType.EXPENSE -> when (debitCredit) {
            DebitCredit.DEBIT -> 1
            DebitCredit.CREDIT -> -1
        }
        AccountType.LIABILITY, AccountType.EQUITY, AccountType.REVENUE -> when (debitCredit) {
            DebitCredit.DEBIT -> -1
            DebitCredit.CREDIT -> 1
        }
    }

    fun denormalize(
        accountType: AccountType,
        isPositive: Boolean
    ): DebitCredit = when (accountType) {
        AccountType.ASSET, AccountType.EXPENSE -> 
            if (isPositive) DebitCredit.DEBIT else DebitCredit.CREDIT
        AccountType.LIABILITY, AccountType.EQUITY, AccountType.REVENUE -> 
            if (isPositive) DebitCredit.CREDIT else DebitCredit.DEBIT
    }
}