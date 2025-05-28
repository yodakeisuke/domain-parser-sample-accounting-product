package domain_order.address

import com.github.michaelbull.result.*
import common.app_core.Showable

// property
@JvmInline
value class DetailedAddress(val value: String) // new type。ラップしただけ

// value
sealed class Address: Showable {
    sealed class Valid : Address() {
        data class Domestic(
            val prefecture: Prefecture,
            val detail: DetailedAddress
        ) : Valid()

        data class Overseas( // 海外は県がnull、なんてことはしない
            val detail: DetailedAddress
        ) : Valid()
    }

    data class Invalid(
        val prefecture: Prefecture?,
        val detail: DetailedAddress,
        val reason: String,
    ) : Address()

    // derived properties
    fun isDomestic(): Boolean = this is Valid.Domestic
    fun isValid(): Boolean = this is Valid
    override fun display() = when (this) {
        is Valid.Domestic -> "${prefecture.displayName} ${detail.value}"
        is Valid.Overseas -> "海外 ${detail.value}"
        is Invalid -> "不正な住所: ${detail.value} (${reason})"
    }
    // transition
    companion object {
        fun verify(address: Address, inputPrefecture: Prefecture?): Valid {
            return when (address) {
                is Valid -> address
                is Invalid -> createValidAddressFrom(inputPrefecture, address.detail)
            }
        }
        
        fun from(prefectureInput: String?, detailInput: String): Result<Address, String> {
            return binding {
                val detail = parseDetailedAddress(detailInput).bind()
                val prefectureOption = parsePrefectureInput(prefectureInput).bind()
                createAddress(prefectureOption, detail).bind()
            }
        }
    }
}

// business rules
internal fun parseDetailedAddress(detailInput: String): Result<DetailedAddress, String> {
    val trimmed = detailInput.trim()
    return if (trimmed.isEmpty()) {
        Err("住所詳細が空です")
    } else {
        Ok(DetailedAddress(trimmed))
    }
}

internal fun parsePrefectureInput(prefectureInput: String?): Result<Prefecture?, String> {
    val trimmed = prefectureInput.orEmpty().trim()
    return if (trimmed.isEmpty()) {
        Ok(null)
    } else {
        Prefecture.from(trimmed).map { it }
    }
}

internal fun createAddress(prefecture: Prefecture?, detail: DetailedAddress): Result<Address, String> {
    return Ok(createValidAddressFrom(prefecture, detail))
}

internal fun createValidAddressFrom(prefecture: Prefecture?, detail: DetailedAddress): Address.Valid {
    return if (prefecture != null) {
        Address.Valid.Domestic(prefecture, detail)
    } else {
        Address.Valid.Overseas(detail)
    }
}
