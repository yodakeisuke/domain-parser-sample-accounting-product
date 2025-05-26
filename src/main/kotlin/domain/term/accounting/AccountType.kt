package domain.term.accounting

enum class AccountType(val japaneseName: String) {
    ASSET("資産"),
    LIABILITY("負債"),
    EQUITY("資本"),
    REVENUE("収益"),
    EXPENSE("費用")
}