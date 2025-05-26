package common.primitive

import com.github.michaelbull.result.*
import java.math.BigDecimal
import java.util.UUID

// primitive types
@JvmInline
value class NonEmptyString private constructor(val value: String) {
    companion object {
        fun from(value: String): Result<NonEmptyString, String> {
            return if (value.isNotBlank()) {
                Ok(NonEmptyString(value))
            } else {
                Err("String cannot be empty or blank")
            }
        }
    }
}

@JvmInline
value class PositiveBigDecimal private constructor(
    val value: BigDecimal
): Comparable<PositiveBigDecimal> {
    companion object {
        fun from(value: BigDecimal): Result<PositiveBigDecimal, String> {
            return if (value > BigDecimal.ZERO) {
                Ok(PositiveBigDecimal(value))
            } else {
                Err("Value must be a positive number")
            }
        }
        fun fromTrusted(value: BigDecimal): PositiveBigDecimal {
            return PositiveBigDecimal(value)
        }
    }

    fun plus(other: PositiveBigDecimal): PositiveBigDecimal {
        return PositiveBigDecimal(this.value + other.value)
    }

    fun plus(value: BigDecimal): Result<PositiveBigDecimal, String> {
        val result = this.value + value
        return if (result > BigDecimal.ZERO) {
            Ok(PositiveBigDecimal(result))
        } else {
            Err("Resulting value must be a positive number")
        }
    }

    fun minus(other: PositiveBigDecimal): Result<PositiveBigDecimal, String> {
        val result = this.value - other.value
        return if (result > BigDecimal.ZERO) {
            Ok(PositiveBigDecimal(result))
        } else {
            Err("Resulting value must be a positive number")
        }
    }

    fun minus(value: BigDecimal): Result<PositiveBigDecimal, String> {
        val result = this.value - value
        return if (result > BigDecimal.ZERO) {
            Ok(PositiveBigDecimal(result))
        } else {
            Err("Resulting value must be a positive number")
        }
    }

    override fun compareTo(other: PositiveBigDecimal): Int {
        return this.value.compareTo(other.value)
    }
}

@JvmInline
value class ID<T>(val value: UUID) {
    companion object {
        fun <T> generate(): ID<T> = ID(UUID.randomUUID())
    }
}