package domain_accounting.term.accounting

import com.github.michaelbull.result.*
import common.primitive.NonEmptyString

data class Account(
    val code: NonEmptyString,
    val name: NonEmptyString,
    val type: AccountType
) {
    companion object {
        fun from(
            code: String,
            name: String,
            type: AccountType
        ): Result<Account, String> {
            return NonEmptyString.from(code).andThen{ accountCode ->
                NonEmptyString.from(name).map { accountName ->
                    Account(
                        code = accountCode,
                        name = accountName,
                        type = type
                    )
                }
            }
        }
    }
}

