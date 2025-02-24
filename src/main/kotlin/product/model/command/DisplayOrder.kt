package product.model.command.displayOrder

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.Ok

import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.PositiveInt


// output as events, and transition actions as command method
sealed interface DisplayOrder {
    data object Empty : DisplayOrder
    data class Initialized(
        val orders:DisplayOrder
    ) : DisplayOrder {
        fun reorder(newOrder: DisplayOrder): Result<Reordered, String> =
            updateDisplayOrder(this, newOrder)
    }
    data object Reordered
}

// actual operations
internal fun updateDisplayOrder(
    state: DisplayOrder.Initialized,
    command: DisplayOrder,
): Result<DisplayOrder.Reordered, String> =
    Ok(
        DisplayOrder.Reordered // 省略
    )