package effect

import com.github.michaelbull.result.*
import domain.command.journal_entry.JournalEntry
import common.primitive.ID
import domain.term.journal_data.JournalHeader

// In-memory storage for testing
private val eventStore = mutableMapOf<ID<JournalHeader>, MutableList<JournalEntry>>()

// Effect functions
fun saveJournalEvent(event: JournalEntry): Result<JournalEntry, String> {
    val journalId = when (event) {
        is JournalEntry.Registered -> event.header.id
        is JournalEntry.Corrected -> event.journalId
        is JournalEntry.Approved -> event.journalId
    }
    
    eventStore.getOrPut(journalId) { mutableListOf() }.add(event)
    return Ok(event)
}

fun findJournalEvents(journalId: ID<JournalHeader>): Result<List<JournalEntry>, String> {
    return eventStore[journalId]?.let { Ok(it.toList()) }
        ?: Err("Journal not found: ${journalId.value}")
}

// Test helper to clear storage
fun clearEventStore() {
    eventStore.clear()
}