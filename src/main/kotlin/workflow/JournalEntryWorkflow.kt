package workflow

import com.github.michaelbull.result.*
import domain_accounting.command.journal_entry.JournalEntry
import domain_accounting.term.journal_data.JournalHeader
import domain_accounting.term.journal_data.JournalLine

data class RegisterJournalEntryRequest(
    val header: JournalHeader,
    val lines: List<JournalLine>
)

sealed interface JournalEntryRegistrationError {
    data class ValidationFailed(val reason: String) : JournalEntryRegistrationError
    data class SaveFailed(val message: String) : JournalEntryRegistrationError
}

internal fun requireExistingAccounts(
    lines: List<JournalLine>,
    findAccount: (common.primitive.NonEmptyString) -> Result<domain_accounting.term.accounting.Account, String>
): Result<List<JournalLine>, String> {
    lines.forEach { line ->
        findAccount(line.account.code).onFailure { error ->
            return Err("無効な科目が指定されました: ${error}")
        }
    }
    return Ok(lines)
}

fun registerJournalEntry(
    findAccount: (common.primitive.NonEmptyString) -> Result<domain_accounting.term.accounting.Account, String>,
    deriveEvent: (JournalEntry) -> Result<JournalEntry, String>,
    request: RegisterJournalEntryRequest
): Result<JournalEntry.Registered, JournalEntryRegistrationError> {
    return requireExistingAccounts(request.lines, findAccount)
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