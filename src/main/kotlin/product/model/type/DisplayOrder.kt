package product.model.type

import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.PositiveInt
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.getOrThrow
import common.model.type.primitive.ID

// model
typealias DisplayOrder = Map<ID<Product>, PositiveInt>
    fun DisplayOrder.getOrder(productId: ID<Product>): Result<PositiveInt, String> =
        this[productId]?.let { Ok(it) }
            ?: Err("指定された商品ID: ${productId.value.value}の表示順が見つかりません") // 想定される業務エラーではなく、例外

    fun DisplayOrder.addToFront(productId: ID<Product>): DisplayOrder =
        addProductToFirstPosition(incrementExistingOrders(this), productId)

    fun DisplayOrder.updateOrder(
        productId: ID<Product>,
        newOrder: PositiveInt
    ): Result<DisplayOrder, String> =
        validateProductExists(productId)
            .flatMap { validateNewOrder(newOrder) }
            .flatMap { recalculateOrders(productId, newOrder) }
            .flatMap { validateProductIdsUnchanged(it) }

// business rules
private fun DisplayOrder.validateProductExists(
    productId: ID<Product>
): Result<Unit, String> =
    if (productId in this) Ok(Unit)
    else Err("指定された商品ID: ${productId.value.value}は存在しません")

private fun DisplayOrder.validateNewOrder(
    newOrder: PositiveInt
): Result<Unit, String> {
    val maxOrder = this.values.maxOfOrNull { it.value } ?: 0
    return if (newOrder.value <= maxOrder) Ok(Unit)
    else Err("新しい表示順は現在の最大値(${maxOrder})までしか指定できません")
}

private fun DisplayOrder.recalculateOrders(
    productId: ID<Product>,
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
    productId: ID<Product>
): DisplayOrder {
    // 既存の商品の表示順序を1つずつ後ろにずらす
    val shiftedOrders = orders.mapValues { (_, order) ->
        PositiveInt.from(order.value + 1).getOrThrow { Throwable("あり得ないエラー") }
    }

    // 新商品を先頭（1番目）に配置する
    val firstPosition = PositiveInt.from(1).getOrThrow { Throwable("あり得ないエラー") }
    return shiftedOrders + (productId to firstPosition)
}

fun Map<ID<Product>, PositiveInt>.toDisplayOrder(): DisplayOrder = this
