package domain.read

import domain.term.accounting.Account
import domain.term.accounting.AccountType
import com.github.michaelbull.result.*
import common.primitive.NonEmptyString

// 科目一覧のリードモデル
object AccountList {
    // インメモリで定義された科目マスタ
    private val accounts = listOfNotNull(
        // 資産
        createAccount("1010", "現金", AccountType.ASSET),
        createAccount("1020", "当座預金", AccountType.ASSET),
        createAccount("1030", "普通預金", AccountType.ASSET),
        createAccount("1210", "売掛金", AccountType.ASSET),
        createAccount("1410", "商品", AccountType.ASSET),
        createAccount("1610", "建物", AccountType.ASSET),
        createAccount("1620", "備品", AccountType.ASSET),

        // 負債
        createAccount("2110", "買掛金", AccountType.LIABILITY),
        createAccount("2210", "短期借入金", AccountType.LIABILITY),
        createAccount("2310", "未払金", AccountType.LIABILITY),
        createAccount("2410", "前受金", AccountType.LIABILITY),

        // 純資産
        createAccount("3110", "資本金", AccountType.EQUITY),
        createAccount("3210", "利益剰余金", AccountType.EQUITY),

        // 収益
        createAccount("4010", "売上高", AccountType.REVENUE),
        createAccount("4110", "受取利息", AccountType.REVENUE),
        createAccount("4210", "雑収入", AccountType.REVENUE),

        // 費用
        createAccount("5010", "仕入高", AccountType.EXPENSE),
        createAccount("5110", "給料手当", AccountType.EXPENSE),
        createAccount("5120", "賞与", AccountType.EXPENSE),
        createAccount("5210", "地代家賃", AccountType.EXPENSE),
        createAccount("5220", "水道光熱費", AccountType.EXPENSE),
        createAccount("5230", "通信費", AccountType.EXPENSE),
        createAccount("5240", "消耗品費", AccountType.EXPENSE),
        createAccount("5310", "支払利息", AccountType.EXPENSE)
    )

    fun findByCode(code: NonEmptyString): Result<Account, String> {
        return accounts.find { it.code == code }
            ?.let { Ok(it) }
            ?: Err("科目が存在しません: ${code.value}")
    }

    fun getAll(): List<Account> = accounts.toList()
    
    // ヘルパー関数
    private fun createAccount(code: String, name: String, type: AccountType): Account? {
        return Account.from(code, name, type).fold(
            success = { it },
            failure = { null }
        )
    }
}