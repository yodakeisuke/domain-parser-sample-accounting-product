package domain_accounting.command.journal_entry

import com.github.michaelbull.result.*
import domain_accounting.term.journal_data.JournalHeader
import domain_accounting.term.journal_data.JournalLine
import common.primitive.ID

// aggregate
sealed interface JournalEntry {
    // event
    data class Registered(
        val header: JournalHeader,
        val lines: List<JournalLine>,
    ) : JournalEntry

    data class Corrected(
        val journalId: ID<JournalHeader>,
        val header: JournalHeader,
        val lines: List<JournalLine>,
    ) : JournalEntry

    data class Approved(
        val journalId: ID<JournalHeader>,
    ) : JournalEntry

    // command
    companion object {
        fun register(
            header: JournalHeader,
            lines: List<JournalLine>
        ): Result<Registered, Rejected> =
            requireMinimumTwoLines(lines)
            .andThen { requireBalancedEntry(it) }
            .map { Registered(header, it) }
            .mapError { Rejected(header.id, it) }

        fun correct(
            journal: Registered,
            header: JournalHeader,
            lines: List<JournalLine>
        ): Result<Corrected, Rejected> =
            requireMinimumTwoLines(lines)
                .andThen { requireBalancedEntry(it) }
                .map { Corrected(journal.header.id, header, it) }
                .mapError { Rejected(journal.header.id, it) }

        fun approve(
            journalId: ID<JournalHeader>
        ): Approved {
            return Approved(journalId)
        }
    }
}

// error path
sealed interface JournalEntryError
    data class Rejected(
        val journalId: ID<JournalHeader>,
        val reason: String,
    ) : JournalEntryError

// business rules
internal fun requireMinimumTwoLines(
    lines: List<JournalLine>,
): Result<List<JournalLine>, String> =
    if (lines.size >= 2) {
        Ok(lines)
    } else {
        Err("Journal entry must have at least 2 lines (current: ${lines.size})")
    }

internal fun requireBalancedEntry(
    lines: List<JournalLine>,
): Result<List<JournalLine>, String> {
    val totalDebits = JournalLine.sumDebits(lines)
    val totalCredits = JournalLine.sumCredits(lines)

    return if (totalDebits == totalCredits) {
        Ok(lines)
    } else {
        Err("Journal entry must balance: debits ($totalDebits) != credits ($totalCredits)")
    }
}