package common.model.type.primitive

import com.github.michaelbull.result.*


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
value class PositiveInt private constructor(val value: Int) {
    companion object {
        fun from(value: Int): Result<PositiveInt, String> {
            return if (value > 0) {
                Ok(PositiveInt(value))
            } else {
                Err("Value must be a positive integer")
            }
        }
    }

    fun plus(other: PositiveInt): PositiveInt {
        return PositiveInt(this.value + other.value)
    }

    fun plus(value: Int): Result<PositiveInt, String> {
        val result = this.value + value
        return if (result > 0) {
            Ok(PositiveInt(result))
        } else {
            Err("Resulting value must be a positive integer")
        }
    }

    fun minus(other: PositiveInt): Result<PositiveInt, String> {
        val result = this.value - other.value
        return if (result > 0) {
            Ok(PositiveInt(result))
        } else {
            Err("Resulting value must be a positive integer")
        }
    }

    fun minus(value: Int): Result<PositiveInt, String> {
        val result = this.value - value
        return if (result > 0) {
            Ok(PositiveInt(result))
        } else {
            Err("Resulting value must be a positive integer")
        }
    }
}
@JvmInline
value class ID<T> private constructor(val value: T) {
    companion object {
        fun <T> from(value: T): Result<ID<T>, NonEmptyString> = Ok(ID(value))
    }
}
