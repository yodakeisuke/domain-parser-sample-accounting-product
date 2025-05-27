package effect.rdb

import com.github.michaelbull.result.*
import domain.command.journal_entry.JournalEntry
import common.primitive.ID
import domain.term.journal_data.JournalHeader
import domain.term.journal_data.JournalLine
import java.time.LocalDate

data class JournalEntrySnapshot(
    val id: ID<JournalHeader>,
    val date: LocalDate,
    val lines: List<JournalLine>,
    val status: JournalEntryStatus,
    val version: Int
) {
    companion object {
        private val storage = mutableMapOf<ID<JournalHeader>, JournalEntrySnapshot>()
        
        fun save(event: JournalEntry): Result<JournalEntry, String> = when (event) {
            is JournalEntry.Registered -> {
                val snapshot = JournalEntrySnapshot(event.header.id, event.header.date, event.lines, JournalEntryStatus.REGISTERED, 1)
                storage[snapshot.id] = snapshot
                Ok(event)
            }
            is JournalEntry.Corrected -> storage[event.journalId]
                ?.let { current ->
                    storage[event.journalId] = current.copy(lines = event.lines, status = JournalEntryStatus.CORRECTED, version = current.version + 1)
                    Ok(event)
                }
                ?: Err("Journal not found: ${event.journalId.value}")
            is JournalEntry.Approved -> storage[event.journalId]
                ?.let { current ->
                    storage[event.journalId] = current.copy(status = JournalEntryStatus.APPROVED, version = current.version + 1)
                    Ok(event)
                }
                ?: Err("Journal not found: ${event.journalId.value}")
        }

        fun listAllLines() = storage.values.sortedByDescending { it.date }.flatMap { it.lines }

        fun clear() = storage.clear()
        
        fun findById(id: ID<JournalHeader>): Result<JournalEntrySnapshot, String> = 
            storage[id]?.let { Ok(it) } ?: Err("Journal not found: ${id.value}")

    }
}

enum class JournalEntryStatus { REGISTERED, CORRECTED, APPROVED }