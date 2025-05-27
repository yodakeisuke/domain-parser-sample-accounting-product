package effect.rdb

import com.github.michaelbull.result.*
import common.primitive.ID
import common.primitive.PositiveBigDecimal
import domain.command.journal_entry.JournalEntry
import domain.term.accounting.*
import domain.term.journal_data.JournalHeader
import domain.term.journal_data.JournalLine
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JournalEntryRepositoryTest {
    
    @BeforeEach
    fun setup() {
        JournalEntrySnapshot.clear()
    }
    
    @Test
    fun `listJournalEvents returns all journal lines sorted by entry date descending`() {
        // Given
        val cash = Account.from("1010", "現金", AccountType.ASSET).component1()!!
        val sales = Account.from("4010", "売上高", AccountType.REVENUE).component1()!!
        val amount = PositiveBigDecimal.from(BigDecimal("10000")).component1()!!
        
        // Create multiple journal entries with different dates
        val id1 = ID<JournalHeader>(UUID.randomUUID())
        val id2 = ID<JournalHeader>(UUID.randomUUID())
        val id3 = ID<JournalHeader>(UUID.randomUUID())
        
        val event1 = JournalEntry.Registered(
            JournalHeader(id1, LocalDate.of(2024, 1, 15)),
            listOf(
                JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "売上1"),
                JournalLine(sales, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "売上1")
            )
        )
        
        val event2 = JournalEntry.Registered(
            JournalHeader(id2, LocalDate.of(2024, 3, 20)),
            listOf(
                JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "売上2"),
                JournalLine(sales, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "売上2")
            )
        )
        
        val event3 = JournalEntry.Registered(
            JournalHeader(id3, LocalDate.of(2024, 2, 10)),
            listOf(
                JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "売上3"),
                JournalLine(sales, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "売上3")
            )
        )
        
        // When
        JournalEntrySnapshot.save(event1)
        JournalEntrySnapshot.save(event2)
        JournalEntrySnapshot.save(event3)
        
        val result = Ok(JournalEntrySnapshot.listAllLines())
        
        // Then
        assertTrue(result.isOk)
        val lines = result.component1()!!
        assertEquals(6, lines.size) // 3 entries x 2 lines each
        
        // Verify order - lines from most recent entry come first
        assertEquals("売上2", lines[0].description)
        assertEquals("売上2", lines[1].description)
        assertEquals("売上3", lines[2].description)
        assertEquals("売上3", lines[3].description)
        assertEquals("売上1", lines[4].description)
        assertEquals("売上1", lines[5].description)
    }
    
    @Test
    fun `listJournalEvents returns empty list when no entries exist`() {
        // When
        val result = Ok(JournalEntrySnapshot.listAllLines())
        
        // Then
        assertTrue(result.isOk)
        val lines = result.component1()!!
        assertEquals(0, lines.size)
    }
    
    @Test
    fun `listJournalEvents includes lines from entries with different statuses`() {
        // Given
        val cash = Account.from("1010", "現金", AccountType.ASSET).component1()!!
        val sales = Account.from("4010", "売上高", AccountType.REVENUE).component1()!!
        val amount = PositiveBigDecimal.from(BigDecimal("10000")).component1()!!
        
        val id1 = ID<JournalHeader>(UUID.randomUUID())
        val id2 = ID<JournalHeader>(UUID.randomUUID())
        
        val header1 = JournalHeader(id1, LocalDate.of(2024, 1, 1))
        val header2 = JournalHeader(id2, LocalDate.of(2024, 1, 2))
        
        val lines1 = listOf(
            JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "売上1"),
            JournalLine(sales, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "売上1")
        )
        
        val lines2 = listOf(
            JournalLine(cash, AccountingAmount.Unsigned(amount, DebitCredit.DEBIT), "売上2"),
            JournalLine(sales, AccountingAmount.Unsigned(amount, DebitCredit.CREDIT), "売上2")
        )
        
        // Register first entry
        JournalEntrySnapshot.save(JournalEntry.Registered(header1, lines1))
        
        // Register and approve second entry
        JournalEntrySnapshot.save(JournalEntry.Registered(header2, lines2))
        JournalEntrySnapshot.save(JournalEntry.Approved(id2))
        
        // When
        val result = Ok(JournalEntrySnapshot.listAllLines())
        
        // Then
        assertTrue(result.isOk)
        val allLines = result.component1()!!
        assertEquals(4, allLines.size) // 2 entries x 2 lines each
        
        // Lines from approved entry (newer date) come first
        assertEquals("売上2", allLines[0].description)
        assertEquals("売上2", allLines[1].description)
        assertEquals("売上1", allLines[2].description)
        assertEquals("売上1", allLines[3].description)
    }
}