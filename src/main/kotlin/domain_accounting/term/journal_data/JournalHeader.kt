package domain_accounting.term.journal_data

import common.primitive.ID
import java.time.LocalDate

data class JournalHeader(
    val id: ID<JournalHeader>,
    val date: LocalDate,
)