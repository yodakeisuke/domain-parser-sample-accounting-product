package product.model.type

import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.PositiveInt
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.getOrThrow

// model
typealias DisplayOrder = Map<NonEmptyString, PositiveInt>
    fun DisplayOrder.getOrder(productId: NonEmptyString): Result<PositiveInt, String> =
        this[productId]?.let { Ok(it) }
            ?: Err("指定された商品ID: ${productId.value}の表示順が見つかりません")

    fun DisplayOrder.addToFront(productId: NonEmptyString): DisplayOrder =
        incrementExistingOrders(this)
            .let { incrementedOrders -> addProductToFirstPosition(incrementedOrders, productId) }

    fun DisplayOrder.updateOrder(
        productId: NonEmptyString,
        newOrder: PositiveInt
    ): Result<DisplayOrder, String> =
        validateProductExists(productId)
            .flatMap { validateNewOrder(newOrder) }
            .flatMap { recalculateOrders(productId, newOrder) }
            .flatMap { validateProductIdsUnchanged(it) }

// business rules
private fun DisplayOrder.validateProductExists(
    productId: NonEmptyString
): Result<Unit, String> =
    if (productId in this) Ok(Unit)
    else Err("指定された商品ID: ${productId.value}は存在しません")

private fun DisplayOrder.validateNewOrder(
    newOrder: PositiveInt
): Result<Unit, String> {
    val maxOrder = this.values.maxOfOrNull { it.value } ?: 0
    return if (newOrder.value <= maxOrder) Ok(Unit)
    else Err("新しい表示順は現在の最大値(${maxOrder})までしか指定できません")
}

private fun DisplayOrder.recalculateOrders(
    productId: NonEmptyString,
    newOrder: PositiveInt
): Result<DisplayOrder, String> {
    val originalOrders = this
    return binding {
        val currentOrder = originalOrders[productId]!!.value
        
        val updatedOrders = originalOrders.mapValues { (_, order) ->
            when (order.value) {
                currentOrder -> newOrder
                in newOrder.value..currentOrder -> PositiveInt.from(order.value + 1).bind()
                in (currentOrder + 1)..newOrder.value -> PositiveInt.from(order.value - 1).bind()
                else -> order
            }
        }
        updatedOrders
    }
}

private fun DisplayOrder.validateProductIdsUnchanged(
    newOrders: DisplayOrder
): Result<DisplayOrder, String> =
    if (newOrders.keys == this.keys) Ok(newOrders)
    else Err("商品IDの集合が変更されています")

private fun incrementExistingOrders(orders: DisplayOrder): DisplayOrder = 
    orders.mapValues { (_, order) ->
        PositiveInt.from(order.value + 1).getOrThrow { Throwable("あり得ないエラー") }
    }

private fun addProductToFirstPosition(
    orders: DisplayOrder,
    productId: NonEmptyString
): DisplayOrder {
    val firstPosition = PositiveInt.from(1).getOrThrow { Throwable("あり得ないエラー") }
    return orders + (productId to firstPosition)
}
