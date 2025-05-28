package common.app_core

interface Event<E> {
    val eventId: String
    val timestamp: String
    val fact: E
}

interface DomainError {
    val code: String
    val msg: String
}

data class Env(
    val tenantId: String,
    val userId: String,
)

interface Showable {
    fun display(): String
}
