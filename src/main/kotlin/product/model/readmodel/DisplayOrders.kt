package product.model.readmodel

import common.model.type.primitive.ID
import common.model.type.primitive.PositiveInt
import product.model.type.Product
import product.model.type.DisplayOrder

// protocol
typealias ReadDisplayOrders = () -> DisplayOrder

// サンプル用のインメモリ実装
val allDisplayOrders: ReadDisplayOrders = {
    val orders = mutableMapOf<ID<Product>, PositiveInt>()
    orders.toMap()
} 