package workflow

import com.github.michaelbull.result.*
import domain.command.journal_entry.JournalEntry
import domain.term.journal_data.JournalHeader
import domain.term.journal_data.JournalLine

data class RegisterJournalEntryRequest(
    val header: JournalHeader,
    val lines: List<JournalLine>
)

sealed interface JournalEntryRegistrationError {
    data class ValidationFailed(val reason: String) : JournalEntryRegistrationError
    data class SaveFailed(val message: String) : JournalEntryRegistrationError
}

fun registerJournalEntry(
    findAccount: (common.primitive.NonEmptyString) -> Result<domain.term.accounting.Account, String>,
    deriveEvent: (JournalEntry) -> Result<JournalEntry, String>,
    request: RegisterJournalEntryRequest
): Result<JournalEntry.Registered, JournalEntryRegistrationError> {
    // まず科目の存在チェック
    return domain.command.journal_entry.requireExistingAccounts(request.lines, findAccount)
        .mapError { JournalEntryRegistrationError.ValidationFailed(it) }
        .andThen { 
            JournalEntry.register(request.header, request.lines)
                .mapError { JournalEntryRegistrationError.ValidationFailed(it.reason) }
        }
        .andThen { event ->
            deriveEvent(event)
                .mapError { JournalEntryRegistrationError.SaveFailed(it) }
                .map { it as JournalEntry.Registered }
        }
}