package product.model.command.displayOrder

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.Ok

import product.model.type.Product
import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.PositiveInt

// input as commands
data class UpdateDisplayOrder(
    val productId: NonEmptyString,
    val newOrder: PositiveInt
)

// output as events, and transition actions as command method
sealed interface DisplayOrder {
    data object Empty : DisplayOrder
    data class Initialized(
        val orders: Map<NonEmptyString, PositiveInt>
    ) : DisplayOrder {
        fun updateOrder(command: UpdateDisplayOrder): Result<Initialized, String> =
            updateDisplayOrder(this, command)
    }
}

// actual operations
internal fun updateDisplayOrder(
    state: DisplayOrder.Initialized,
    command: UpdateDisplayOrder
): Result<DisplayOrder.Initialized, String> =
    Ok(
        DisplayOrder.Initialized(
            state.orders + (command.productId to command.newOrder)
        )
    )