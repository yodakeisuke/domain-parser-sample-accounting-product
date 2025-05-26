package workflow

import com.github.michaelbull.result.*
import common.primitive.ID
import common.primitive.PositiveBigDecimal
import domain.command.journal_entry.JournalEntry
import domain.term.accounting.*
import domain.term.journal_data.JournalHeader
import domain.term.journal_data.JournalLine
import domain.read.AccountList
import effect.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JournalEntryRegistrationTest {

    @BeforeEach
    fun setup() {
        clearEventStore()
    }

    @Test
    fun `should register valid journal entry`() {
        val cashResult = Account.from("1010", "現金", AccountType.ASSET)
        val salesResult = Account.from("4010", "売上高", AccountType.REVENUE)
        
        // Resultの成功を確認
        assertTrue(cashResult.isOk)
        assertTrue(salesResult.isOk)
        
        val cash = cashResult.component1()!!
        val sales = salesResult.component1()!!

        val journalId = ID<JournalHeader>(UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"))
        val amountResult = PositiveBigDecimal.from(BigDecimal("10000"))
        assertTrue(amountResult.isOk)
        val amount = amountResult.component1()!!
        
        val request = RegisterJournalEntryRequest(
            header = JournalHeader(journalId, LocalDate.now()),
            lines = listOf(
                JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "現金売上"),
                JournalLine(sales, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "現金売上")
            )
        )

        // 関数を渡してワークフローを実行
        val result = registerJournalEntry(AccountList::findByCode, ::saveJournalEvent, request)

        // 成功を確認
        assertTrue(result.isOk)
        val event = result.component1()!!
        assertEquals(journalId, event.header.id)
        assertEquals(2, event.lines.size)

        // 保存されたイベントを確認
        val savedEvents = findJournalEvents(journalId)
        assertTrue(savedEvents.isOk)
        val events = savedEvents.component1()!!
        assertEquals(1, events.size)
    }

    @Test
    fun `should fail when journal entry is unbalanced`() {
        val cashResult = Account.from("1010", "現金", AccountType.ASSET)
        val salesResult = Account.from("4010", "売上高", AccountType.REVENUE)
        
        assertTrue(cashResult.isOk)
        assertTrue(salesResult.isOk)
        
        val cash = cashResult.component1()!!
        val sales = salesResult.component1()!!

        val journalId = ID<JournalHeader>(UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"))
        
        val amount1Result = PositiveBigDecimal.from(BigDecimal("10000"))
        val amount2Result = PositiveBigDecimal.from(BigDecimal("5000"))
        assertTrue(amount1Result.isOk)
        assertTrue(amount2Result.isOk)
        
        val amount1 = amount1Result.component1()!!
        val amount2 = amount2Result.component1()!!
        
        val request = RegisterJournalEntryRequest(
            header = JournalHeader(journalId, LocalDate.now()),
            lines = listOf(
                JournalLine(cash, AccountingAmount.Unsigned(amount1, DebitCredit.DEBIT), "現金売上"),
                JournalLine(sales, AccountingAmount.Unsigned(amount2, DebitCredit.CREDIT), "現金売上")
            )
        )

        val result = registerJournalEntry(AccountList::findByCode, ::saveJournalEvent, request)

        // エラーを確認
        assertTrue(result.isErr)
        val error = result.component2()!!
        assertTrue(error is JournalEntryRegistrationError.ValidationFailed)
        assertTrue(error.reason.contains("balance"))
    }

    @Test
    fun `should fail when less than two journal lines`() {
        val cashResult = Account.from("1010", "現金", AccountType.ASSET)
        assertTrue(cashResult.isOk)
        val cash = cashResult.component1()!!

        val journalId = ID<JournalHeader>(UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"))
        
        val amountResult = PositiveBigDecimal.from(BigDecimal("10000"))
        assertTrue(amountResult.isOk)
        val amount = amountResult.component1()!!
        
        val request = RegisterJournalEntryRequest(
            header = JournalHeader(journalId, LocalDate.now()),
            lines = listOf(
                JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "現金")
            )
        )

        val result = registerJournalEntry(AccountList::findByCode, ::saveJournalEvent, request)

        // エラーを確認
        assertTrue(result.isErr)
        val error = result.component2()!!
        assertTrue(error is JournalEntryRegistrationError.ValidationFailed)
        assertTrue(error.reason.contains("at least 2 lines"))
    }

    @Test
    fun `should fail when account does not exist`() {
        // 存在しない科目コードで科目を作成
        val invalidAccount = Account.from("9999", "存在しない科目", AccountType.ASSET).component1()!!
        val cashResult = Account.from("1010", "現金", AccountType.ASSET)
        
        assertTrue(cashResult.isOk)
        val cash = cashResult.component1()!!

        val journalId = ID<JournalHeader>(UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"))
        val amountResult = PositiveBigDecimal.from(BigDecimal("10000"))
        assertTrue(amountResult.isOk)
        val amount = amountResult.component1()!!
        
        val request = RegisterJournalEntryRequest(
            header = JournalHeader(journalId, LocalDate.now()),
            lines = listOf(
                JournalLine(invalidAccount, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "テスト"),
                JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "テスト")
            )
        )

        val result = registerJournalEntry(AccountList::findByCode, ::saveJournalEvent, request)

        // エラーを確認
        assertTrue(result.isErr)
        val error = result.component2()!!
        assertTrue(error is JournalEntryRegistrationError.ValidationFailed)
        assertTrue(error.reason.contains("無効な科目"))
    }

    @Test
    fun `should handle save failure`() {
        val cashResult = Account.from("1010", "現金", AccountType.ASSET)
        val salesResult = Account.from("4010", "売上高", AccountType.REVENUE)
        
        assertTrue(cashResult.isOk)
        assertTrue(salesResult.isOk)
        
        val cash = cashResult.component1()!!
        val sales = salesResult.component1()!!

        val journalId = ID<JournalHeader>(UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"))
        val amountResult = PositiveBigDecimal.from(BigDecimal("10000"))
        assertTrue(amountResult.isOk)
        val amount = amountResult.component1()!!
        
        val request = RegisterJournalEntryRequest(
            header = JournalHeader(journalId, LocalDate.now()),
            lines = listOf(
                JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "現金売上"),
                JournalLine(sales, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "現金売上")
            )
        )

        // 失敗する保存関数を渡す
        val failingSave: (JournalEntry) -> Result<JournalEntry, String> = { 
            Err("Database connection failed") 
        }
        
        val result = registerJournalEntry(AccountList::findByCode, failingSave, request)

        // エラーを確認
        assertTrue(result.isErr)
        val error = result.component2()!!
        assertTrue(error is JournalEntryRegistrationError.SaveFailed)
        assertEquals("Database connection failed", error.message)
    }
}