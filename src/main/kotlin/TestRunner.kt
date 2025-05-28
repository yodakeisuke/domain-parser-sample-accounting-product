import com.github.michaelbull.result.*
import common.primitive.ID
import common.primitive.PositiveBigDecimal
import domain_accounting.read.AccountList
import domain_accounting.term.accounting.*
import domain_accounting.term.journal_data.JournalHeader
import domain_accounting.term.journal_data.JournalLine
import effect.rdb.JournalEntrySnapshot
import domain_accounting.command.journal_entry.JournalEntry
import workflow.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

fun main() {
    println("=== 仕訳登録ワークフローのテスト ===")
    
    // テスト1: 正常な仕訳登録
    println("\n1. 正常な仕訳登録のテスト")
    testValidJournalEntry()
    
    // テスト2: 存在しない科目でエラー
    println("\n2. 存在しない科目でのエラーテスト")
    testNonExistentAccount()
    
    // テスト3: 貸借不一致でエラー
    println("\n3. 貸借不一致でのエラーテスト")
    testUnbalancedEntry()
}

fun testValidJournalEntry() {
    val cashResult = Account.from("1010", "現金", AccountType.ASSET)
    val salesResult = Account.from("4010", "売上高", AccountType.REVENUE)
    
    if (cashResult.isErr || salesResult.isErr) {
        println("科目の作成に失敗しました")
        return
    }
    
    val cash = cashResult.component1()!!
    val sales = salesResult.component1()!!
    
    val journalId = ID<JournalHeader>(UUID.randomUUID())
    val amountResult = PositiveBigDecimal.from(BigDecimal("10000"))
    
    if (amountResult.isErr) {
        println("金額の作成に失敗しました")
        return
    }
    
    val amount = amountResult.component1()!!
    
    val request = RegisterJournalEntryRequest(
        header = JournalHeader(journalId, LocalDate.now()),
        lines = listOf(
            JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "現金売上"),
            JournalLine(sales, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "現金売上")
        )
    )
    
    val saveEvent: (JournalEntry) -> Result<JournalEntry, String> = { event ->
        JournalEntrySnapshot.save(event).map { event }
    }
    val result = registerJournalEntry(AccountList::findByCode, saveEvent, request)
    result.fold(
        success = { event -> println("✅ 成功: 仕訳が登録されました (ID: ${event.header.id.value})") },
        failure = { error -> println("❌ エラー: $error") }
    )
}

fun testNonExistentAccount() {
    // 存在しない科目コードで科目を作成
    val invalidAccount = Account.from("9999", "存在しない科目", AccountType.ASSET).component1()!!
    val cashResult = Account.from("1010", "現金", AccountType.ASSET)
    
    if (cashResult.isErr) {
        println("科目の作成に失敗しました")
        return
    }
    
    val cash = cashResult.component1()!!
    
    val journalId = ID<JournalHeader>(UUID.randomUUID())
    val amountResult = PositiveBigDecimal.from(BigDecimal("10000"))
    
    if (amountResult.isErr) {
        println("金額の作成に失敗しました")
        return
    }
    
    val amount = amountResult.component1()!!
    
    val request = RegisterJournalEntryRequest(
        header = JournalHeader(journalId, LocalDate.now()),
        lines = listOf(
            JournalLine(invalidAccount, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "テスト"),
            JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "テスト")
        )
    )
    
    val saveEvent: (JournalEntry) -> Result<JournalEntry, String> = { event ->
        JournalEntrySnapshot.save(event).map { event }
    }
    val result = registerJournalEntry(AccountList::findByCode, saveEvent, request)
    result.fold(
        success = { println("❌ エラー: 存在しない科目でも登録できてしまいました") },
        failure = { error -> 
            when (error) {
                is JournalEntryRegistrationError.ValidationFailed -> {
                    if (error.reason.contains("無効な科目")) {
                        println("✅ 成功: 期待通りのエラー「${error.reason}」")
                    } else {
                        println("❌ エラー: 異なるエラー「${error.reason}」")
                    }
                }
                else -> println("❌ エラー: 異なるエラータイプ $error")
            }
        }
    )
}

fun testUnbalancedEntry() {
    val cashResult = Account.from("1010", "現金", AccountType.ASSET)
    val salesResult = Account.from("4010", "売上高", AccountType.REVENUE)
    
    if (cashResult.isErr || salesResult.isErr) {
        println("科目の作成に失敗しました")
        return
    }
    
    val cash = cashResult.component1()!!
    val sales = salesResult.component1()!!
    
    val journalId = ID<JournalHeader>(UUID.randomUUID())
    val amount1Result = PositiveBigDecimal.from(BigDecimal("10000"))
    val amount2Result = PositiveBigDecimal.from(BigDecimal("5000"))
    
    if (amount1Result.isErr || amount2Result.isErr) {
        println("金額の作成に失敗しました")
        return
    }
    
    val amount1 = amount1Result.component1()!!
    val amount2 = amount2Result.component1()!!
    
    val request = RegisterJournalEntryRequest(
        header = JournalHeader(journalId, LocalDate.now()),
        lines = listOf(
            JournalLine(cash, AccountingAmount.Unsigned(amount1, DebitCredit.DEBIT), "テスト"),
            JournalLine(sales, AccountingAmount.Unsigned(amount2, DebitCredit.CREDIT), "テスト")
        )
    )
    
    val saveEvent: (JournalEntry) -> Result<JournalEntry, String> = { event ->
        JournalEntrySnapshot.save(event).map { event }
    }
    val result = registerJournalEntry(AccountList::findByCode, saveEvent, request)
    result.fold(
        success = { println("❌ エラー: 貸借不一致でも登録できてしまいました") },
        failure = { error -> 
            when (error) {
                is JournalEntryRegistrationError.ValidationFailed -> {
                    if (error.reason.contains("balance")) {
                        println("✅ 成功: 期待通りのエラー「${error.reason}」")
                    } else {
                        println("❌ エラー: 異なるエラー「${error.reason}」")
                    }
                }
                else -> println("❌ エラー: 異なるエラータイプ $error")
            }
        }
    )
}